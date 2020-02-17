package mr.vpn.offer;

import com.google.gson.Gson;

class SharedGson {

    private static Gson gson;

    static Gson get() {
        return gson != null ? gson : (gson = new Gson());
    }
}
