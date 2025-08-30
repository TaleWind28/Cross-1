package com.unipi.lab3.cross.server;

import java.net.*;
import java.io.*;
import java.util.concurrent.ConcurrentHashMap;

public class UdpNotifier {

    // socket udp
    private DatagramSocket socket;
    private int serverPort; // fixed server port

    // map username -> address
    private ConcurrentHashMap<String, InetSocketAddress> udpClients;

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

    public void notifyClient (String username, String message) {
        // check if this username is registered
        if (!this.udpClients.containsKey(username))
            return;

        try {
            // get username info
            InetSocketAddress clientAddress = this.udpClients.get(username);

            InetAddress addr = clientAddress.getAddress();
            int clientPort = clientAddress.getPort();
    
            // convert string to bytes
            byte [] buf = message.getBytes();

            // create packet to send data
            DatagramPacket packet = new DatagramPacket(buf, buf.length, addr, clientPort);

            socket.send(packet);
        }
        catch (IOException e) {
            System.err.println(e.getMessage());
        }

    }

    public void notify2Clients (String user1, String msg1, String user2, String msg2) {
        notifyClient(user1, msg1);
        notifyClient(user2, msg2);
    }

}
