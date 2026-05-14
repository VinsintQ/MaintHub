package com.MaintHub.demo.exception;

public class EquipmentCategoryNotFoundException extends RuntimeException {
    public EquipmentCategoryNotFoundException(String message) {
        super(message);
    }
}
