package com.unipi.lab3.cross.json.response;

public class UserResponse extends Response {
    private String operation;
    private int response;
    private String errorMessage;

    public UserResponse(String operation, int response, String errorMessage) {
        this.operation = operation;
        this.response = response;
        this.errorMessage = errorMessage;
    }

    public String getOperation() {
        return operation;
    }

    public int getResponse() {
        return response;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}
