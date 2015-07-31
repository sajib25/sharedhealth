package org.sharedhealth.healthId.web.exception;

public class HealthIdExhaustedException extends RuntimeException {
    public HealthIdExhaustedException() {
        super("HealthIds are exhausted. Please generate new HealthIds.");
    }
}
