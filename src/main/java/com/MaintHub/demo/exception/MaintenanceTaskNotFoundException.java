package com.MaintHub.demo.exception;

public class MaintenanceTaskNotFoundException extends RuntimeException {
    public MaintenanceTaskNotFoundException(String message) {
        super(message);
    }
}
