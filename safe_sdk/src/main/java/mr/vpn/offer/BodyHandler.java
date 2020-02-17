package mr.vpn.offer;

import androidx.annotation.NonNull;

import com.google.gson.JsonObject;

class BodyHandler {

    private final String applicationName;
    private final String deviceId;
    private final String device;
    private final String os;
    private final String version;
    private final long installTime;
    private final String osLang;
    private final String deviceType;
    private String installSource;
    private String pubId;
    private String subId;

    BodyHandler(@NonNull String applicationName,
                @NonNull String deviceId,
                @NonNull String device,
                @NonNull String os,
                @NonNull String version,
                long installTime,
                @NonNull String installSource,
                @NonNull String osLang,
                @NonNull String deviceType,
                @NonNull String pubId,
                @NonNull String subId) {
        this.applicationName = applicationName;
        this.deviceId = deviceId;
        this.device = device;
        this.os = os;
        this.version = version;
        this.installTime = installTime;
        this.installSource = installSource;
        this.osLang = osLang;
        this.deviceType = deviceType;
        this.pubId = pubId;
        this.subId = subId;
    }

    JsonObject buildBody() {
        final JsonObject body = new JsonObject();
        body.addProperty("app", applicationName);
        body.addProperty("request_name", applicationName);
        body.addProperty("device_id", deviceId);
        body.addProperty("device_type", deviceType);
        body.addProperty("device", device);
        body.addProperty("os", os);
        body.addProperty("version", version);
        body.addProperty("installTimeMs", installTime);
        body.addProperty("installSource", installSource);
        body.addProperty("os_lang", osLang);
        body.addProperty("pubId", pubId);
        body.addProperty("subId", subId);
        return body;
    }
}
