package mr.vpn.offer;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.WindowManager;

import androidx.annotation.NonNull;

import java.net.NetworkInterface;
import java.net.URLEncoder;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

class Utils {

    private static final String TARGET_MOBILE = "mobile";
    private static final String TARGET_TV = "tv";
    private static final String TARGET_AMAZON = "amazon";

    private static final String AMAZON_PACKAGE = "com.amazon";
    private static final String GP_PACKAGE = "com.android.vending";

    private static final String DEFAULT_PKG_MOBILE = "torrent.safe.watch";
    private static final String DEFAULT_PKG_TV = "safewatch.tv.vpn";
    private static final String DEFAULT_PKG_AMAZON = "playsafe.tv.vpn";

    private static final String DEFAULT_INSTALL_MOBILE = "https://play.google.com/store/apps/details?id=torrent.safe.watch&launch=true";
    private static final String DEFAULT_INSTALL_TV = "https://play.google.com/store/apps/details?id=safewatch.tv.vpn&launch=true";
    private static final String DEFAULT_INSTALL_AMAZON = "http://www.amazon.com/gp/mas/dl/android?p=playsafe.tv.vpn";

    static String getDeviceType(Context context) {
        return isTv(context) ? "tv" : isTablet(context.getResources()) ? "tablet" : "phone";
    }

    private static boolean isTablet(Resources context) {
        if (context == null) {
            return false;
        } else {
            boolean isTablet;
            tabletBlock:
            {
                if ((context.getConfiguration().screenLayout & 15) <= 3) {
                    Configuration configuration = context.getConfiguration();
                    if (!((configuration.screenLayout & 15) <= 3 && configuration.smallestScreenWidthDp >= 600)) {
                        isTablet = false;
                        break tabletBlock;
                    }
                }

                isTablet = true;
            }

            return isTablet;
        }
    }

    private static boolean isTv(Context context) {
        return context.getPackageManager().hasSystemFeature("com.google.android.tv")
                || context.getPackageManager().hasSystemFeature("android.hardware.type.television")
                || context.getPackageManager().hasSystemFeature("android.software.leanback");
    }

    static long getInstallTime(Context context) {
        long installTime;
        try {
            installTime = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).firstInstallTime;
        } catch (PackageManager.NameNotFoundException e) {
            installTime = System.currentTimeMillis();
        }
        return installTime;
    }

    static String getInstallSource(Context context) {
        String ipn = "";
        try {
            PackageManager pkgManager = context.getPackageManager();
            ipn = pkgManager.getInstallerPackageName(context.getPackageName());
        } catch (Exception ignored) {
        }
        return ipn != null ? ipn : "";
    }

    static String detectTarget(Context context) {
        String target = getTargetFromInstallSource(context);
        try {
            if (TextUtils.isEmpty(target))
                if (searchForPackage(context, GP_PACKAGE)) {
                    target = context.getPackageManager().hasSystemFeature("android.hardware.hdmi.cec") ? TARGET_TV : TARGET_MOBILE;
                } else if (searchForPackage(context, AMAZON_PACKAGE))
                    target = TARGET_AMAZON;
                else
                    target = TARGET_MOBILE;
        } catch (Exception ignored) {
        }
        return target;
    }

    static boolean searchForPackage(Context context, String pkg) {
        if (pkg != null)
            try {
                context.getPackageManager().getPackageInfo(pkg, 0);
                return true;
            } catch (PackageManager.NameNotFoundException ignore) {
            }
        return false;
    }

    private static String getTargetFromInstallSource(Context context) {
        try {
            PackageManager pkgManager = context.getPackageManager();
            String ipn = pkgManager.getInstallerPackageName(context.getPackageName());
            if (!TextUtils.isEmpty(ipn))
                if (ipn.startsWith(AMAZON_PACKAGE)) {
                    return TARGET_AMAZON;
                } else if (GP_PACKAGE.equals(ipn)) {
                    return TARGET_MOBILE;
                }
        } catch (Exception ignored) {
        }
        return "";
    }

    static String getDefaultInstallPkg(String target) {
        String pkg = DEFAULT_PKG_MOBILE;
        switch (target) {
            case TARGET_TV:
                pkg = DEFAULT_PKG_TV;
                break;
            case TARGET_AMAZON:
                pkg = DEFAULT_PKG_AMAZON;
                break;
        }
        return pkg;
    }

    static String getDefaultInstallUrl(String target) {
        String installUrl = DEFAULT_INSTALL_MOBILE;
        switch (target) {
            case TARGET_TV:
                installUrl = DEFAULT_INSTALL_TV;
                break;
            case TARGET_AMAZON:
                installUrl = DEFAULT_INSTALL_AMAZON;
                break;
        }
        return installUrl;
    }

    static String encodeURL(String url) {
        String result = url;
        try {
            result = URLEncoder.encode(url, "UTF-8");
        } catch (Exception ignored) {
        }
        return result;
    }

    static String getDeviceId(Context context) {
        String deviceId = "";
        try {
            final String keyDeviceId = "device_id";
            final SharedPreferences preferences = context.getSharedPreferences("OFFER_SDK", Context.MODE_PRIVATE);
            deviceId = preferences.getString(keyDeviceId, null);
            if (TextUtils.isEmpty(deviceId)) {
                final String androidId = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
                String macAddress = getMACAddress("wlan0");
                if (TextUtils.isEmpty(macAddress)) {
                    macAddress = getMACAddress("eth0");
                }
                final String name = androidId
                        + "-" + name_part(Build.SERIAL, androidId)
                        + "-" + name_part(Build.BOARD, androidId)
                        + "-" + (TextUtils.isEmpty(macAddress) ? androidId : macAddress);
                deviceId = UUID.nameUUIDFromBytes(name.getBytes()).toString().replaceAll("-", "");
                preferences.edit().putString(keyDeviceId, deviceId).apply();
            }
        } catch (Exception ignored) {
        }
        return deviceId;
    }

    private static String getMACAddress(@NonNull String interfaceName) {
        try {
            List<NetworkInterface> networkInterfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface networkInterface : networkInterfaces) {
                if (!networkInterface.getName().equalsIgnoreCase(interfaceName)) {
                    continue;
                }
                byte[] mac = networkInterface.getHardwareAddress();
                if (mac != null) {
                    StringBuilder builder = new StringBuilder();
                    for (byte b : mac) {
                        builder.append(String.format("%02X:", b));
                    }
                    if (builder.length() > 0) {
                        builder.deleteCharAt(builder.length() - 1);
                    }
                    return builder.toString();
                }
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    private static String name_part(String part, String def) {
        return TextUtils.isEmpty(part) || Build.UNKNOWN.equals(part) ? def : part;
    }

    static String getScreenResolution(Context context) {
        String resolution = "";
        try {
            DisplayMetrics metrics = new DisplayMetrics();
            ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getMetrics(metrics);
            int width = metrics.widthPixels;
            int height = metrics.heightPixels;
            resolution = width + "x" + height;
        } catch (Exception ignored) {
        }
        return resolution;
    }

    static String getAppVersion(Context context) {
        String version = "";
        try {
            PackageInfo pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            version = pInfo.versionName;
        } catch (Exception ignored) {
        }
        return version;
    }
}
