package com.unipi.lab3.cross.server;

import java.net.*;
import java.io.*;
import java.util.concurrent.ConcurrentHashMap;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.unipi.lab3.cross.server.UdpNotifier;
import com.unipi.lab3.cross.json.response.Notification;

public class UdpNotifier {

    // socket udp
    private DatagramSocket socket;
    private int serverPort; // fixed server port

    // map username -> address
    private ConcurrentHashMap<String, InetSocketAddress> udpClients;

    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public UdpNotifier (int serverPort) throws Exception {
        this.serverPort = serverPort;
        this.socket = new DatagramSocket(serverPort);
        this.udpClients = new ConcurrentHashMap<>();
    }

    public InetSocketAddress getClient (String username) {
        return this.udpClients.get(username);
    }

    public void registerClient (String username, InetAddress address, int port) {
        // add client udp info to the map
        InetSocketAddress socketAddress = new InetSocketAddress(address, port);
        this.udpClients.put(username, socketAddress);
    }

    public void removeClient (String username) {
        this.udpClients.remove(username);
    }

    public synchronized void notifyClient (String username, Notification notification) {
        // check if this username is registered
        if (!this.udpClients.containsKey(username))
            return;

        try {
            // get username info
            InetSocketAddress clientAddress = this.udpClients.get(username);

            InetAddress addr = clientAddress.getAddress();
            int clientPort = clientAddress.getPort();

            // notification to json
            String jsonString = gson.toJson(notification);
    
            // convert string to bytes
            byte [] buf = jsonString.getBytes();

            // create packet to send data
            DatagramPacket packet = new DatagramPacket(buf, buf.length, addr, clientPort);

            socket.send(packet);
        }
        catch (IOException e) {
            System.err.println("UDP error to " + username + ": " + e.getMessage());
        }

    }

    public void close() {
    if (socket != null && !socket.isClosed())
        socket.close();
    }

}
