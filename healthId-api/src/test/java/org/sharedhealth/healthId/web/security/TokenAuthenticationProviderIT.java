package org.sharedhealth.healthId.web.security;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import net.sf.ehcache.CacheManager;
import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.sharedhealth.healthId.web.config.EnvironmentMock;
import org.sharedhealth.healthId.web.launch.WebMvcConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.Assert.assertNotNull;
import static org.sharedhealth.healthId.web.utils.FileUtil.asString;

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration(initializers = EnvironmentMock.class, classes = WebMvcConfig.class)
public class TokenAuthenticationProviderIT {
    @Autowired
    private IdentityServiceClient identityServiceClient;

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(9997);

    @After
    public void tearDown() throws Exception {
        CacheManager.getInstance().clearAll();
    }

    @Test
    public void shouldCacheTheResultFromIdentityServer() throws Exception {
        String token = "85HoExoxghh1pislg65hUM0q3wM9kfzcMdpYS0ixPD";
        UserAuthInfo userAuthInfo = new UserAuthInfo("18570", "shrsystemadmin@test.com", token);

        givenThat(WireMock.get(urlEqualTo("/token/" + token))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(asString("jsons/userDetails/userDetailForSHRSystemAdmin.json"))));

        TokenAuthenticationProvider provider = new TokenAuthenticationProvider(identityServiceClient);
        assertNotNull(provider.authenticate(new PreAuthenticatedAuthenticationToken(userAuthInfo, token)));
        assertNotNull(provider.authenticate(new PreAuthenticatedAuthenticationToken(userAuthInfo, token)));

        verify(1, getRequestedFor(urlEqualTo("/token/" + token)));
    }

    @Test(expected = BadCredentialsException.class)
    public void shouldNotCacheIfUnauthorized() throws Exception {
        String token = "85HoExoxghh1pislg65hUM0q3wM9kfzcMdpYS0ixPD";
        UserAuthInfo userAuthInfo = new UserAuthInfo("1", "email.com", token);

        givenThat(WireMock.get(urlEqualTo("/token/" + token))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(asString("jsons/userDetails/userDetailForSHRSystemAdmin.json"))));

        TokenAuthenticationProvider provider = new TokenAuthenticationProvider(identityServiceClient);
        assertNotNull(provider.authenticate(new PreAuthenticatedAuthenticationToken(userAuthInfo, token)));
        assertNotNull(provider.authenticate(new PreAuthenticatedAuthenticationToken(userAuthInfo, token)));

        verify(2, getRequestedFor(urlEqualTo("/token/" + token)));

    }
}