package org.sharedhealth.healthId.web.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.client.WireMock;
import org.apache.tomcat.util.http.fileupload.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.sharedhealth.healthId.web.config.HealthIdProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import javax.servlet.Filter;
import java.io.File;
import java.util.List;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.givenThat;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.junit.Assert.assertTrue;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.sharedhealth.healthId.web.controller.HealthIdController.GENERATE_ALL_URI;
import static org.sharedhealth.healthId.web.controller.HealthIdController.GENERATE_BLOCK_FOR_ORG_URI;
import static org.sharedhealth.healthId.web.controller.HealthIdController.GENERATE_BLOCK_URI;
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

    private static final String API_END_POINT = "/healthIds";
    @Autowired
    protected WebApplicationContext webApplicationContext;

    @Autowired
    private Filter springSecurityFilterChain;

    @Autowired
    private HealthIdProperties healthIdProperties;


    @Before
    public void setUp() throws Exception {
        initMocks(this);
        setUpMockMvcBuilder();
    }

    @After
    public void tearDown() throws Exception {
        File file = new File("test-hid");
        if (file.exists()) {
            FileUtils.cleanDirectory(file);
            file.delete();
        }
    }

    @Test
    public void testGenerate() throws Exception {
        validAccessToken = "85HoExoxghh1pislg65hUM0q3wM9kfzcMdpYS0ixPD";
        validClientId = "18570";
        validEmail = "shrsystemadmin@test.com";

        mockMvc = MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
                .addFilters(springSecurityFilterChain)
                .build();

        givenThat(WireMock.get(urlEqualTo("/token/" + validAccessToken))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(asString("jsons/userDetails/userDetailForSHRSystemAdmin.json"))));


        mockMvc.perform(post(API_END_POINT + GENERATE_ALL_URI)
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
        validClientId = "18570";
        validEmail = "shrsystemadmin@test.com";

        mockMvc = MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
                .addFilters(springSecurityFilterChain)
                .build();

        givenThat(WireMock.get(urlEqualTo("/token/" + validAccessToken))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(asString("jsons/userDetails/userDetailForSHRSystemAdmin.json"))));


        mockMvc.perform(post(API_END_POINT + GENERATE_BLOCK_URI + "?start=9800000050&totalHIDs=1000")
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
        validClientId = "18570";
        validEmail = "shrsystemadmin@test.com";

        mockMvc = MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
                .addFilters(springSecurityFilterChain)
                .build();

        givenThat(WireMock.get(urlEqualTo("/token/" + validAccessToken))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(asString("jsons/userDetails/userDetailForSHRSystemAdmin.json"))));


        MvcResult mvcResult = mockMvc.perform(get(API_END_POINT + "/nextBlock/mci/MCI1")
                .accept(APPLICATION_JSON)
                .header(AUTH_TOKEN_KEY, validAccessToken)
                .header(FROM_KEY, validEmail)
                .header(CLIENT_ID_KEY, validClientId)
                .contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
        String contentAsString = mvcResult.getResponse().getContentAsString();

        final int healthIdBlockSize = healthIdProperties.getHealthIdBlockSize();
        Map response = new ObjectMapper().readValue(contentAsString, Map.class);
        assertTrue(healthIdBlockSize == ((List) response.get("hids")).size());
        assertTrue(healthIdBlockSize == (Integer) response.get("total"));
    }

    @Test
    public void testGetNextBlockWithBlockSizeDefined() throws Exception {
        validAccessToken = "85HoExoxghh1pislg65hUM0q3wM9kfzcMdpYS0ixPD";
        validClientId = "18570";
        validEmail = "shrsystemadmin@test.com";

        mockMvc = MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
                .addFilters(springSecurityFilterChain)
                .build();

        givenThat(WireMock.get(urlEqualTo("/token/" + validAccessToken))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(asString("jsons/userDetails/userDetailForSHRSystemAdmin.json"))));

        int blockSize = 7;
        MvcResult mvcResult = mockMvc.perform(get(API_END_POINT + "/nextBlock/mci/MCI1?blockSize=" + blockSize)
                .accept(APPLICATION_JSON)
                .header(AUTH_TOKEN_KEY, validAccessToken)
                .header(FROM_KEY, validEmail)
                .header(CLIENT_ID_KEY, validClientId)
                .contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
        String contentAsString = mvcResult.getResponse().getContentAsString();

        Map response = new ObjectMapper().readValue(contentAsString, Map.class);
        assertTrue(blockSize == ((List) response.get("hids")).size());
        assertTrue(blockSize == (Integer) response.get("total"));
    }

    @Test
    public void testGenerateOnlyForShrSystemAdmin() throws Exception {
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

        mockMvc.perform(post(API_END_POINT + GENERATE_ALL_URI)
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

        mockMvc.perform(get(API_END_POINT + "/nextBlock/mci/MCI1")
                .accept(APPLICATION_JSON)
                .header(AUTH_TOKEN_KEY, validAccessToken)
                .header(FROM_KEY, validEmail)
                .header(CLIENT_ID_KEY, validClientId)
                .contentType(APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andReturn();
    }

    @Test
    public void testGenerateRangeForOrg() throws Exception {
        validAccessToken = "85HoExoxghh1pislg65hUM0q3wM9kfzcMdpYS0ixPD";
        validClientId = "18570";
        validEmail = "shrsystemadmin@test.com";

        mockMvc = MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
                .addFilters(springSecurityFilterChain)
                .build();

        givenThat(WireMock.get(urlEqualTo("/api/1.0/facilities/1234.json"))
                .withHeader(CLIENT_ID_KEY, equalTo("18554"))
                .withHeader(AUTH_TOKEN_KEY, equalTo("b43d2b284fa678fb8248b7cc3ab391f9c21e5d7f8e88f815a9ef4346e426bd33"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(asString("jsons/facility.json"))));


        givenThat(WireMock.get(urlEqualTo("/token/" + validAccessToken))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(asString("jsons/userDetails/userDetailForSHRSystemAdmin.json"))));


        mockMvc.perform(post(API_END_POINT + GENERATE_BLOCK_FOR_ORG_URI + "?org=1234&start=9200100100&totalHIDs=1000")
                .accept(APPLICATION_JSON)
                .header(AUTH_TOKEN_KEY, validAccessToken)
                .header(FROM_KEY, validEmail)
                .header(CLIENT_ID_KEY, validClientId)
                .contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

    }
}