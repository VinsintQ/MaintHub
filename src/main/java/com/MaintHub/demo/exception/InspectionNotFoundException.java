package com.MaintHub.demo.exception;

public class InspectionNotFoundException extends RuntimeException {
    public InspectionNotFoundException(String message) {
        super(message);
    }
}
