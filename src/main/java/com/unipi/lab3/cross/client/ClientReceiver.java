package com.unipi.lab3.cross.client;

import java.io.*;
import java.net.*;
import java.util.concurrent.atomic.AtomicBoolean;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import com.unipi.lab3.cross.json.response.*;

public class ClientReceiver implements Runnable {

    private BufferedReader in;

    private volatile boolean running;

    private final AtomicBoolean logged;

    public ClientReceiver(BufferedReader in, AtomicBoolean logged) {
        this.in = in;
        this.logged = logged;
    }

    public void run() {
        running = true;

        try {
            String responseMsg;

            while (running && ((responseMsg = in.readLine()) != null)) {
                
                JsonObject obj = JsonParser.parseString(responseMsg).getAsJsonObject();

                if (obj.has("orderID")) {
                    // order response
                    Gson gson = new Gson();

                    OrderResponse orderResponse = gson.fromJson(responseMsg, OrderResponse.class);

                    handleResponse(orderResponse);
                }
                else if (obj.has("operation") && obj.has("response") && obj.has("errorMessage")) {
                    // user response
                    Gson gson = new Gson();

                    UserResponse userResponse = gson.fromJson(responseMsg, UserResponse.class);

                    handleResponse(userResponse);
                }
                else {  
                    // unknown response
                    System.out.println("unknown response from server" + responseMsg);
                }
            }
        }
        catch (SocketException e) {

        }
        catch (IOException e) {
            System.err.println(e.getMessage());
        }
        finally {
            running = false;
            try {
                in.close();
            }
            catch (IOException e) {
                System.err.println(e.getMessage());
            }
        }
    }

    public void handleResponse (Response responseMsg) {

        if (responseMsg instanceof OrderResponse) {
            OrderResponse orderResponse = (OrderResponse)responseMsg;

            if (orderResponse.getOrderID() != -1) {
                System.out.println("order with ID: " + orderResponse.getOrderID());
            }
            else {
                System.out.println("order failed");
            }
        }
        else if (responseMsg instanceof UserResponse) {
            UserResponse userResponse = (UserResponse)responseMsg;

            String op = userResponse.getOperation();

            switch (op) {
                case "register":
                    if (userResponse.getResponse() == 100) {
                        System.out.println("registration successful");
                    }
                    else {
                        System.out.println(userResponse.getErrorMessage());
                    }
                break;

                case "updateCredentials":
                    if (userResponse.getResponse() == 100) {
                        System.out.println("update successful");
                    }
                    else {
                        System.out.println(userResponse.getErrorMessage());
                    }
                break;

                case "login":
                    if (userResponse.getResponse() == 100) {
                        logged.set(true);
                        System.out.println("login successful");
                    }
                    else {
                        System.out.println(userResponse.getErrorMessage());
                        logged.set(false);
                    }
                break;

                case "logout":
                    if (userResponse.getResponse() == 100) {
                        logged.set(false);
                        System.out.println("logout successful");
                    }
                    else {
                        System.out.println(userResponse.getErrorMessage());
                    }

                break;

                case "cancelOrder":
                    if (userResponse.getResponse() == 100) {
                        System.out.println("order successfully deleted");
                    }
                    else {
                        System.out.println(userResponse.getErrorMessage());
                    }
                break;

                case "insertLimitOrder":
                case "insertMarketOrder":
                case "insertStopOrder":
                    if (userResponse.getResponse() != 100) {
                        System.out.println(userResponse.getErrorMessage());
                    }
                break;

                case "getOrderBook":
                    // print order book and stop orders

                break;

                case "getPriceHistory":
                    // print price history

                break;

                default:
                    System.out.println(userResponse.getErrorMessage());
                break;
            }
        }
    
    }
    
}