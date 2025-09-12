package com.unipi.lab3.cross.server;

import java.util.*;
import java.io.*;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.unipi.lab3.cross.model.OrderBook;
import com.unipi.lab3.cross.model.user.UserManager;
import com.unipi.lab3.cross.model.trade.Trade;

public class PersistenceHandler {

    private OrderBook orderBook;
    private UserManager userManager;
    private LinkedList<Trade> bufferedTrades;

    private final String usersFile = "src/main/resources/users.json";
    private final String ordersFile = "src/main/resources/orders.json";
    private final String tradesFile = "src/main/resources/storicoOrdini.json";

    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public PersistenceHandler (OrderBook orderBook, UserManager userManager, LinkedList<Trade> bufferedTrades) {
        this.orderBook = orderBook;
        this.userManager = userManager;
        this.bufferedTrades = bufferedTrades;
    }

    public synchronized void saveAll () {
        // debug
        System.out.println("saving data");
        
        saveUsers();
        saveOrders();
        saveTrades();
    }

    private void saveUsers () {
        try (FileWriter writer = new FileWriter(usersFile)) {
            gson.toJson(userManager.getUsers(), writer);
        } catch (IOException e) {
            System.err.println("error saving users: " + e.getMessage());
        }
    }

    private void saveOrders () {
        try (FileWriter writer = new FileWriter(ordersFile)) {
            gson.toJson(orderBook, writer);
        } catch (IOException e) {
            System.err.println("error saving orders: " + e.getMessage());
        }
    }

    private synchronized void saveTrades() {
        if (bufferedTrades.isEmpty()) return;

        // append into file storicoOrdini.json
        try (FileWriter writer = new FileWriter(tradesFile, true)) {
            for (Trade t : bufferedTrades) {
                writer.write(gson.toJson(t) + "\n");
            }
            bufferedTrades.clear();
        } catch (IOException e) {
            System.err.println("error saving trades: " + e.getMessage());
        }
    }
}