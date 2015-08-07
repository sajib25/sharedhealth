package org.sharedhealth.healthId.web.controller;

import com.github.tomakehurst.wiremock.client.WireMock;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.givenThat;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.sharedhealth.healthId.web.utils.FileUtil.asString;
import static org.sharedhealth.healthId.web.utils.HttpUtil.AUTH_TOKEN_KEY;
import static org.sharedhealth.healthId.web.utils.HttpUtil.CLIENT_ID_KEY;
import static org.sharedhealth.healthId.web.utils.HttpUtil.FROM_KEY;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringJUnit4ClassRunner.class)
public class HealthIdControllerIT extends BaseControllerTest {

    private static final String API_END_POINT = "/healthId";
    @Autowired
    protected WebApplicationContext webApplicationContext;

    @Before
    public void setUp() throws Exception {
        initMocks(this);
        setUpMockMvcBuilder();
    }

    @Test
    public void testGenerate() throws Exception {
        validAccessToken = "85HoExoxghh1pislg65hUM0q3wM9kfzcMdpYS0ixPD";
        validClientId = "18564";
        validEmail = "MciAdmin@test.com";

        mockMvc = MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
                .addFilters(springSecurityFilterChain)
                .build();

        givenThat(WireMock.get(urlEqualTo("/token/" + validAccessToken))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(asString("jsons/userDetails/userDetailForMCIAdmin.json"))));


        mockMvc.perform(post(API_END_POINT + "/generate")
                .accept(APPLICATION_JSON)
                .header(AUTH_TOKEN_KEY, validAccessToken)
                .header(FROM_KEY, validEmail)
                .header(CLIENT_ID_KEY, validClientId)
                .contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

    }

    @Test
    public void testGenerateRange() throws Exception {
        validAccessToken = "85HoExoxghh1pislg65hUM0q3wM9kfzcMdpYS0ixPD";
        validClientId = "18564";
        validEmail = "MciAdmin@test.com";

        mockMvc = MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
                .addFilters(springSecurityFilterChain)
                .build();

        givenThat(WireMock.get(urlEqualTo("/token/" + validAccessToken))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(asString("jsons/userDetails/userDetailForMCIAdmin.json"))));


        mockMvc.perform(post(API_END_POINT + "/generateRange?start=9800100100&end=9800100200")
                .accept(APPLICATION_JSON)
                .header(AUTH_TOKEN_KEY, validAccessToken)
                .header(FROM_KEY, validEmail)
                .header(CLIENT_ID_KEY, validClientId)
                .contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

    }

    @Test
    public void testGetNextBlock() throws Exception {
        validAccessToken = "85HoExoxghh1pislg65hUM0q3wM9kfzcMdpYS0ixPD";
        validClientId = "18564";
        validEmail = "MciAdmin@test.com";

        mockMvc = MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
                .addFilters(springSecurityFilterChain)
                .build();

        givenThat(WireMock.get(urlEqualTo("/token/" + validAccessToken))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(asString("jsons/userDetails/userDetailForMCIAdmin.json"))));


        mockMvc.perform(get(API_END_POINT + "/nextBlock")
                .accept(APPLICATION_JSON)
                .header(AUTH_TOKEN_KEY, validAccessToken)
                .header(FROM_KEY, validEmail)
                .header(CLIENT_ID_KEY, validClientId)
                .contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
    }

    @Test
    public void testGenerateOnlyForAdmins() throws Exception {
        validAccessToken = "40214a6c-e27c-4223-981c-1f837be90f02";
        validClientId = "18548";
        validEmail = "facility@gmail.com";

        mockMvc = MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
                .addFilters(springSecurityFilterChain)
                .build();

        givenThat(WireMock.get(urlEqualTo("/token/" + validAccessToken))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(asString("jsons/userDetails/userDetailForFacility.json"))));

        mockMvc.perform(post(API_END_POINT + "/generate")
                .accept(APPLICATION_JSON)
                .header(AUTH_TOKEN_KEY, validAccessToken)
                .header(FROM_KEY, validEmail)
                .header(CLIENT_ID_KEY, validClientId)
                .contentType(APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andReturn();
    }

    @Test
    public void testGetNextOnlyForAdmins() throws Exception {
        validAccessToken = "40214a6c-e27c-4223-981c-1f837be90f02";
        validClientId = "18548";
        validEmail = "facility@gmail.com";

        mockMvc = MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
                .addFilters(springSecurityFilterChain)
                .build();

        givenThat(WireMock.get(urlEqualTo("/token/" + validAccessToken))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(asString("jsons/userDetails/userDetailForFacility.json"))));

        mockMvc.perform(get(API_END_POINT + "/nextBlock")
                .accept(APPLICATION_JSON)
                .header(AUTH_TOKEN_KEY, validAccessToken)
                .header(FROM_KEY, validEmail)
                .header(CLIENT_ID_KEY, validClientId)
                .contentType(APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andReturn();
    }
}