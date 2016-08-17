package org.sharedhealth.healthId.web.security;

import org.sharedhealth.healthId.web.config.HealthIdProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.stereotype.Component;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.web.client.AsyncRestTemplate;

import javax.naming.AuthenticationException;
import java.util.concurrent.ExecutionException;

@Component
public class IdentityServiceClient {
    public static final String CLIENT_ID_KEY = "client_id";
    public static final String AUTH_TOKEN_KEY = "X-Auth-Token";
    public static final String FROM_KEY = "From";

    private AsyncRestTemplate restTemplate;
    private HealthIdProperties healthIdProperties;
    private ClientAuthenticator clientAuthentication;

    private final static Logger logger = LoggerFactory.getLogger(IdentityServiceClient.class);

    @Autowired
    public IdentityServiceClient(@Qualifier("HealthIdRestTemplate") AsyncRestTemplate restTemplate,
                                 HealthIdProperties healthIdProperties,
                                 ClientAuthenticator clientAuthentication) {
        this.restTemplate = restTemplate;
        this.healthIdProperties = healthIdProperties;
        this.clientAuthentication = clientAuthentication;
    }

    @Cacheable(value = "identityCache", unless = "#result == null")
    public TokenAuthentication authenticate(UserAuthInfo userAuthInfo, String token) throws AuthenticationException, ExecutionException,
            InterruptedException {
        String userInfoUrl = ensureEndsWithBackSlash(healthIdProperties.getIdentityServerBaseUrl()) + token;
        HttpHeaders httpHeaders = getHrmIdentityHeaders(healthIdProperties);
        ListenableFuture<ResponseEntity<UserInfo>> listenableFuture = restTemplate.exchange(userInfoUrl,
                HttpMethod.GET,
                new HttpEntity(httpHeaders), UserInfo.class);
        ResponseEntity<UserInfo> responseEntity;
        try {
            responseEntity = listenableFuture.get();
        } catch (Exception e) {
            logger.error(String.format("Error while validating client with email %s", userAuthInfo.getEmail()));
            throw new AuthenticationServiceException("Unable to authenticate user.");
        }
        if (!responseEntity.getStatusCode().is2xxSuccessful())
            throw new AuthenticationServiceException("Identity Server responded :" + responseEntity.getStatusCode()
                    .toString());
        UserInfo userInfo = responseEntity.getBody();
        boolean isAuthenticated = clientAuthentication.authenticate(userAuthInfo, token, userInfo);
        return new TokenAuthentication(userInfo, isAuthenticated);
    }

    public static String ensureEndsWithBackSlash(String value) {
        String trimmedValue = value.trim();
        if (!trimmedValue.endsWith("/")) {
            return trimmedValue + "/";
        } else {
            return trimmedValue;
        }
    }

    public static HttpHeaders getHrmIdentityHeaders(HealthIdProperties healthIdProperties) {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add(CLIENT_ID_KEY, healthIdProperties.getIdpClientId());
        httpHeaders.add(AUTH_TOKEN_KEY, healthIdProperties.getIdpAuthToken());
        return httpHeaders;

    }
}
