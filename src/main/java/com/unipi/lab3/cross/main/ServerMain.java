package com.unipi.lab3.cross.main;

import com.unipi.lab3.cross.model.*;
import com.unipi.lab3.cross.model.orders.*;
import com.unipi.lab3.cross.server.*;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ServerMain {

    public static final String configFile = "server.properties";
    public static int tcpPort;
    public static int udpPort;

    public static int inactivityTimeout;

    public static ServerSocket serverSocket;

    //threadpool
    public static final ExecutorService pool = Executors.newCachedThreadPool();

    public static void main(String[] args) throws Exception{

        readConfig();


        // upload orderbook, users from file ? how to do it ?

        // OrderBook orderBook = new OrderBook();
        // UserManager userManager = new UserManager();
        // mettere anche variabili per le varie liste

        // UdpNotifier udpNotifier = new UdpNotifier();

        // TCP socket
        try {

            serverSocket = new ServerSocket(tcpPort);

            // inactivity handler thread
            // Runtime ...

            // listening server for client connections
            while (true) {
                try { 
                    
                    Socket clientSocket = serverSocket.accept();

                    ClientHandler handler = new ClientHandler(clientSocket, null);

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
            System.err.println(e.getMessage());
        }
    }

    public static void readConfig () throws FileNotFoundException, IOException {
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
