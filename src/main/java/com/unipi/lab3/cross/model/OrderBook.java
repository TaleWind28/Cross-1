package com.unipi.lab3.cross.model;

import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.Iterator;
import java.util.Map;
import java.util.Collections;

import java.util.concurrent.atomic.AtomicInteger;

import com.unipi.lab3.cross.model.orders.*;

public class OrderBook {

    // map price - list ask limit orders
    // ascending order for keys
    private ConcurrentSkipListMap<Integer, OrderGroup> askOrders;
    
    // map price - list bid limit orders
    // descending order for keys
    private ConcurrentSkipListMap<Integer, OrderGroup> bidOrders;

    // stop orders
    private ConcurrentLinkedQueue<StopOrder> stopAsks;
    private ConcurrentLinkedQueue<StopOrder> stopBids;

    private int spread;

    private int bestAskPrice;
    private int bestBidPrice;

    private static final AtomicInteger idCounter = new AtomicInteger(100);

    public OrderBook (ConcurrentSkipListMap<Integer, OrderGroup> askOrders, ConcurrentSkipListMap<Integer, OrderGroup> bidOrders, int spread, int bestAskPrice, int bestBidPrice) {
        this.askOrders = askOrders;
        this.bidOrders = bidOrders;
        this.spread = spread;
        this.bestAskPrice = bestAskPrice;
        this.bestBidPrice = bestBidPrice;
    }

    private ConcurrentSkipListMap<Integer, OrderGroup> getLimitAsks () {
        return this.askOrders;
    }

    private ConcurrentSkipListMap<Integer, OrderGroup> getLimitBids () {
        return this.bidOrders;
    }

    private ConcurrentLinkedQueue<StopOrder> getStopAsks () {
        return this.stopAsks;
    }

    private ConcurrentLinkedQueue<StopOrder> getStopBids () {
        return this.stopBids;
    }

    private int getSpread () {
        return this.spread;
    }

    private int getBestAskPrice () {
        return this.bestAskPrice;
    }

    private int getBestBidPrice () {
        return this.bestBidPrice;
    }

    private int counterOrderId () {
        return idCounter.getAndIncrement();
    }

    // method to add a limit order
    public void addLimitOrder (int orderId, String type, String username, int size, int price) {
        // create a new LimitOrder object
        LimitOrder order = new LimitOrder(orderId, username, type , size, price);

        // select the right map
        ConcurrentSkipListMap<Integer, OrderGroup> selectedMap = type.equals("ask") ? this.askOrders : this.bidOrders;

        if (selectedMap.containsKey(price)) {
            // if the price already exists, add the order to the OrderGroup
            OrderGroup group = selectedMap.get(price);

            group.addOrder(order);
        } else {
            // if the price doesn't exist, create a new OrderGroup
            ConcurrentLinkedQueue<LimitOrder> ordersQueue = new ConcurrentLinkedQueue<>();

            ordersQueue.add(order);

            OrderGroup newGroup = new OrderGroup(size, price * size, ordersQueue);

            selectedMap.put(price, newGroup);
        }
    }

    // method to add a stop order
    public int addStopOrder (String username, int size, int price, String type) {
        // create new order id
        int orderId = counterOrderId(); // generate unique order id

        // create a new StopOrder object
        StopOrder order = new StopOrder(orderId, username, type, size, price);

        ConcurrentLinkedQueue<StopOrder> selectedQueue = type.equals("ask") ? this.stopAsks : this.stopBids;

        // add the stop order to the stop orders queue
        selectedQueue.add(order);

        return orderId;
    }

    // method for removing an order
    public int cancelOrder (int orderId, String username) {

        // check if the order is in the ask map
        for (OrderGroup group : this.askOrders.values()) {
            if (group.removeOrder(orderId, username)) {
                // if the order has been removed successfully, return 100
                return 100;
            }
        }

        // check if the order is in the bid map
        for (OrderGroup group : this.bidOrders.values()) {
            if (group.removeOrder(orderId, username)) {
                return 100;
            }
        }

        // check if the order is in the stop ask queue
        Iterator<StopOrder> askIterator = this.stopAsks.iterator();
        while (askIterator.hasNext()) {
            StopOrder stopOrder = askIterator.next();

            if (stopOrder.getOrderId() == orderId && stopOrder.getUsername().equals(username)) {
                // remove the stop order
                askIterator.remove();

                return 100; // success
            }
        }

        // check if the order is in the stop bid queue
        Iterator<StopOrder> bidIterator = this.stopBids.iterator();
        while (bidIterator.hasNext()) {
            StopOrder stopOrder = bidIterator.next();
            
            if (stopOrder.getOrderId() == orderId && stopOrder.getUsername().equals(username)) {
                bidIterator.remove();

                return 100;
            }
        }

        // error -> order not found or not removed
        return 101;
    }
}