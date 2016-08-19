package org.sharedhealth.healthId.web.exception;

public class HealthIdNotFoundException extends RuntimeException {
    private String errorMessage;


    public HealthIdNotFoundException(String message) {
        super();
        this.errorMessage = message;
    }

    public HealthIdNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    @Override
    public String getMessage() {
        return errorMessage;
    }
}
