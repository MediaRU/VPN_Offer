package mr.vpn.offer;

import android.content.SharedPreferences;

class PrefStorage implements Storage {

    private static final String KEY_CONFIG = "key_config";
    private SharedPreferences pref;

    PrefStorage(SharedPreferences pref) {
        this.pref = pref;
    }

    @Override
    public void storeConfig(String configJson) {
        try {
            final SharedPreferences.Editor editor = pref.edit();
            editor.putString(KEY_CONFIG, configJson);
            editor.apply();
        } catch (Exception ignored) {
        }
    }

    @Override
    public String readConfig() {
        String stored = "{}";
        try {
            stored = pref.getString(KEY_CONFIG, "{}");
        } catch (Exception ignored) {
        }
        return stored;
    }
}
