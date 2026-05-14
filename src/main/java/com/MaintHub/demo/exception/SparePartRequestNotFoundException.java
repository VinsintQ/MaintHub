package com.MaintHub.demo.exception;

public class SparePartRequestNotFoundException extends RuntimeException {
    public SparePartRequestNotFoundException(String message) {
        super(message);
    }
}
