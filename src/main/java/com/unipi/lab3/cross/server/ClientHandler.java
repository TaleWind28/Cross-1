package com.unipi.lab3.cross.server;

import java.io.*;
import java.net.*;
import java.time.Year;
import java.time.YearMonth;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

import com.unipi.lab3.cross.model.*;
import com.unipi.lab3.cross.model.user.*;
import com.unipi.lab3.cross.model.orders.*;
import com.unipi.lab3.cross.model.trade.*;
import com.unipi.lab3.cross.server.*;
import com.unipi.lab3.cross.client.*;
import com.unipi.lab3.cross.json.request.*;
import com.unipi.lab3.cross.json.response.*;

public class ClientHandler implements Runnable {

    private Socket clientSocket;

    private OrderBook orderBook; // shared
    private UserManager userManager; // shared
    private TradeMap tradeMap; // shared

    private PriceHistory priceHistory;

    private UdpNotifier udpNotifier; // shared
    private int udpPort;

    private User user;

    private volatile long lastActivityTime;

    private volatile boolean running;

    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public ClientHandler(Socket clientSocket, UserManager userManager, OrderBook orderBook, TradeMap tradeMap, UdpNotifier udpNotifier) {
        this.clientSocket = clientSocket;
        this.userManager = userManager;
        this.orderBook = orderBook;
        this.tradeMap = tradeMap;
        this.udpNotifier = udpNotifier;
        this.priceHistory = new PriceHistory();
        this.user = null; // initially not authenticated
    }

    @Override
    public void run() {

        running = true;

        try (
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)) {

                String receivedMsg;
                Response response;

                // listening for client messages
                while (running && ((receivedMsg = in.readLine()) != null)) {
                    try {
                        updateLastActivityTime();

                        // function to handle messages
                        response = handleRequest(receivedMsg);

                        // convert response to json

                        if (response != null) {
                            
                            String jsonString = gson.toJson(response);

                            // send response to client
                            out.println(jsonString);
                        }  
                    }
                    catch (JsonSyntaxException e) {
                        System.err.println(e.getMessage());
                        out.println(new Gson().toJson(new UserResponse("error", -1, "json error")));
                    }
                    catch (Exception e) {
                        System.err.println(e.getMessage());
                        out.println(new Gson().toJson(new UserResponse("error", -1, "server error")));
                    }

                }
        } 
        catch (IOException e) {
            System.err.println("socket error: " + e.getMessage());
        }
        finally {
            running = false;

            // log out user
            if (this.user != null && this.user.getLogged()) {
                userManager.logout(this.user.getUsername());
                udpNotifier.removeClient(this.user.getUsername());

                this.user = null;

                System.out.println("user logged out due to disconnection");
            }

            try {
                clientSocket.close();
            } catch (IOException e) {
                System.err.println("socket error" + e.getMessage());
            }

        }
        
    }

    // process request with json
    public Response handleRequest (String request) {

        Gson gson = new Gson();

        JsonObject obj = JsonParser.parseString(request).getAsJsonObject();

        String op = obj.get("operation").getAsString();

        Response response = null;
        int code = -1;
        String msg = "";

        switch (op) {
            case "register":
                UserValues userVal = gson.fromJson(obj.get("values"), UserValues.class);

                if (this.user != null && this.user.getLogged())
                    return new UserResponse("register", 103, "user already logged in");

                code = userManager.register(userVal.getUsername(), userVal.getPassword());

                if (code == 100) {
                    msg = "OK";

                    System.out.println("user " + userVal.getUsername() + " registered");
                }
                else if (code == 101)
                    msg = "invalid password";
                else if (code == 102)
                    msg = "username not available";
                else if (code == 103)
                    msg = "invalid username";

                response = new UserResponse("register", code, msg);

            break;

            case "updateCredentials":
                userVal = gson.fromJson(obj.get("values"), UserValues.class);

                if (this.user.getLogged())
                    return new UserResponse("updateCredentials", 104, "user currently logged");

                if (userManager.getUser(userVal.getUsername()) == null)
                    return new UserResponse("updateCredentials", 102, "non existent username");

                code = userManager.updateCredentials(userVal.getUsername(), userVal.getNewPassword(), userVal.getPassword());

                if (code == 100) {
                    msg = "OK";

                    System.out.println("user " + userVal.getUsername() + " updated credentials");
                }
                else if (code == 101)
                    msg = "invalid new password";
                else if (code == 102)
                    msg = "old password mismatch";
                else if (code == 103)
                    msg = "new passord equal to old one";

                response = new UserResponse("updateCredentials", code, msg);

            break;

            case "login":
                userVal = gson.fromJson(obj.get("values"), UserValues.class);

                if (this.user.getLogged())
                    return new UserResponse("login", 102, "user already logged in");

                if (userManager.getUser(userVal.getUsername()) == null)
                    return new UserResponse("login", 101, "non existent username");
                
                if (userVal.getNetworkValues() == null || userVal.getNetworkValues().getPort() <= 1024)
                    return new UserResponse("login", 104, "invalid network values");

                code = userManager.login(userVal.getUsername(), userVal.getPassword());

                if (code == 100) {
                    msg = "OK";

                    // register client for udp notifications
                    udpPort = userVal.getNetworkValues().getPort();
                    udpNotifier.registerClient(userVal.getUsername(), clientSocket.getInetAddress(), udpPort);

                    // set client handler user
                    this.user = userManager.getUser(userVal.getUsername());
                    this.user.setLogged(true);

                    updateLastActivityTime();

                    System.out.println("user " + this.user.getUsername() + " logged in");
                }  
                else if (code == 101)
                    msg = "password mismatch";
                else if (code == 102)
                    msg = "user already logged in";
                else if (code == 103)
                    msg = "invalid password";

                response = new UserResponse("login", code, msg);

            break;

            case "logout":
                if (this.user == null)
                    return new UserResponse("logout", 101, "user error");

                if (this.user.getLogged() == false)
                    return new UserResponse("logout", 101, "user not logged in");

                userVal = gson.fromJson(obj.get("values"), UserValues.class);

                if (userManager.getUser(userVal.getUsername()) == null)
                    return new UserResponse("logout", 101, "user not found");

                code = userManager.logout(this.user.getUsername());

                if (code == 100) {
                    msg = "OK";

                    // unregister client from udp notifications
                    udpNotifier.removeClient(this.user.getUsername());
                    this.user.setLogged(false);
                    this.user = null;

                    System.out.println("user logged out");
                }

                response = new UserResponse("logout", code, msg);
            
            break;

            case "insertLimitOrder":
                if (this.user == null)
                    return new UserResponse("insertLimitOrder", 101, "user error");

                if (this.user.getLogged() == false)
                    return new UserResponse("insertLimitOrder", 102, "you can't insert orders if not logged in");
                
                OrderValues orderVal = gson.fromJson(obj.get("values"), OrderValues.class);

                code = orderBook.execLimitOrder(this.user.getUsername(), orderVal.getType(), orderVal.getSize(), orderVal.getPrice());

                if (code == -1) {
                    System.out.println("error with limit order inserted by user " + this.user.getUsername());
                }

                response = new OrderResponse(code);
            break;

            case "insertMarketOrder":
                if (this.user == null)
                    return new UserResponse("insertMarketOrder", 101, "user error");

                if (this.user.getLogged() == false)
                    return new UserResponse("insertMarketOrder", 102, "you can't insert orders if not logged in");
                
                orderVal = gson.fromJson(obj.get("values"), OrderValues.class);

                code = orderBook.execMarketOrder(orderVal.getSize(), orderVal.getType(), "market", this.user.getUsername(), -1);

                if (code == -1) {
                    System.out.println("market order inserted by user " + this.user.getUsername() + "cannot be executed");
                }

                response = new OrderResponse(code);

            break;

            case "insertStopOrder":
                if (this.user == null)
                    return new UserResponse("insertStopOrder", 101, "user error");

                if (this.user.getLogged() == false)
                    return new UserResponse("insertStopOrder", 102, "you can't insert orders if not logged in");
                
                orderVal = gson.fromJson(obj.get("values"), OrderValues.class);

                code = orderBook.addStopOrder(this.user.getUsername(), orderVal.getSize(), orderVal.getPrice(), orderVal.getType());

                if (code == -1) {
                    System.out.println("error with stop order inserted by user " + this.user.getUsername());
                }

                response = new OrderResponse(code);

            break;

            case "cancelOrder":
                if (this.user == null)
                    return new UserResponse("cancelOrder", 101, "user error");
                
                if (this.user.getLogged() == false)
                    return new UserResponse("cancelOrder", 102, "you can't cancel orders if not logged in");

                int orderID = obj.get("orderID").getAsInt();

                code = orderBook.cancelOrder(orderID, this.user.getUsername());

                if (code == 100) {
                    msg = "OK";
                    System.out.println("order with ID: " + orderID + " cancelled by user " + this.user.getUsername());
                }
                else if (code == 101) {
                    msg = "order does not exist";
                }

                response = new UserResponse("cancelOrder", code, msg);
            break;

            case "getOrderBook":
                if (this.user == null)
                    return new UserResponse("getOrderBook", 101, "user error");

                // anyone can get order book

                // get order book function ...

                // in msg put the order book and stop orders
                
                response = new UserResponse("getOrderBook", code, msg);
            break;

            case "getPriceHistory":
                if (this.user == null)
                    return new UserResponse("getPriceHistory", 101, "user error");

                if (this.user.getLogged() == false)
                    return new UserResponse("getPriceHistory", 102, "you can't get price history if not logged in");

                HistoryValues historyVal = gson.fromJson(obj.get("values"), HistoryValues.class);

                int months = historyVal.getMonth();
                int year = historyVal.getYear();  

                if (months < 1 || months > 12)
                    return new UserResponse("getPriceHistory", 103, "invalid month");
                    
                if (year < 1970 || year > Year.now().getValue())
                    return new UserResponse("getPriceHistory", 104, "invalid year");

                response = new HistoryResponse(YearMonth.of(year, months), priceHistory.getPriceHistory(YearMonth.of(year, months), tradeMap));
            break;

            default:
                response = new UserResponse("unknown", 101, "unknown operation");
            break;
        }

        return response;
    }

    public long getLastActivityTime() {
        return lastActivityTime;
    }

    public void updateLastActivityTime() {
        this.lastActivityTime = System.currentTimeMillis();
    }

}