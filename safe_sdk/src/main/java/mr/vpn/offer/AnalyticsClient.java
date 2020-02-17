package mr.vpn.offer;

import android.content.Context;

import java.util.Arrays;
import java.util.Locale;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

class AnalyticsClient {
    private static final String ACTION_STREAM = "STREAM";
    private static final String ACTION_DOWNLOAD = "DOWNLOAD";
    private static final String ACTION_INSTALL = "INSTALL";
    private static final String ACTION_ALREADY_INSTALL = "ALREADY_INSTALL";
    private static final String MEASUREMENT_HOST = "https://www.google-analytics.com/collect";
    private static final String TRACKING_ID = "UA-154884319-1";
    private static final int CATEGORY_MAX_LENGTH = 149;
    private OkHttpClient netClient;
    private String clientId;
    private String appName;
    private String dataSource;
    private String screenResolution;
    private String userLanguage;
    private String aid;
    private String applicationVersion;
    private String applicationInstallerId;

    AnalyticsClient(OkHttpClient netClient, String clientId, String appName, String dataSource, Context context) {
        this.netClient = netClient;
        this.clientId = clientId == null ? "" : clientId;
        this.appName = appName == null ? "" : verifyLength(Utils.encodeURL(appName));
        this.dataSource = dataSource;
        screenResolution = Utils.getScreenResolution(context);
        userLanguage = Locale.getDefault().getLanguage();
        aid = context != null ? context.getPackageName() : "";
        applicationVersion = Utils.getAppVersion(context);
        applicationInstallerId = Utils.getInstallSource(context);
    }

    private String verifyLength(String appName) {
        return appName.getBytes().length > CATEGORY_MAX_LENGTH ? new String(Arrays.copyOf(appName.getBytes(), CATEGORY_MAX_LENGTH)) : appName;
    }

    void trackStream(String publisher, String subId) {
        sendEvent(buildBody(ACTION_STREAM, publisher, subId));
    }

    void trackDownload(String publisher, String subId) {
        sendEvent(buildBody(ACTION_DOWNLOAD, publisher, subId));
    }

    void trackInstall(String publisher, String subId, long installTime) {
        sendEvent(buildBody(ACTION_INSTALL, publisher, subId, installTime));
    }

    void trackAlreadyInstalled(String publisher, String subId) {
        sendEvent(buildBody(ACTION_ALREADY_INSTALL, publisher, subId));
    }

    private void sendEvent(RequestBody body) {
        try {
            if (netClient != null)
                netClient.newCall(new Request.Builder().url(MEASUREMENT_HOST).post(body).addHeader("User-Agent", "").build()).enqueue(new EmptyHttpCallback());
        } catch (Exception ignored) {
        }
    }

    private RequestBody buildBody(String action, String publisher, String subId) {
        return buildBody(action, publisher, subId, 0);
    }

    private RequestBody buildBody(String action, String publisher, String subId, long installTime) {
        FormBody.Builder builder = new FormBody.Builder()
                .add("v", "1")
                .add("tid", TRACKING_ID)
                .add("cid", clientId)
                .add("t", "event")
                .addEncoded("ec", appName)
                .add("ea", action == null ? "" : action)
                .add("el", publisher == null ? "" : publisher)
                .add("ds", dataSource)
                .add("cn", subId == null ? "" : subId)
                .add("ci", subId == null ? "" : subId)
                .add("cs", publisher == null ? "" : publisher)
                .add("sr", screenResolution)
                .add("ul", userLanguage)
                .add("an", appName)
                .add("aid", aid)
                .add("av", applicationVersion)
                .add("aiid", applicationInstallerId);
        if (installTime > 0)
            builder.add("ev", String.valueOf(installTime));
        return builder.build();
    }

}
