package com.unipi.lab3.cross.server;

import java.io.*;
import java.net.Socket;

public class ClientHandler implements Runnable {

    private Socket clientSocket;
    private volatile long lastActivityTime;
    private volatile boolean active;

    private UdpNotifier udpNotifier; // shared
    private String username;

    public ClientHandler(Socket clientSocket, UdpNotifier udpNotifier) {
        this.clientSocket = clientSocket;
        this.udpNotifier = udpNotifier;
    }

    @Override
    public void run() {

        active = true;

        String receivedMsg = null;

        try (
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);) {

                // listening for client messages
                while (active) {

                    receivedMsg = in.readLine();

                    if (receivedMsg == null)
                        break;

                    updateLastActivityTime();

                    // function to handle messages
                    handleRequest(receivedMsg);

                    // send the answer to the client                   

                }


        } 
        catch (Exception e) {
            
        }
        finally {
            active = false;
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
