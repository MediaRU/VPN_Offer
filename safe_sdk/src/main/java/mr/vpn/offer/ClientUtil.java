package mr.vpn.offer;

import android.os.Build;

import java.util.Arrays;

import okhttp3.CipherSuite;
import okhttp3.ConnectionSpec;
import okhttp3.OkHttpClient;
import okhttp3.TlsVersion;

class ClientUtil {

    static OkHttpClient newClient() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            final ConnectionSpec spec = new ConnectionSpec.Builder(ConnectionSpec.MODERN_TLS)
                    .tlsVersions(TlsVersion.TLS_1_0)
                    .cipherSuites(
                            CipherSuite.TLS_ECDHE_ECDSA_WITH_AES_128_CBC_SHA,
                            CipherSuite.TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA
                    )
                    .build();
            return new OkHttpClient.Builder()
                    .connectionSpecs(Arrays.asList(spec, ConnectionSpec.CLEARTEXT))
                    .build();
        } else {
            return new OkHttpClient.Builder().build();
        }
    }
}
