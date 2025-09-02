package com.unipi.lab3.cross.main;

import com.unipi.lab3.cross.client.*;
import com.unipi.lab3.cross.json.request.*;
import com.unipi.lab3.cross.json.response.*;
import com.unipi.lab3.cross.model.OrderBook;
import com.unipi.lab3.cross.model.orders.Order;
import com.unipi.lab3.cross.model.user.User;
import com.unipi.lab3.cross.model.user.UserManager;
import com.unipi.lab3.cross.server.UdpNotifier;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class ClientMain {

    private static final String configFile = "client.properties";

    private static int tcpPort;
    private static String address;

    private static Socket socket;

    private static BufferedReader in;
    private static PrintWriter out;
    private static Scanner scanner;

    private static Thread receiver;
    private static ClientReceiver clientReceiver;

    private static Thread listener;
    private static UdpListener udpListener;

    private static boolean active = false;

    private static String username;
    private static final AtomicBoolean logged = new AtomicBoolean(false);

    public static void main (String[] args) throws Exception {

        getProperties();

        try {
            socket = new Socket(address, tcpPort);

            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            scanner = new Scanner(System.in);

            clientReceiver = new ClientReceiver(in, logged);
            receiver = new Thread(clientReceiver);
            receiver.start();

            active = true;

            while (active) {
                try {
                    String userInput = scanner.nextLine();

                    if (userInput.isEmpty() || userInput.isBlank() || !isValid(userInput)) {
                        // command not valid
                        continue;
                    }

                    String[] inputStrings = userInput.split("\\(", 2);

                    String command = inputStrings[0].trim();

                    String param = inputStrings[1].substring(0, inputStrings[1].length() - 1).trim();

                    Request request = null;
                    
                    switch(command) {
                        case "help":
                            // print all commands
                            System.out.println("available commands:");
                            // ... to finish ...
                        break;

                        case "exit":
                            close();
                        break;

                        default:
                            List<String> params = new ArrayList<>();

                            if (!param.isEmpty()) {
                                String[] tokens = param.split("\\s*,\\s*");
                                params = Arrays.asList(tokens);
                            }

                            request = buildRequest(command, params);

                        break;
                    }

                    if (request != null) {
                        // convert to json message with gson builder
                        Gson gson = new GsonBuilder()
                                        .setPrettyPrinting()
                                        .create();
                        
                        String jsonString = gson.toJson(request);

                        // send on tcp socket to server
                        out.println(jsonString);
                    }
                }
                catch (NoSuchElementException e) {
                    // scanner closed
                    active = false;
                }   
                catch (Exception e) {

                }
            }
        }
        catch (IOException e) {
            System.err.println("network error:" + e.getMessage());
            active = false;
        }
        catch (NumberFormatException e) {
            System.err.println("invalid number format:" + e.getMessage());
        }


    }

    private static Request buildRequest (String operation, List<String> paramList) {
        Request request = null;

        switch (operation) {
            case "register":
                // check number of params
                if (paramList.size() != 2) {
                    System.out.println("invalid number of parameters");
                    break;
                }

                username = paramList.get(0);
                String password = paramList.get(1);

                // create values object
                UserValues values = new UserValues(username, password);

                // create request object
                request = new Request<UserValues>("register", values);  

            break;

            case "login":
                if (logged.get()) {
                    System.out.println("you are already logged in");
                    break;
                }

                if (paramList.size() != 2) {
                    System.out.println("invalid number of parameters, insert username and password");
                    break;
                }

                username = paramList.get(0);
                password = paramList.get(1);

                try {
                    udpListener = new UdpListener(0);
                    listener = new Thread(udpListener);

                    listener.start();

                    // wait for listener to start
                    int sleepTime = 500;
                    Thread.sleep(sleepTime);

                    int udpPort = udpListener.getPort();

                    NetworkValues netValues = new NetworkValues(udpPort);

                    values = new UserValues(username, password, netValues);

                    request = new Request<UserValues>("login", values);
                }
                catch (InterruptedException e) {
                    System.err.println("error waiting for udp listener to start" + e.getMessage());
                }
                catch (Exception e) {
                    System.err.println("error starting udp listener" + e.getMessage());
                }

            break;

            case "updateCredentials":
                if (logged.get()) {
                    System.out.println("you can't change credentials while logged in");
                    break;
                }

                if (paramList.size() != 3) {
                    System.out.println("invalid number of parameters, insert username, oldPassword and newPassword");
                    break;
                }

                username = paramList.get(0);
                String oldPassword = paramList.get(1);
                String newPassword = paramList.get(2);

                values = new UserValues(username, oldPassword, newPassword);

                request = new Request<UserValues>("updateCredentials", values);

            break;

            case "logout":
                if (username == null) {
                    System.out.println("you have to sign in first");
                    break;
                }
                else if (!logged.get()) {
                    System.out.println("you are not logged in");
                    break;
                }

                if (!paramList.isEmpty()) {
                    System.out.println("invalid command");
                    break;
                }

                values = new UserValues(username);

                request = new Request<UserValues>("logout", values);
            
            break;

            case "insertLimitOrder":
                if (username == null || !logged.get()) {
                    System.out.println("operation not allowed");
                    break;
                }

                if (paramList.size() != 3) {
                    System.out.println("invalid number of parameters, insert type, size and limitPrice");
                    break;
                }

                String type = paramList.get(0);

                if (!type.equals("ask") && !type.equals("bid")) {
                    System.out.println("type must be ask or bid");
                    break;
                }

                int size = 0;
                int limitPrice = 0;

                try {
                    size = Integer.parseInt(paramList.get(1));
                    limitPrice = Integer.parseInt(paramList.get(2));
                }
                catch (NumberFormatException e) {
                    System.out.println("invalid number format");
                    break;
                }

                if (size <= 0 || limitPrice <= 0) {
                    System.out.println("invalid parameters");
                    break;
                }

                OrderValues orderVal = new OrderValues(type, size, limitPrice);

                request = new Request<OrderValues>("insertLimitOrder", orderVal);

            break;

            case "insertMarketOrder":
                if (username == null || !logged.get()) {
                    System.out.println("operation not allowed");
                    break;
                }

                if (paramList.size() != 2) {
                    System.out.println("invalid number of parameters for market order, insert type and size");
                    break;
                }

                type = paramList.get(0);

                if (!type.equals("ask") && !type.equals("bid")) {
                    System.out.println("type must be ask or bid");
                    break;
                }

                try {
                    size = Integer.parseInt(paramList.get(1));
                }
                catch (NumberFormatException e) {
                    System.out.println("invalid number format");
                    break;
                }

                if (size <= 0) {
                    System.out.println("invalid order parameters");
                    break;
                }

                orderVal = new OrderValues(type, size, 0);

                request = new Request<OrderValues>("insertMarketOrder", orderVal);

            break;

            case "insertStopOrder":
                if (username == null || !logged.get()) {
                    System.out.println("operation not allowed");
                    break;
                }    
            
                if (paramList.size() != 3) {
                    System.out.println("invalid number of parameters for stop order, insert type, size and stopPrice");
                    break;
                }

                type = paramList.get(0);

                if (!type.equals("ask") && !type.equals("bid")) {
                    System.out.println("type must be ask or bid");
                    break;
                }

                int stopPrice = 0;

                try {
                    size = Integer.parseInt(paramList.get(1));
                    stopPrice = Integer.parseInt(paramList.get(2));
                }
                catch (NumberFormatException e) {
                    System.out.println("invalid number format");
                    break;
                }

                if (size <= 0 || stopPrice <= 0) {
                    System.out.println("invalid order parameters");
                    break;
                }

                orderVal = new OrderValues(type, size, stopPrice);

                request = new Request<OrderValues>("insertStopOrder", orderVal);            

            break;

            case "cancelOrder":
                if (username == null || !logged.get()) {
                    System.out.println("operation not allowed");
                    break;
                }

                if (paramList.size() != 1) {
                    System.out.println("invalid number of parameters, insert orderID");
                    break;
                }

                int orderID = Integer.parseInt(paramList.get(0));

                if (orderID <= 0) {
                    System.out.println("invalid orderID");
                    break;
                }

                OrderResponse ID = new OrderResponse(orderID);

                request = new Request<OrderResponse>("cancelOrder", ID);

            break;

            case "getOrderBook":
                if (!paramList.isEmpty()) {
                    System.out.println("invalid command");
                    break;
                }

                request = new Request<Values>("getOrderBook", null);

            break;

            case "getPriceHistory":
                if (username == null || !logged.get()) {
                    System.out.println("operation not allowed");
                    break;
                }
                
                if (paramList.size() != 2) {
                    System.out.println("invalid number of parameters, insert month and year");
                    break;
                }

                int month = Integer.parseInt(paramList.get(0));
                int year = Integer.parseInt(paramList.get(1));

                if (month < 1 || month > 12 || year < 2000) {
                    System.out.println("invalid year and month parameters");
                    break;
                }

                HistoryValues stats = new HistoryValues(month, year);

                request = new Request<HistoryValues>("getPriceHistory", stats);

            break;
        }

        return request;
    }

    public static void close () {
        // implement ...
    }

    // check if input is valid
    // should be command(args,...) or command()
    public static boolean isValid (String input) {
        return input.matches("^[a-zA-Z]+\\(([a-zA-Z0-9_]+(,[a-zA-Z0-9_]+)*)?\\)$");
    }

    public static void getProperties () throws FileNotFoundException, IOException {
        Properties props = new Properties();

        FileInputStream inputFile = new FileInputStream(configFile);
        props.load(inputFile);

        tcpPort = Integer.parseInt(props.getProperty("tcpPort"));
        address = props.getProperty("address");

        inputFile.close();
    }
}