package com.MaintHub.demo.exception;

public class InvalidWorkflowActionException extends RuntimeException {
    public InvalidWorkflowActionException(String message) {
        super(message);
    }
}
