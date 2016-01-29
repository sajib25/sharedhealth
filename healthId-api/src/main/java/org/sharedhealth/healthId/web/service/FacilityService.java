package org.sharedhealth.healthId.web.service;

import org.sharedhealth.healthId.web.Model.FacilityResponse;
import org.sharedhealth.healthId.web.client.FacilityRegistryClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static java.lang.String.format;

@Component
public class FacilityService {

    private static final Logger logger = LoggerFactory.getLogger(FacilityService.class);
    private FacilityRegistryClient client;

    @Autowired
    public FacilityService(FacilityRegistryClient facilityRegistryClient) {
        this.client = facilityRegistryClient;
    }

    public FacilityResponse find(String facilityId) {
        logger.debug(format("Find facility for facilityId: (%s)", facilityId));
        return client.find(facilityId);
    }

}
