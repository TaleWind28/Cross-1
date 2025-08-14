package com.unipi.lab3.cross.model;

/**
 * class representing a user in the system
 * contains username, password and login status
*/

public class User {
    private String username;
    private String password;
    private boolean isLogged;

    public User (String username, String password, boolean isLogged) {
        this.username = username;
        this.password = password;
        this.isLogged = isLogged;
    }

    public String getUsername () {
        return this.username;
    }

    public String getPassword () {
        return this.password;
    }

    public boolean getLogged () {
        return this.isLogged;
    }

    public void setPassword (String password) {
        this.password = password;
    }

    public void setLogged (boolean isLogged) {
        this.isLogged = isLogged;
    }
    
}
