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

public class ServerMain {

    public static final String configFile = "server.properties";
    public static int tcpPort;
    public static int udpPort;

    public static ServerSocket serverSocket;

    private static UserManager userManager;

    // list of all orders ...

    private static OrderBook orderBook;

    private static TradeMap tradeMap;
    private static ArrayList<Trade> bufferedTrades;

    public static ConcurrentHashMap<Socket, ClientHandler> activeClients;

    public static UdpNotifier udpNotifier;

    public static InactivityHandler inactivityHandler;
    public static Thread inactivityThread;

    public static int inactivityTimeout;

    public static PersistenceHandler persistenceHandler;

    //threadpool
    public static final ExecutorService pool = Executors.newCachedThreadPool();

    public static void main(String[] args) throws Exception{
        try {
            getServerProperties();

            // load from files into user manager, orderbook and trade map ...

            userManager = new UserManager();

            // order lists ...
            orderBook = new OrderBook();

            tradeMap = new TradeMap();

            bufferedTrades = new ArrayList<>();

            activeClients = new ConcurrentHashMap<>();

            
            udpNotifier = new UdpNotifier(udpPort);

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

            // TCP socket
            serverSocket = new ServerSocket(tcpPort);

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

    public void loadOrderBook () {

    }

    public void loadUsers () {

    }

    public void loadTrades () {

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
