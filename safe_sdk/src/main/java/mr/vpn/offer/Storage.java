package mr.vpn.offer;

interface Storage {

    void storeConfig(String configJson);

    String readConfig();
}
