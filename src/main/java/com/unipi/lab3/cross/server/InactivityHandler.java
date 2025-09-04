package com.unipi.lab3.cross.server;

import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.unipi.lab3.cross.model.OrderBook;
import com.unipi.lab3.cross.model.orders.StopOrder;
import com.unipi.lab3.cross.model.user.User;
import com.unipi.lab3.cross.model.user.UserManager;

public class InactivityHandler implements Runnable {

    private ConcurrentHashMap<Socket, ClientHandler> activeClients;
    private OrderBook orderBook;
    private UserManager userManager;

    private static final long TIMEOUT = 300000; // 5 minutes

    // active sockets

    public InactivityHandler (ConcurrentHashMap<Socket, ClientHandler> activeClients, UserManager userManager, OrderBook orderBook) {
        this.activeClients = activeClients;
        this.userManager = userManager;
        this.orderBook = orderBook;
    }

    public void run () {
        while (true) {
            long now = System.currentTimeMillis();

            try {
                for (ConcurrentHashMap.Entry<Socket, ClientHandler> entry : activeClients.entrySet()) {
                    Socket socket = entry.getKey();
                    ClientHandler handler = entry.getValue();

                    if (now - handler.getLastActivityTime() > TIMEOUT) {
                        handleTimeout(socket, handler);
                    }
                }
            }
            catch (Exception e) {
                System.err.println(e.getMessage());
            }

            try {
                Thread.sleep(5000);
            }
            catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.err.println("thread interrotto " + e.getMessage());
                break;
            }
        }
    }

    public void handleTimeout (Socket socket, ClientHandler handler) {
        try {
            if (!handler.isLoggedIn()) {
                System.out.println("not logged inactive client");
            }
            else {
                String username = handler.getUsername();

                // check if user has pending stop orders
                if (hasStopOrders(username)) {
                    System.out.println("user " + username + " has pending stop orders, not logging out");
                    return;
                }
                else {
                    User user = userManager.getUser(username);
                    if (user != null) {
                        user.setLogged(false);
                        
                        System.out.println("inactive user " + username);
                    }
                }
            }

            socket.close();
            activeClients.remove(socket);
            System.out.println("closed inactive connection");
        }
        catch (Exception e) {
            System.err.println("errore chiusura socket: " + e.getMessage());
        }
    }

    public boolean hasStopOrders (String username) {
        ConcurrentLinkedQueue<StopOrder> userStopOrders = orderBook.getUserStopOrders(username);
        
        if (userStopOrders != null && !userStopOrders.isEmpty())
            return true;
        
        return false;
    }
}