package com.unipi.lab3.cross.client;

import java.io.BufferedReader;
import java.net.DatagramSocket;

public class UdpListener implements Runnable {

    private DatagramSocket socket;

    private BufferedReader in;

    private int port;

    public UdpListener(BufferedReader in, int port) throws Exception {
        if (port == 0)
            this.socket = new DatagramSocket();
        else
            this.socket = new DatagramSocket(port);
        
        this.in = in;
        this.port = this.socket.getLocalPort();
    }

    public int getPort() {
        return this.port;
    }

    public void run() {

    }   
    
    
}
