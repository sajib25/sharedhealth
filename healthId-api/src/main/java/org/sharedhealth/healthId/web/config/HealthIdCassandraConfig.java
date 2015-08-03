package org.sharedhealth.healthId.web.config;

import com.datastax.driver.core.AuthProvider;
import com.datastax.driver.core.PlainTextAuthProvider;
import com.datastax.driver.core.SocketOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.cassandra.config.java.AbstractCassandraConfiguration;
import org.springframework.data.cassandra.core.CassandraOperations;
import org.springframework.data.cassandra.core.CassandraTemplate;
import org.springframework.data.cassandra.repository.config.EnableCassandraRepositories;

@Configuration
@EnableCassandraRepositories(basePackages = "org.sharedhealth.healthId")
public class HealthIdCassandraConfig extends AbstractCassandraConfiguration {

    @Autowired
    private HealthIdProperties healthIdProperties;

    @Override
    protected String getKeyspaceName() {
        return healthIdProperties.getCassandraKeySpace();
    }

    @Override
    protected String getContactPoints() {
        return healthIdProperties.getContactPoints();
    }

    @Override
    protected int getPort() {
        return healthIdProperties.getCassandraPort();
    }

    @Override
    protected AuthProvider getAuthProvider() {
        return new PlainTextAuthProvider(healthIdProperties.getCassandraUser(), healthIdProperties.getCassandraPassword());
    }

    @Override
    protected SocketOptions getSocketOptions() {
        SocketOptions socketOptions = new SocketOptions();
        socketOptions.setConnectTimeoutMillis(healthIdProperties.getCassandraTimeout());
        socketOptions.setReadTimeoutMillis(healthIdProperties.getCassandraTimeout());
        return socketOptions;
    }

    @Bean(name = "HealthIdCassandraTemplate")
    public CassandraOperations CassandraTemplate() throws Exception {
        return new CassandraTemplate(session().getObject());
    }

}
