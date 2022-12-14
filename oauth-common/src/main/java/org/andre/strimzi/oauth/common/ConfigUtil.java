package org.andre.strimzi.oauth.common;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSocketFactory;

public class ConfigUtil {
    public static SSLSocketFactory createSSLFactory(Config config) {
        String truststore = config.getValue(Config.OAUTH_SSL_TRUSTSTORE_LOCATION);
        String password = config.getValue(Config.OAUTH_SSL_TRUSTSTORE_PASSWORD);
        String type = config.getValue(Config.OAUTH_SSL_TRUSTSTORE_TYPE);
        String rnd = config.getValue(Config.OAUTH_SSL_SECURE_RANDOM_IMPLEMENTATION);

        return SSLUtil.createSSLFactory(truststore, password, type, rnd);
    }

    public static HostnameVerifier createHostnameVerifier(Config config) {
        String hostCheck = config.getValue(Config.OAUTH_SSL_ENDPOINT_IDENTIFICATION_ALGORITHM, "HTTPS");
        return "".equals(hostCheck) ? SSLUtil.createAnyHostHostnameVerifier() : null;
    }
}
