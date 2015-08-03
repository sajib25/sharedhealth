package org.sharedhealth.healthId.web.launch;

import org.sharedhealth.healthId.web.config.HealthIdConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.embedded.EmbeddedServletContainerFactory;
import org.springframework.boot.context.embedded.ServletContextInitializer;
import org.springframework.boot.context.embedded.tomcat.TomcatEmbeddedServletContainerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.web.servlet.DispatcherServlet;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRegistration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static java.lang.Integer.valueOf;
import static java.lang.String.format;
import static java.lang.System.getenv;


@Import(WebMvcConfig.class)
public class Main {

    public static final String HEALTH_ID_SERVICE_PORT = "HEALTH_ID_SERVICE_PORT";
    private static final String PROFILE_DEFAULT = "default";

    @Bean
    public EmbeddedServletContainerFactory getFactory() {
        final Map<String, String> env = getenv();
        TomcatEmbeddedServletContainerFactory factory = new TomcatEmbeddedServletContainerFactory();
        factory.addInitializers(new ServletContextInitializer() {
            @Override
            public void onStartup(ServletContext servletContext) throws ServletException {

                ServletRegistration.Dynamic healthId = servletContext.addServlet("healthId", DispatcherServlet.class);

                healthId.addMapping("/*");
                healthId.setInitParameter("contextClass", "org.springframework.web.context.support" +
                        ".AnnotationConfigWebApplicationContext");
                healthId.setAsyncSupported(true);

            }
        });

        factory.setPort(valueOf(env.get(HEALTH_ID_SERVICE_PORT)));
        return factory;
    }

    public static void main(String[] args) throws Exception {
        SpringApplication.run(Main.class, args);
    }

}
