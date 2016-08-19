package org.sharedhealth.healthId.web.exception;

public class HealthIdConflictException extends RuntimeException {
    private String errorMessage;


    public HealthIdConflictException(String message) {
        super();
        this.errorMessage = message;
    }

    public HealthIdConflictException(String message, Throwable cause) {
        super(message, cause);
    }

    @Override
    public String getMessage() {
        return errorMessage;
    }
}
