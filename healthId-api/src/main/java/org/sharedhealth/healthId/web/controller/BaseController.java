package org.sharedhealth.healthId.web.controller;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;
import org.sharedhealth.healthId.web.exception.Forbidden;
import org.sharedhealth.healthId.web.exception.HealthIdExhaustedException;
import org.sharedhealth.healthId.web.exception.HealthIdNotFoundException;
import org.sharedhealth.healthId.web.exception.InvalidRequestException;
import org.sharedhealth.healthId.web.security.UserInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.List;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

public class BaseController {
    private static final Logger logger = LoggerFactory.getLogger(BaseController.class);

    protected UserInfo getUserInfo() {
        return (UserInfo) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    protected void logAccessDetails(UserInfo userInfo, String action) {
        logger.info("ACCESS: EMAIL={} ACTION={}", userInfo.getProperties().getEmail(), action);
    }

    @ResponseStatus(value = INTERNAL_SERVER_ERROR)
    @ExceptionHandler(HealthIdExhaustedException.class)
    @ResponseBody
    public ErrorInfo handleException(HealthIdExhaustedException e) {
        logger.error("Handling generic exception. ", e);
        return new ErrorInfo(INTERNAL_SERVER_ERROR.value(), e.getMessage());
    }

    @ResponseStatus(value = HttpStatus.FORBIDDEN)
    @ResponseBody
    @ExceptionHandler(AccessDeniedException.class)
    public ErrorInfo accessDenied(AccessDeniedException accessDeniedException) {
        logger.error(accessDeniedException.getMessage());
        return new ErrorInfo(HttpStatus.FORBIDDEN.value(), accessDeniedException.getMessage());
    }

    @ResponseStatus(value = HttpStatus.FORBIDDEN)
    @ResponseBody
    @ExceptionHandler(Forbidden.class)
    public ErrorInfo forbidden(Forbidden forbidden) {
        logger.error(forbidden.getMessage());
        return new ErrorInfo(HttpStatus.FORBIDDEN.value(), forbidden.getMessage());
    }

    @ResponseStatus(value = HttpStatus.NOT_FOUND)
    @ResponseBody
    @ExceptionHandler(HealthIdNotFoundException.class)
    public ErrorInfo healthIdNotFound(HealthIdNotFoundException exception) {
        logger.error(exception.getMessage());
        return new ErrorInfo(HttpStatus.NOT_FOUND.value(), exception.getMessage());
    }

    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    @ResponseBody
    @ExceptionHandler(InvalidRequestException.class)
    public ErrorInfo invalidRequest(InvalidRequestException exception) {
        logger.error(exception.getMessage());
        return new ErrorInfo(HttpStatus.BAD_REQUEST.value(), exception.getMessage());
    }

    @ResponseStatus(value = INTERNAL_SERVER_ERROR)
    @ExceptionHandler(Exception.class)
    @ResponseBody
    public ErrorInfo handleException(Exception e) {
        logger.error(e.getMessage());
        return new ErrorInfo(INTERNAL_SERVER_ERROR.value(), e.getLocalizedMessage());
    }

    @JsonRootName(value = "error")
    public static class ErrorInfo implements Comparable<ErrorInfo> {


        @JsonProperty
        private String message;

        @JsonProperty
        private int code;


        @JsonProperty
        @JsonInclude(NON_EMPTY)
        private List<ErrorInfo> errors;

        public ErrorInfo() {
        }

        public ErrorInfo(int code, String message) {
            this.code = code;
            this.message = message;
        }

        public List<ErrorInfo> getErrors() {
            return this.errors;
        }

        public int getCode() {
            return this.code;
        }

        public String getMessage() {
            return this.message;
        }

        @Override
        public int compareTo(ErrorInfo e) {
            if (this.code < e.getCode()) return this.code > e.getCode() ? 1 : -1;
            else return this.code > e.getCode() ? 1 : 0;
        }
    }
}
