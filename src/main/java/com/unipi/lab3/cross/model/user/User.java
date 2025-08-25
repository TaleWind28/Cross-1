package com.unipi.lab3.cross.model.user;

/**
 * class representing a user in the system
 * contains username, password and login status
*/

public class User {
    private String username;
    private String passwordHash;
    private boolean isLogged;

    public User (String username, String passwordHash, boolean isLogged) {
        this.username = username;
        this.passwordHash = passwordHash;
        this.isLogged = isLogged;
    }

    public String getUsername () {
        return this.username;
    }

    public String getPassword () {
        return this.passwordHash;
    }

    public boolean getLogged () {
        return this.isLogged;
    }

    public void setPassword (String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public void setLogged (boolean isLogged) {
        this.isLogged = isLogged;
    }
    
}
