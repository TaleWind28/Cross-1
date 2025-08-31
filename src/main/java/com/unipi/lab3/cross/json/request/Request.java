package com.unipi.lab3.cross.json.request;

public class Request <T extends Values> {
    private String operation;
    private T values;

    public Request (String operation, T values) {
        this.operation = operation;
        this.values = values;
    }

    public String getOperation () {
        return this.operation;
    }

    public T getValues () {
        return this.values;
    }

    public void setOperation (String operation) {
        this.operation = operation;
    }
}