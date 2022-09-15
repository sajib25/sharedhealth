package org.sharedhealth.healthId.web.launch;

import org.sharedhealth.healthId.web.config.ActuatorConfig;
import org.sharedhealth.healthId.web.config.HealthIdConfig;
//import org.sharedhealth.healthId.web.config.HealthIdSecurityConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.http.converter.xml.Jaxb2RootElementHttpMessageConverter;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import org.springframework.web.servlet.config.annotation.ContentNegotiationConfigurer;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

@Configuration
@Import({HealthIdConfig.class,/* HealthIdSecurityConfig.class,*/ ActuatorConfig.class})
@EnableWebMvc
@EnableScheduling
@ComponentScan(basePackages = {
        "org.sharedhealth.healthId.web.config",
        "org.sharedhealth.healthId.web.controller",
        "org.sharedhealth.healthId.web.exception",
        "org.sharedhealth.healthId.web.launch",
        "org.sharedhealth.healthId.web.model",
        "org.sharedhealth.healthId.web.repository",
        "org.sharedhealth.healthId.web.security",
        "org.sharedhealth.healthId.web.service",
        "org.sharedhealth.healthId.web.client",
        "org.sharedhealth.healthId.web.utils"
})
public class WebMvcConfig extends WebMvcConfigurerAdapter implements SchedulingConfigurer {

    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        converters.add(new MappingJackson2HttpMessageConverter());
        converters.add(new Jaxb2RootElementHttpMessageConverter());
    }

    @Override
    public void configureContentNegotiation(ContentNegotiationConfigurer contentNegotiator) {
        super.configureContentNegotiation(contentNegotiator);
        contentNegotiator.mediaType("application", MediaType.APPLICATION_ATOM_XML);
    }

    @Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
        taskRegistrar.setScheduler(taskExecutor());
    }

    @Bean(destroyMethod = "shutdown")
    public Executor taskExecutor() {
        return Executors.newScheduledThreadPool(10);
    }

}
