package com.unipi.lab3.cross.model;

import java.util.concurrent.ConcurrentLinkedQueue;

import com.unipi.lab3.cross.model.orders.LimitOrder;

/**
    class representing a group of limit orders at a specific price
    
*/

public class OrderGroup {
    // price è la chiave della mappa 

    private int size;
    private int total; // useful or i could calculate it when needed?

    // limit orders list with that price
    // in the list they have to be ordered by arriving time
    
    // private List<LimitOrder> limitOrders;
    private ConcurrentLinkedQueue<LimitOrder> limitOrders = new ConcurrentLinkedQueue<>();

    public OrderGroup (int size, int total, ConcurrentLinkedQueue<LimitOrder> limitOrders) {
        this.size = size;
        this.total = total;
        this.limitOrders = limitOrders;
    }

    public int getSize () {
        return this.size;
    }

    public int getTotal () {
        return this.total;
    }

    public ConcurrentLinkedQueue<LimitOrder> getLimitOrders () {
        return this.limitOrders;
    }

    public void setSize (int size) {
        this.size = size;
    }

    public void setTotal (int total) {
        this.total = total;
    }
}
