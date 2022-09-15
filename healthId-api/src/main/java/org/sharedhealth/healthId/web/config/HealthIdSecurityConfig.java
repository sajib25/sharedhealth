package org.sharedhealth.healthId.web.config;

import org.sharedhealth.healthId.web.security.TokenAuthenticationFilter;
import org.sharedhealth.healthId.web.security.TokenAuthenticationProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.logout.LogoutFilter;
import org.springframework.security.web.util.matcher.AndRequestMatcher;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.NegatedRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;

//@Configuration
//@EnableWebSecurity
//@EnableGlobalMethodSecurity(prePostEnabled = true)
//public class HealthIdSecurityConfig extends WebSecurityConfigurerAdapter {
    /*private static final Logger logger = LoggerFactory.getLogger(HealthIdSecurityConfig.class);

    @Autowired
    TokenAuthenticationProvider tokenAuthenticationProvider;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        *//*http
                .csrf().disable()
                .anonymous().disable()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);

        http
                .requestMatcher(new AndRequestMatcher(new ArrayList<RequestMatcher>() {
                    {
                        add(new NegatedRequestMatcher(new AntPathRequestMatcher(HealthIdProperties.DIAGNOSTICS_HEALTH_SERVLET_PATH)));
                        add(new NegatedRequestMatcher(new AntPathRequestMatcher(HealthIdProperties.REMAINING_HEALTH_ID_SERVLET_PATH)));
                        add(new AntPathRequestMatcher("/**"));
                    }
                }))
                .authorizeRequests()
                .anyRequest()
                .authenticated()
                .and()
                .addFilterBefore(new TokenAuthenticationFilter(authenticationManager()), LogoutFilter.class)
                .exceptionHandling().accessDeniedHandler(unauthorizedEntryPoint()).authenticationEntryPoint(unauthenticatedEntryPoint());*//*

        http.authorizeRequests().anyRequest().permitAll();
    }

    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    @Override
    @Autowired
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.authenticationProvider(tokenAuthenticationProvider);
    }

    private AuthenticationEntryPoint unauthenticatedEntryPoint() {
        return new AuthenticationEntryPoint() {
            @Override
            public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException
                    authException) throws IOException, ServletException {
                logger.debug(authException.getMessage());
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, authException.getMessage());
            }
        };
    }

    private AccessDeniedHandler unauthorizedEntryPoint() {
        return new AccessDeniedHandler() {
            @Override
            public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException, ServletException {
                logger.debug(accessDeniedException.getMessage());
                response.sendError(HttpServletResponse.SC_FORBIDDEN, accessDeniedException.getMessage());
            }
        };
    }
}

*/