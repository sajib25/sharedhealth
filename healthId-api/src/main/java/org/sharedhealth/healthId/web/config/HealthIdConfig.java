package org.sharedhealth.healthId.web.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.client.AsyncRestTemplate;

@Configuration
@Import({HealthIdCassandraConfig.class,
})
public class HealthIdConfig {
    public static final String DIAGNOSTICS_SERVLET_PATH = "/diagnostics/health";

    @Autowired
    private HealthIdProperties healthIdProperties;


    @Bean(name = "HealthIdRestTemplate")
    public AsyncRestTemplate restTemplate() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.initialize();
        executor.setCorePoolSize(healthIdProperties.getRestPoolSize());
        return new AsyncRestTemplate(executor);
    }


}
