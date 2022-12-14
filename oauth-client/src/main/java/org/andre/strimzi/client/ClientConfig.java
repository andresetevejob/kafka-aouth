package org.andre.strimzi.client;

import org.andre.strimzi.oauth.common.Config;

public class ClientConfig extends Config {

    public static final String OAUTH_ACCESS_TOKEN = "oauth.access.token";
    public static final String OAUTH_REFRESH_TOKEN = "oauth.refresh.token";
    public static final String OAUTH_TOKEN_ENDPOINT_URI = "oauth.token.endpoint.uri";

    public ClientConfig() {
    }

    public ClientConfig(java.util.Properties p) {
        super(p);
    }
}
