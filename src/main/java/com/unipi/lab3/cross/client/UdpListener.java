package com.unipi.lab3.cross.client;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.io.IOException;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.unipi.lab3.cross.json.response.Notification;
import com.unipi.lab3.cross.model.trade.*;

public class UdpListener implements Runnable {

    private DatagramSocket socket;

    private int port;

    private volatile boolean running;

    private final DateTimeFormatter FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public UdpListener(int port) throws Exception {
        if (port == 0)
            this.socket = new DatagramSocket();
        else
            this.socket = new DatagramSocket(port);
        
        this.socket.setSoTimeout(1000);
        this.port = this.socket.getLocalPort();
    }

    public int getPort() {
        return this.port;
    }

    public void run() {

        running = true;

        try {
            while (running && !Thread.currentThread().isInterrupted()) {
                try {
                    byte [] buf = new byte[4096];

                    DatagramPacket packet = new DatagramPacket(buf, buf.length);

                    socket.receive(packet);

                    String jsonString = new String(packet.getData(), 0, packet.getLength());

                    Notification notification = gson.fromJson(jsonString, Notification.class);

                    for (Trade trade : notification.getTrades()) {
                        System.out.println(trade.getOrderType() + " " + trade.getType() + " order " + trade.getOrderId() + " of " + trade.getSize() + " BTC at " + trade.getPrice() + " USD has been executed at " + formatDate(trade.getTimestamp()));
                    }
                }
                catch (SocketTimeoutException e) {
                    continue;
                } 
                catch (SocketException e) {
                    if (running) {
                        System.err.println("Socket error: " + e.getMessage());
                    }
                }
                catch (IOException e) {
                    if (running) {
                        System.err.println("UDP error: " + e.getMessage());
                    }
                }
            }
        }
        finally {
            running = false;
        }
    }
    
    public void stop() {
        running = false;
        socket.close();
    }

    public String formatDate (long timestamp) {
        LocalDateTime date = Instant.ofEpochSecond(timestamp).atZone(ZoneId.systemDefault()).toLocalDateTime();

        return date.format(FORMAT);
    }
}
