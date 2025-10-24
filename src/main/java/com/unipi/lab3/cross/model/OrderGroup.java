package com.unipi.lab3.cross.model;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.Iterator;

import com.unipi.lab3.cross.model.orders.LimitOrder;

/**
    class representing a group of limit orders at a specific price
    
*/

public class OrderGroup {
    // price è la chiave della mappa 

    private int size;
    private long total;

    // limit orders list with that price
    // in the list they have to be ordered by arriving time
    
    private ConcurrentLinkedQueue<LimitOrder> limitOrders = new ConcurrentLinkedQueue<>();

    public OrderGroup (int size, long total, ConcurrentLinkedQueue<LimitOrder> limitOrders) {
        this.size = size;
        this.total = total;
        this.limitOrders = limitOrders;
    }

    public OrderGroup () {
        this.size = 0;
        this.total = 0;
        this.limitOrders = new ConcurrentLinkedQueue<>();
    }

    public int getSize () {
        return this.size;
    }

    public long getTotal () {
        return this.total;
    }

    public ConcurrentLinkedQueue<LimitOrder> getLimitOrders () {
        return this.limitOrders;
    }

    public void setSize (int size) {
        this.size = size;
    }

    public void setTotal (long total) {
        this.total = total;
    }

    public boolean isEmpty () {
        return this.limitOrders.isEmpty();
    }

    public synchronized void updateGroup (int filledSize, int limitPrice) {
        this.size -= filledSize;
        this.total = (long) limitPrice * this.size;
    }

    public int getFilteredSize (String excludedUsername) {
        int filteredSize = this.size;

        for (LimitOrder order : this.limitOrders) {
            if (order.getUsername().equals(excludedUsername)) {
                // if the order is from the user, subtract its size from the total size
                filteredSize -= order.getSize();
            }
        }

        return filteredSize;
    }

    // add a limit order to the queue
    public synchronized void addOrder (LimitOrder order) {
        // add the new order to the existing queue
        this.limitOrders.add(order);

        // update size of the group
        this.size += order.getSize();

        // update total of the group
        long newTotal = (long) order.getLimitPrice() * this.size;
        this.total = newTotal;
    }

    public synchronized boolean removeOrder (int orderId, String username) {
        // check if the order with this id exists
        // iterate through the queue

        Iterator<LimitOrder> iterator = this.limitOrders.iterator();

        while (iterator.hasNext()) {
            LimitOrder order = iterator.next();

            // remove the order if it has the same ID and it has been added by the same user who is trying to remove it
            if (order.getOrderId() == orderId && order.getUsername().equals(username)) {
                // remove the order from the queue
                iterator.remove();

                // update the size
                this.size -= order.getSize();

                // recalculate the total with the new size value
                long newTotal = (long) order.getLimitPrice() * this.size;
                setTotal(newTotal);

                return true;
            }
        }

        // order not found or not removed
        return false;
    }

    public void printGroup () {
        System.out.println("Size: " + this.size + " Total: " + this.total);
        System.out.println("Orders:");
        for (LimitOrder order : this.limitOrders) {
            System.out.println(order.toString());
        }
    }
}
