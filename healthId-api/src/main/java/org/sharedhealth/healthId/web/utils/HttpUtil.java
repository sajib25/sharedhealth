package org.sharedhealth.healthId.web.utils;

import org.sharedhealth.healthId.web.config.HealthIdProperties;
import org.springframework.http.HttpHeaders;

public class HttpUtil {

    public static final String CLIENT_ID_KEY = "client_id";
    public static final String AUTH_TOKEN_KEY = "X-Auth-Token";
    public static final String FROM_KEY = "From";

    public static HttpHeaders getHrmIdentityHeaders(HealthIdProperties properties) {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add(CLIENT_ID_KEY, properties.getIdpClientId());
        httpHeaders.add(AUTH_TOKEN_KEY, properties.getIdpAuthToken());
        return httpHeaders;

    }
}
