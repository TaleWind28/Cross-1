package com.unipi.lab3.cross.model.user;

import java.util.concurrent.ConcurrentHashMap;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class UserManager {

    // user map
    private ConcurrentHashMap<String, User> users;

    public UserManager () {
        this.users = new ConcurrentHashMap<>();
    }

    public UserManager (ConcurrentHashMap<String, User> users) {
        this.users = users;
    }

    public ConcurrentHashMap<String, User> getUsers () {
        return this.users;
    }

    public User getUser (String username) {
        return this.users.get(username);
    }

    // register
    public synchronized int register (String username, String password) {

        // invalid password
        if (!isValid(password, 8, 20))
            return 101;

        // invalid username
        if (!isValid(username, 3, 12))
            return 103;
        
        String hashedPassword = hashPassword(password);
        User user = new User(username, hashedPassword, false);

        // add user if not already exists
        if (this.users.putIfAbsent(username, user) != null)
            return 102; // username not available

        return 100;
    }

    public synchronized int updateCredentials (String username, String newPwd, String oldPwd) {

        User user = this.users.get(username);

        // user not found
        if (user == null)
            return 105;

        String currentPwd = user.getPassword();

        // old password mismatch
        if (!hashPassword(oldPwd).equals(currentPwd))
            return 102;

        // new password equal to old one
        if (newPwd.equals(oldPwd))
            return 103;

        // invalid new password
        if (!isValid(newPwd, 8, 20))
            return 101;

        String newHashedPwd = hashPassword(newPwd);
        
        User newUser = new User(username, newHashedPwd, false);
        
        if (!this.users.replace(username, user, newUser))
            return 105;

        return 100;
    }

    public synchronized int login (String username, String password) {

        // invalid password ??
        if (!isValid(password, 8, 20))
            return 103;

        User user = this.users.get(username);

        if (user.getLogged())
            return 102;
        
        String currentPwd = user.getPassword();

        // password mismatch
        if (!hashPassword(password).equals(currentPwd))
            return 101;

        // set logged state
        user.setLogged(true);

        return 100;
    }

    public synchronized int logout (String username) {

        User user = this.users.get(username);

        // unset logged state
        user.setLogged(false);

        return 100;
    }  
    
    // password hashed
    public String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");

            byte[] digest = md.digest(password.getBytes(StandardCharsets.UTF_8));

            StringBuilder sb = new StringBuilder(digest.length * 2);

            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }

            return sb.toString();
        } 
        catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    private boolean isValid (String str, int minLen, int maxLen) {
        if (str == null || str.isEmpty())
            return false;

        if (str.length() < minLen || str.length() > maxLen)
            return false;

        for (char c : str.toCharArray()) {
            if (!Character.isLetterOrDigit(c))
                return false;
        }

        return true;
    }
}
