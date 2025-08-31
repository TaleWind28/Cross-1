package com.unipi.lab3.cross.json.request;

public class NetworkValues implements Values {
    private int port;

    public NetworkValues(int port) {
        this.port = port;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String toString() {
        return "{port: " + this.port + "}";
    }
}
