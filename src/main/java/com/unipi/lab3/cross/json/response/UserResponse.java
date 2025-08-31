package com.unipi.lab3.cross.json.response;

public class UserResponse extends Response {
    private int response;
    private String errorMessage;

    public UserResponse(int response, String errorMessage) {
        this.response = response;
        this.errorMessage = errorMessage;
    }

    public int getResponse() {
        return response;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}
