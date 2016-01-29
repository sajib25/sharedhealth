package org.sharedhealth.healthId.web.client;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.sharedhealth.healthId.web.Model.FacilityResponse;
import org.sharedhealth.healthId.web.config.EnvironmentMock;
import org.sharedhealth.healthId.web.launch.WebMvcConfig;
import org.sharedhealth.healthId.web.utils.HttpUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.sharedhealth.healthId.web.utils.FileUtil.asString;

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration(initializers = EnvironmentMock.class, classes = WebMvcConfig.class)
public class FacilityRegistryClientTest {

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(9997);

    @Autowired
    private FacilityRegistryClient frClient;

    @Test
    public void shouldFetchAFacilityByFacilityIdWhenFacilityCatersToOneCatchment() throws Exception {
        String facilityId = "10000059";

        givenThat(get(urlEqualTo("/api/1.0/facilities/" + facilityId + ".json"))
                .withHeader(HttpUtil.CLIENT_ID_KEY, equalTo("18554"))
                .withHeader(HttpUtil.AUTH_TOKEN_KEY, equalTo("b43d2b284fa678fb8248b7cc3ab391f9c21e5d7f8e88f815a9ef4346e426bd33"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(asString("jsons/facility.json"))));

        FacilityResponse facility = frClient.find(facilityId);

        assertThat(facility, is(notNullValue()));
        assertThat(facility.getId(), is(facilityId));
    }
}