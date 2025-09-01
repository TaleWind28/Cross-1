package com.unipi.lab3.cross.server;

import java.io.*;
import java.net.*;

public class ClientHandler implements Runnable {

    private Socket clientSocket;

    private UdpNotifier udpNotifier; // shared
    private String username;

    private volatile long lastActivityTime;
    private volatile boolean running;

    public ClientHandler(Socket clientSocket, UdpNotifier udpNotifier) {
        this.clientSocket = clientSocket;
        this.udpNotifier = udpNotifier;
    }

    @Override
    public void run() {

        running = true;

        try (
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)) {

                String receivedMsg;

                // listening for client messages
                while (running && ((receivedMsg = in.readLine()) != null)) {

                    updateLastActivityTime();

                    // function to handle messages
                    handleRequest(receivedMsg);

                    // send the answer to the client                   

                }


        } 
        catch (Exception e) {
            
        }
        finally {
            running = false;
            try {
                clientSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        
    }

    public long getLastActivityTime() {
        return lastActivityTime;
    }

    public void updateLastActivityTime() {
        this.lastActivityTime = System.currentTimeMillis();
    }
    
    public void handleRequest (String request) {
        // process request with json ...
    }

}
