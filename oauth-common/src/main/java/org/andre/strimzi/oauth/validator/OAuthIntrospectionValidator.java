package org.andre.strimzi.oauth.validator;

import com.fasterxml.jackson.databind.JsonNode;
import org.andre.strimzi.oauth.common.TimeUtil;
import org.andre.strimzi.oauth.common.TokenInfo;
import org.apache.kafka.common.utils.Time;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSocketFactory;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import static org.andre.strimzi.oauth.common.HttpUtil.post;
import static org.andre.strimzi.oauth.common.OAuthAuthenticator.base64encode;

public class OAuthIntrospectionValidator implements TokenValidator{
    private static final Logger log = LoggerFactory.getLogger(OAuthIntrospectionValidator.class);

    private final URI introspectionURI;
    private final String validIssuerURI;
    private final String clientId;
    private final String clientSecret;
    private final boolean defaultChecks;
    private final String audience;
    private final SSLSocketFactory socketFactory;
    private final HostnameVerifier hostnameVerifier;
    public OAuthIntrospectionValidator(String introspectionEndpointUri,
                                       SSLSocketFactory socketFactory,
                                       HostnameVerifier verifier,
                                       String issuerUri,
                                       String clientId,
                                       String clientSecret,
                                       boolean defaultChecks,
                                       String audience) {

        if (introspectionEndpointUri == null) {
            throw new IllegalArgumentException("introspectionEndpointUri == null");
        }

        try {
            this.introspectionURI = new URI(introspectionEndpointUri);
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("Invalid introspection endpoint uri: " + introspectionEndpointUri, e);
        }

        if (socketFactory != null && !"https".equals(introspectionURI.getScheme())) {
            throw new IllegalArgumentException("SSL socket factory set but introspectionEndpointUri not 'https'");
        }
        this.socketFactory = socketFactory;

        if (verifier != null && !"https".equals(introspectionURI.getScheme())) {
            throw new IllegalArgumentException("Certificate hostname verifier set but keysEndpointUri not 'https'");
        }
        this.hostnameVerifier = verifier;

        try {
            new URI(issuerUri);
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("Invalid issuer uri: " + issuerUri, e);
        }
        this.validIssuerURI = issuerUri;

        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.defaultChecks = defaultChecks;
        this.audience = audience;
    }
    @Override
    public TokenInfo validate(String token) {
        String authorization = clientSecret != null ?
                "Basic " + base64encode(clientId + ':' + clientSecret) :
                null;

        StringBuilder body = new StringBuilder("token=").append(token);

        JsonNode response;
        try {
            response = post(introspectionURI, socketFactory, hostnameVerifier, authorization, "application/x-www-form-urlencoded", body.toString(), JsonNode.class);
        } catch (IOException e) {
            throw new RuntimeException("Failed to introspect token - send, fetch or parse failed: ", e);
        }

        boolean active;
        try {
            active = response.get("active").asBoolean();
        } catch (Exception e) {
            throw new RuntimeException("Failed to introspect token - invalid response: \"active\" attribute is missing or not a boolean (" + response.get("active") + ")");
        }

        if (!active) {
            throw new TokenExpiredException("Token has expired");
        }

        JsonNode value = null;

        value = response.get("exp");
        if (value == null) {
            throw new IllegalStateException("Introspection response contains no expires information (\"exp\"): " + response);
        }
        long expiresMillis = 1000 * value.asLong();
        if (Time.SYSTEM.milliseconds() > expiresMillis)    {
            throw new TokenExpiredException("The token expired at: " + expiresMillis + " (" +
                    TimeUtil.formatIsoDateTimeUTC(expiresMillis) + ")");
        }

        value = response.get("iat");
        if (value == null) {
            throw new IllegalStateException("Introspection response contains no issued time information (\"iat\"): " + response);
        }
        long iat = 1000 * value.asLong();


        value = response.get("sub");
        String subject = value != null ? value.asText() : null;

        if (defaultChecks) {
            value = response.get("iss");
            if (value == null || !validIssuerURI.equals(value.asText())) {
                throw new TokenValidationException("Token check failed - invalid issuer: " + value)
                        .status(TokenValidationException.Status.INVALID_TOKEN);
            }

            if (subject == null) {
                throw new TokenValidationException("Token check failed - invalid subject: " + subject)
                        .status(TokenValidationException.Status.INVALID_TOKEN);
            }

            value = response.get("token_type");
            if (value != null && !"access_token".equals(value.asText())) {
                throw new TokenValidationException("Token check failed - invalid token type: " + value)
                        .status(TokenValidationException.Status.UNSUPPORTED_TOKEN_TYPE);
            }
        }

        if (audience != null) {
            value = response.get("aud");
            if (value == null || !audience.equals(value.asText())) {
                throw new TokenValidationException("Token check failed - invalid audience: " + value)
                        .status(TokenValidationException.Status.INVALID_TOKEN);
            }
        }

        value = response.get("scope");
        String scopes = value != null ? value.asText() : null;

        return new TokenInfo(token, scopes, subject, iat, expiresMillis);
    }

}
