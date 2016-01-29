package org.sharedhealth.healthId.web.client;

import org.sharedhealth.healthId.web.Model.FacilityResponse;
import org.sharedhealth.healthId.web.config.HealthIdProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.concurrent.ListenableFutureAdapter;
import org.springframework.web.client.AsyncRestTemplate;
import org.springframework.web.client.RestClientException;

import java.util.concurrent.ExecutionException;

import static org.sharedhealth.healthId.web.utils.HttpUtil.AUTH_TOKEN_KEY;
import static org.sharedhealth.healthId.web.utils.HttpUtil.CLIENT_ID_KEY;


@Component
public class FacilityRegistryClient {
    private static final Logger logger = LoggerFactory.getLogger(FacilityRegistryClient.class);
    private AsyncRestTemplate restTemplate;
    private HealthIdProperties properties;

    @Autowired
    public FacilityRegistryClient(@Qualifier("HealthIdRestTemplate") AsyncRestTemplate restTemplate, HealthIdProperties properties) {
        this.restTemplate = restTemplate;
        this.properties = properties;
    }

    public FacilityResponse find(String facilityId) {
        String url = properties.getFrUrl() + "/" + facilityId + ".json";
        try {
            return getResponse(url);

        } catch (RestClientException | InterruptedException | ExecutionException e) {
            logger.debug("No facility found with URL: " + url, e);
            return null;
        }
    }

    protected FacilityResponse getResponse(final String url) throws ExecutionException, InterruptedException {
        return new ListenableFutureAdapter<FacilityResponse, ResponseEntity<FacilityResponse>>(restTemplate.exchange(
                url,
                HttpMethod.GET,
                buildHeaders(), FacilityResponse.class)) {

            @Override
            protected FacilityResponse adapt(ResponseEntity<FacilityResponse> result) throws ExecutionException {
                HttpStatus statusCode = result.getStatusCode();
                if (statusCode.is2xxSuccessful()) {
                    return result.getBody();
                }
                return null;
            }
        }.get();
    }

    protected HttpEntity buildHeaders() {
        MultiValueMap<String, String> header = new LinkedMultiValueMap<>();
        header.add(AUTH_TOKEN_KEY, properties.getIdpAuthToken());
        header.add(CLIENT_ID_KEY, properties.getIdpClientId());
        return new HttpEntity(header);
    }
}
