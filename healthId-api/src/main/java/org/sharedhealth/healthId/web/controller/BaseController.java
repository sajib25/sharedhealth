package org.sharedhealth.healthId.web.controller;

import org.sharedhealth.healthId.web.security.UserInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;

public class BaseController {
    private static final Logger logger = LoggerFactory.getLogger(BaseController.class);

    protected UserInfo getUserInfo() {
        return (UserInfo) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    protected void logAccessDetails(UserInfo userInfo, String action) {
        logger.info(String.format("ACCESS: USER=%s EMAIL=%s ACTION=%s",
                userInfo.getProperties().getId(), userInfo.getProperties().getEmail(), action));
    }
}
