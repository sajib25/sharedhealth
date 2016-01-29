package org.sharedhealth.healthId.web.service;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.sharedhealth.healthId.web.Model.FacilityResponse;
import org.sharedhealth.healthId.web.client.FacilityRegistryClient;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

public class FacilityServiceTest {


    @Mock
    private FacilityRegistryClient facilityRegistryClient;

    private FacilityService facilityService;

    @Before
    public void setUp() {
        initMocks(this);
        facilityService = new FacilityService(facilityRegistryClient);
    }

    @Test
    public void shouldQueryFacilityRegistry() {
        FacilityResponse facility = new FacilityResponse();
        facility.setId("1");
        facility.setName("foo");

        when(facilityRegistryClient.find(facility.getId())).thenReturn(facility);
        facilityService.find(facility.getId());
        verify(facilityRegistryClient).find(facility.getId());
    }
}
