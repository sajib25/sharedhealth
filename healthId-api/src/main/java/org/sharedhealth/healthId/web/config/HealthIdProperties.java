package org.sharedhealth.healthId.web.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.stereotype.Component;

@Component
public class HealthIdProperties {

    public static final String DIAGNOSTICS_HEALTH_SERVLET_PATH = "/diagnostics/health";
    public static final String REMAINING_HEALTH_ID_SERVLET_PATH = "/healthIds/checkRemaining";

    @Bean
    public static PropertySourcesPlaceholderConfigurer propertyPlaceholderConfigurer() {
        return new PropertySourcesPlaceholderConfigurer();
    }
    
    @Value("${CASSANDRA_KEYSPACE}")
    private String cassandraKeySpace;
    @Value("${CASSANDRA_HOST}")
    private String cassandraHost;
    @Value("${CASSANDRA_PORT}")
    private String cassandraPort;
    @Value("${CASSANDRA_USER}")
    private String cassandraUser;
    @Value("${CASSANDRA_PASSWORD}")
    private String cassandraPassword;
    @Value("${CASSANDRA_TIMEOUT}")
    private String cassandraTimeout;
    @Value("${REST_POOL_SIZE}")
    private String restPoolSize;

    @Value("${IDENTITY_SERVER_BASE_URL}")
    private String identityServerBaseUrl;
    @Value("${IDP_CLIENT_ID}")
    private String idpClientId;
    @Value("${IDP_AUTH_TOKEN}")
    private String idpAuthToken;

    @Value("${FR_URL}")
    private String frUrl;

    @Value("${HID_STORAGE_PATH}")
    private String hidStoragePath;

    @Value("${OTHER_ORG_INVALID_HID_PATTERN}")
    private String otherOrgInvalidHidPattern;
    @Value("${OTHER_ORG_START_HID}")
    private String otherOrgStartHid;
    @Value("${OTHER_ORG_END_HID}")
    private String otherOrgEndHid;


    @Value("${MCI_START_HID}")
    private String mciStartHid;
    @Value("${MCI_END_HID}")
    private String mciEndHid;
    @Value("${MCI_ORG_CODE}")
    private String mciOrgCode;
    @Value("${MCI_INVALID_HID_PATTERN}")
    private String mciInvalidHidPattern;

    @Value("${HEALTH_ID_WARNING_THRESHOLD}")
    private String healthIdWarningThreshold;

    @Value("${HEALTH_ID_BLOCK_SIZE}")
    private String healthIdBlockSize;

    public String getCassandraKeySpace() {
        return cassandraKeySpace;
    }

    public String getContactPoints() {
        return cassandraHost;
    }

    public int getCassandraPort() {
        return Integer.parseInt(cassandraPort);
    }

    public int getCassandraTimeout() {
        return Integer.parseInt(cassandraTimeout);
    }

    public int getRestPoolSize() {
        return Integer.parseInt(restPoolSize);
    }

    public String getIdentityServerBaseUrl() {
        return identityServerBaseUrl;
    }
    public String getIdpClientId() {
        return idpClientId;
    }

    public String getIdpAuthToken() {
        return idpAuthToken;
    }
    public String getCassandraUser() {
        return cassandraUser;
    }

    public String getCassandraPassword() {
        return cassandraPassword;
    }

    public String getMciInvalidHidPattern() {
        return mciInvalidHidPattern;
    }

    public Long getMciEndHid() {
        return Long.valueOf(mciEndHid);
    }

    public Long getMciStartHid() {
        return Long.valueOf(mciStartHid);
    }

    public void setMciInvalidHidPattern(String mciInvalidHidPattern) {
        this.mciInvalidHidPattern = mciInvalidHidPattern;
    }

    public void setMciStartHid(String mciStartHid) {
        this.mciStartHid = mciStartHid;
    }

    public void setMciEndHid(String mciEndHid) {
        this.mciEndHid = mciEndHid;
    }

    public int getHealthIdBlockSize() {
        return Integer.parseInt(healthIdBlockSize);
    }

    public void setHealthIdBlockSize(String healthIdBlockSize) {
        this.healthIdBlockSize = healthIdBlockSize;
    }

    
    public String getHidStoragePath() {
        return hidStoragePath;
    }

    public void setHidStoragePath(String hidStoragePath) {
        this.hidStoragePath = hidStoragePath;
    }

    public String getOtherOrgInvalidHidPattern() {
        return otherOrgInvalidHidPattern;
    }

    public void setOtherOrgInvalidHidPattern(String otherOrgInvalidHidPattern) {
        this.otherOrgInvalidHidPattern = otherOrgInvalidHidPattern;
    }

    public Long getOtherOrgStartHid() {
        return Long.valueOf(otherOrgStartHid);
    }

    public void setOtherOrgStartHid(String otherOrgStartHid) {
        this.otherOrgStartHid = otherOrgStartHid;
    }

    public Long getOtherOrgEndHid() {
        return Long.valueOf(otherOrgEndHid);
    }

    public void setOtherOrgEndHid(String otherOrgEndHid) {
        this.otherOrgEndHid = otherOrgEndHid;
    }

    public String getMciOrgCode() {
        return mciOrgCode;
    }

    public void setMciOrgCode(String mciOrgCode) {
        this.mciOrgCode = mciOrgCode;
    }


    public String getFrUrl() {
        return frUrl;
    }

    public void setFrUrl(String frUrl) {
        this.frUrl = frUrl;
    }

    public int getHealthIdWarningThreshold() {
        return Integer.parseInt((healthIdWarningThreshold));
    }
}
