package com.unipi.lab3.cross.json.request;

public class UserValues implements Values {
    private String username;
    private String password;
    private String newPassword;
    private NetworkValues networkValues;

    public UserValues (String username) {
        this.username = username;
    }

    public UserValues (String username, String password) {
        this.username = username;
        this.password = password;
    }
 
    public UserValues (String username, String password, NetworkValues netVal) {
        this.username = username;
        this.password = password;
        this.networkValues = netVal;
    }

    public UserValues (String username, String password, String newPassword) {
        this.username = username;
        this.password = password;
        this.newPassword = newPassword;
    }

    public String getUsername () {
        return username;
    }

    public String getPassword () {
        return password;
    }

    public String getNewPassword () {
        return newPassword;
    }

    public void setUsername (String username) {
        this.username = username;
    }

    public void setPassword (String password) {
        this.password = password;
    }

    public void setNewPassword (String new_password) {
        this.newPassword = new_password;
    }

    public String credentials () {
        return "{username: " + this.username + ", password: " + this.password + "}";
    }

    public String toString () {
        return "{username: " + this.username + ", old_password: " + this.password + ", new_password: " + this.newPassword + "}";
    }
}
