package com.unipi.lab3.cross.client;

import java.net.DatagramSocket;

public class UdpListener implements Runnable {
    private DatagramSocket socket;

    private int port;

    public UdpListener(int port) throws Exception {
        if (port == 0)
            this.socket = new DatagramSocket();
        else
            this.socket = new DatagramSocket(port);
        
        this.port = this.socket.getLocalPort();
    }

    public int getPort() {
        return this.port;
    }

    public void run() {

    }   
    
    
}
