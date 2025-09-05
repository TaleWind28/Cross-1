package com.unipi.lab3.cross.main;

import com.unipi.lab3.cross.model.*;
import com.unipi.lab3.cross.model.orders.*;
import com.unipi.lab3.cross.model.trade.*;
import com.unipi.lab3.cross.model.user.*;
import com.unipi.lab3.cross.server.*;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.lang.reflect.Type;
import java.time.*;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;

public class ServerMain {

    public static final String configFile = "server.properties";
    public static int tcpPort;
    public static int udpPort;

    public static ServerSocket serverSocket;

    private static ConcurrentHashMap<String, User> users;

    private static UserManager userManager;

    private static ConcurrentSkipListMap<Integer, OrderGroup> askOrders;
    private static ConcurrentSkipListMap<Integer, OrderGroup> bidOrders;
    private static ConcurrentLinkedQueue<StopOrder> stopAsks;
    private static ConcurrentLinkedQueue<StopOrder> stopBids;

    private static OrderBook orderBook;

    private static TradeMap tradeMap;
    private static LinkedList<Trade> bufferedTrades;

    public static ConcurrentHashMap<Socket, ClientHandler> activeClients;

    public static UdpNotifier udpNotifier;

    public static InactivityHandler inactivityHandler;
    public static Thread inactivityThread;

    public static int inactivityTimeout;

    public static PersistenceHandler persistenceHandler;

    public static Gson gson = new Gson();

    //threadpool
    public static final ExecutorService pool = Executors.newCachedThreadPool();

    public static void main(String[] args) throws Exception{
        try {
            // read config file with server properties
            getServerProperties();

            // TCP socket
            serverSocket = new ServerSocket(tcpPort);

            // UDP notifier
            udpNotifier = new UdpNotifier(udpPort);

            // load users, trades and orders from files

            loadUsers();

            userManager = new UserManager(users);

            loadTrades();

            loadOrderBook();

            bufferedTrades = new LinkedList<>();

            orderBook.setUdpNotifier(udpNotifier);
            orderBook.setTradeMap(tradeMap);
            orderBook.setBufferedTrades(bufferedTrades);

            activeClients = new ConcurrentHashMap<>();

            persistenceHandler = new PersistenceHandler(orderBook, userManager, bufferedTrades);

            ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
            scheduler.scheduleAtFixedRate(() -> {
                try {
                    persistenceHandler.saveAll();
                } catch (Exception e) {
                    System.err.println("Error during persistence: " + e.getMessage());
                }
            }, 1,1, java.util.concurrent.TimeUnit.MINUTES);

            // inactivity handler thread
            inactivityHandler = new InactivityHandler(activeClients, userManager, orderBook);
            inactivityThread = new Thread(inactivityHandler);
            inactivityThread.start();

            Runtime.getRuntime().addShutdownHook(new Thread() {
                public void run(){
                    System.out.println("closing server...");
                    // method to close ...
                }
            });

            // listening server for client connections
            while (true) {
                try { 
                        
                    Socket clientSocket = serverSocket.accept();

                    ClientHandler handler = new ClientHandler(clientSocket, userManager, orderBook, tradeMap, udpNotifier, inactivityHandler);

                    activeClients.put(clientSocket, handler);

                    pool.execute(handler);

                }
                catch (SocketException se) {
                    break;
                }
                catch (IOException e) {
                    System.err.println(e.getMessage());
                }

            }

        }
        catch (Exception e) {
            System.err.println("Server error: " + e.getMessage());
            return;
        }
    }

    public static void loadUsers () {
        File file = new File("src/main/resources/users.json");

        try (FileReader fr = new FileReader(file)) {
            Type type = new TypeToken<ConcurrentHashMap<String, User>>() {}.getType();

            if (file.length() != 0) {
                users = gson.fromJson(fr, type);
                if (users == null)
                    // empty json file
                    users = new ConcurrentHashMap<>();
            }
            else
                users = new ConcurrentHashMap<>();
            
        } catch (FileNotFoundException e) {
            System.err.println("users file not found");
            users = new ConcurrentHashMap<>();
        } 
        catch (JsonIOException e) {
            System.err.println("error parsing users file: " + e.getMessage());
            users = new ConcurrentHashMap<>();
        }
        catch (JsonSyntaxException e) {
            System.err.println("error in users file syntax: " + e.getMessage());
            users = new ConcurrentHashMap<>();
        }
        catch (IOException e) {
            System.err.println("error reading users file: " + e.getMessage());
            users = new ConcurrentHashMap<>();
        }
    }

    public static void loadTrades () {
        File file = new File("src/main/resources/storicoOrdini.json");

        try (FileReader fr = new FileReader(file)) {
            if (file.length() != 0) {
                JsonObject obj = JsonParser.parseReader(fr).getAsJsonObject();

                JsonArray tradesArray = obj.getAsJsonArray("trades");

                Type tradeListType = new TypeToken<LinkedList<Trade>>() {}.getType();

                LinkedList<Trade> trades = gson.fromJson(tradesArray, tradeListType);

                tradeMap = new TradeMap();

                for (Trade trade : trades) {

                    LocalDate date = Instant.ofEpochSecond(trade.getTimestamp()).atZone(ZoneId.systemDefault()).toLocalDate();

                    tradeMap.addTrade(date, trade);
                }
            }
            else
                tradeMap = new TradeMap();
        }
        catch (FileNotFoundException e) {
            System.err.println("trades file not found");
            tradeMap = new TradeMap();
        } 
        catch (JsonIOException e) {
            System.err.println("error parsing trades file: " + e.getMessage());
            tradeMap = new TradeMap();
        }
        catch (JsonSyntaxException e) {
            System.err.println("error in trades file syntax: " + e.getMessage());
            tradeMap = new TradeMap();
        }
        catch (IOException e) {
            System.err.println("error reading trades file: " + e.getMessage());
            tradeMap = new TradeMap();
        }
    }

    public static void loadOrderBook () {
        File file = new File ("src/main/resources/orderBook.json");

        try (FileReader fr = new FileReader(file)) {
            if (file.length() != 0) {
                Type type = new TypeToken<OrderBook>() {}.getType();

                orderBook = gson.fromJson(fr, type);

                if (orderBook.getLimitAsks() == null)
                    orderBook.setAskOrders(new ConcurrentSkipListMap<>());

                if (orderBook.getLimitBids() == null)
                    orderBook.setBidOrders(new ConcurrentSkipListMap<>(Comparator.reverseOrder()));

                if (orderBook.getStopAsks() == null)
                    orderBook.setStopAsks(new ConcurrentLinkedQueue<>());

                if (orderBook.getStopBids() == null)
                    orderBook.setStopBids(new ConcurrentLinkedQueue<>());
            }
            else
                orderBook = new OrderBook();
        }
        catch (FileNotFoundException e) {
            System.err.println("orderBook file not found");
            orderBook = new OrderBook();
        }
        catch (JsonIOException e) {
            System.err.println("error parsing order book file: " + e.getMessage());
            orderBook = new OrderBook();
        } 
        catch (JsonSyntaxException e) {
            System.err.println("error in order book file syntax: " + e.getMessage());
            orderBook = new OrderBook();
        }
        catch (IOException e) {
            System.err.println("error reading order book file: " + e.getMessage());
            orderBook = new OrderBook();
        }  
    }

    public void closeServer () {

        // save data

        // interrump all threads
        // inactivity
        // scheduler

        // chiudere il pool

        // chiudere socket
    }

    public static void getServerProperties () throws FileNotFoundException, IOException {
        Properties props = new Properties();

        FileInputStream inputFile = new FileInputStream(configFile);
        props.load(inputFile);

        tcpPort = Integer.parseInt(props.getProperty("tcpPort"));
        udpPort = Integer.parseInt(props.getProperty("udpPort"));
        inactivityTimeout = Integer.parseInt(props.getProperty("timeout"));
        // other properties ...

        inputFile.close();
    }
}
