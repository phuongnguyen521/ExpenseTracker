package com.phuong.exception;

public class ExternalServiceException extends RuntimeException{
    private final String serviceName;

    public ExternalServiceException(String message, String serviceName) {
        super(String.format("External service %s error: %s", serviceName, message));
        this.serviceName = serviceName;
    }

    public ExternalServiceException(String serviceName, String message, Throwable cause) {
        super(String.format("External service %s error: %s", serviceName, message), cause);
        this.serviceName = serviceName;
    }

    public String getServiceName() {
        return serviceName;
    }
}
