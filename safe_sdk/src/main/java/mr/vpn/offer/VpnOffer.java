package mr.vpn.offer;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Log;

import java.io.IOException;
import java.util.Collections;
import java.util.Locale;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import sm.euzee.github.com.servicemanager.ServiceManager;

public class VpnOffer {

    private static final String OFFER_SDK = "OFFER_SDK";
    private static final String GOOGLE_PLAY_LINK = "https://play.google.com/";
    private static final long INSTALLATION_CHECK_INTERVAL = 500;
    private static final String REFERRER = "&referrer=";
    private static final String ENDPOINT_PATH = "/sdk_config/1?encryptRequestBody=1&decryptResponseBody=1";
    private static final String SESSION = "session";
    private static final String SESSION_ID = "66327e179d54456b990ebcbbd5145258";
    private static final String SDK_NAME = "media_ru_vpn_offer";
    private static final String SDK_VERSION = "1.0";
    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    private static String target;
    private static String publisherId;
    private static OkHttpClient client;
    private static AnalyticsClient analyticsClient;
    //TODO set default fallback URL's
    private static String[] fallbackIps = new String[]{"https://configmr.com", "https://cndmr.net", "https://cdnmr.net"};
    private static Storage storage;
    private static long AWAIT_PACKAGE_INSTALLATION_TIME = 300000;
    private static String deviceId;
    private static String subId;
    private static String appName;
    private static RequestBody requestBody;

    public static void initWith(Context context, String publisherId, String subId) {
        target = Utils.detectTarget(context);
        VpnOffer.publisherId = publisherId;
        VpnOffer.deviceId = Utils.getDeviceId(context);
        VpnOffer.subId = subId;
        setAppName(context);
        setUp(context.getApplicationContext());
        ConcurrentLinkedQueue<String> fallbackQueue = new ConcurrentLinkedQueue<>();
        Collections.addAll(fallbackQueue, fallbackIps);
        String jsonBody = new BodyHandler(SDK_NAME,
                deviceId,
                Build.MODEL,
                "android" + Build.VERSION.RELEASE,
                SDK_VERSION,
                Utils.getInstallTime(context),
                Utils.getInstallSource(context),
                Locale.getDefault().getLanguage(),
                Utils.getDeviceType(context),
                publisherId,
                subId
        ).buildBody().toString();
        Log.e(OFFER_SDK, jsonBody);
        requestBody = RequestBody.create(JSON, jsonBody);
        requestConfiguration(fallbackQueue);
    }

    private static void setAppName(Context context) {
        VpnOffer.appName = "";
        try {
            VpnOffer.appName = context.getApplicationInfo().loadLabel(context.getPackageManager()).toString();
        } catch (Exception ignored) {

        }
    }

    public static void run(Context context, MediaInfoModel info) {
        stream(context, info);
    }

    public static void stream(Context context, MediaInfoModel info) {
        getAnalyticsClient(context).trackStream(publisherId, subId);
        if (info != null)
            info.setMode(MaskParser.MODE_WATCH);
        launch(context, info);
    }

    public static void download(Context context, MediaInfoModel info) {
        getAnalyticsClient(context).trackDownload(publisherId, subId);
        if (info != null)
            info.setMode(MaskParser.MODE_DOWNLOAD);
        launch(context, info);
    }

    private static void launch(final Context context, MediaInfoModel info) {
        try {
            if (info != null) {
                info.setPid(publisherId);
                info.setSid(subId);
            }
            final ConfigEntity entity = SharedGson.get().fromJson(getStorage(context).readConfig(), ConfigEntity.class);
            final String maskedScheme = MaskParser.parseMask(entity.getSchemeMask(), info == null ? new MediaInfoModel.Builder().pid(publisherId).subid(subId).build() : info);
            String[] packages = entity.getVerificationPackages();
            String installedApp = null;
            Intent intent;
            if (packages != null)
                for (String pack : packages)
                    if (!TextUtils.isEmpty(pack) && Utils.searchForPackage(context, pack)) {
                        installedApp = pack;
                        break;
                    }

            //if there is no packages - it means that we want only install new app without fallbacks
            if (installedApp != null)
                intent = buildIntentForInstalledApp(context, installedApp, Uri.parse(maskedScheme));
            else {
                intent = new Intent();
                String installUrl;
                final String installationPackage;
                if (TextUtils.isEmpty(entity.getInstallUrl()) || TextUtils.isEmpty(entity.getInstallationPackage())) {
                    installUrl = Utils.getDefaultInstallUrl(target);
                    installationPackage = Utils.getDefaultInstallPkg(target);
                } else {
                    installUrl = entity.getInstallUrl();
                    installationPackage = entity.getInstallationPackage();
                }
                if (Utils.searchForPackage(context, installationPackage)) {
                    getAnalyticsClient(context).trackAlreadyInstalled(publisherId, subId);
                    intent = buildIntentForInstalledApp(context, installationPackage, Uri.parse(maskedScheme));
                } else {
                    ServiceManager.runService(context, () -> {
                        try {
                            long terminationTime = System.currentTimeMillis() + AWAIT_PACKAGE_INSTALLATION_TIME;
                            boolean jobFinished = false;
                            while (System.currentTimeMillis() < terminationTime && !jobFinished) {
                                SystemClock.sleep(INSTALLATION_CHECK_INTERVAL);
                                jobFinished = Utils.searchForPackage(context, installationPackage);
                            }
                            if (jobFinished) {
                                getAnalyticsClient(context).trackInstall(publisherId, subId, (System.currentTimeMillis() - (terminationTime - AWAIT_PACKAGE_INSTALLATION_TIME)) / 1000L);
                                if (!installUrl.startsWith(GOOGLE_PLAY_LINK))
                                    launchAppWithData(context, entity.getInstallationPackage(), Uri.parse(maskedScheme));
                            }
                        } catch (Exception ignored) {
                        }
                    }, true);
                    intent.setData(Uri.parse(!installUrl.startsWith(GOOGLE_PLAY_LINK) ? installUrl :
                            installUrl + REFERRER + Utils.encodeURL(maskedScheme)));
                }
            }
            intent.setAction(Intent.ACTION_VIEW);
            context.startActivity(intent);
        } catch (Exception e) {
            Log.e(OFFER_SDK, "Error during launch, try to reinitialize VpnOffer");
        }
    }

    private static Intent buildIntentForInstalledApp(Context context, String installedApp, Uri data) {
        // prepare content to launch installed app
        Intent intent = null;
        try {
            intent = context.getPackageManager().getLaunchIntentForPackage(installedApp);
        } catch (Exception ignored) {
        }
        if (intent == null)
            intent = new Intent();
        intent.setData(data);
        intent.setPackage(installedApp);
        return intent;
    }

    private static void launchAppWithData(Context context, String installedPack, Uri data) {
        try {
            PendingIntent.getActivity(context, 0, buildIntentForInstalledApp(context, installedPack, data).setAction(Intent.ACTION_VIEW), 0).send();
        } catch (Exception ignored) {
        }
    }

    private static void requestConfiguration(final ConcurrentLinkedQueue<String> fallbackQueue) {
        if (fallbackQueue != null && !fallbackQueue.isEmpty()) {
            try {
                String url = fallbackQueue.poll();
                client.newCall(new Request.Builder()
                        .url(url + ENDPOINT_PATH)
                        .addHeader(SESSION, SESSION_ID)
                        .post(requestBody)
                        .build())
                        .enqueue(new Callback() {
                            @Override
                            public void onFailure(Call call, IOException e) {
                                requestConfiguration(fallbackQueue);
                            }

                            @Override
                            public void onResponse(Call call, Response response) {
                                ResponseBody responseBody = response.body();
                                if (!response.isSuccessful() || responseBody == null) {
                                    requestConfiguration(fallbackQueue);
                                    return;
                                }
                                handleResponse(responseBody);
                                Log.i(OFFER_SDK, "successful initialization");
                            }
                        });
            } catch (Exception e) {
                requestConfiguration(fallbackQueue);
            }
        } else {
            Log.e(OFFER_SDK, "unable to get configuration response from server. Default configuration will be used");
        }
    }

    private static void handleResponse(ResponseBody body) {
        try {
            String bodyString = body.string();
            SharedGson.get().fromJson(bodyString, ConfigEntity.class);
            // parse config before storing
            storage.storeConfig(bodyString);
        } catch (Exception e) {
            Log.e(OFFER_SDK, "unable to parse server configuration");
        }

    }

    private static AnalyticsClient getAnalyticsClient(Context context) {
        return analyticsClient != null ? analyticsClient :
                (analyticsClient = new AnalyticsClient(ClientUtil.newClient().newBuilder().readTimeout(5, TimeUnit.SECONDS).connectTimeout(5, TimeUnit.SECONDS).build(), deviceId, appName, target, context));
    }

    private static void setUp(Context context) {
        if (client == null)
            client = ClientUtil.newClient().newBuilder().readTimeout(5, TimeUnit.SECONDS).connectTimeout(5, TimeUnit.SECONDS).build();
        getStorage(context);
    }

    private static Storage getStorage(Context context) {
        return storage != null ? storage : (storage = new PrefStorage(context.getSharedPreferences(OFFER_SDK, Context.MODE_PRIVATE)));
    }
}
