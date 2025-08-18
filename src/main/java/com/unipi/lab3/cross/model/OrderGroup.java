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
    private int total; // useful or i could calculate it when needed?

    // limit orders list with that price
    // in the list they have to be ordered by arriving time
    
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

    public boolean isEmpty () {
        return this.limitOrders.isEmpty();
    }

    // add a limit order to the queue
    public void addOrder (LimitOrder order) {
        // add the new order to the existing queue
        this.limitOrders.add(order);

        // update size of the group
        this.size += order.getSize();

        // update total of the group
        int newTotal = order.getLimitPrice() * this.size;
        setTotal(newTotal);
    }

    public boolean removeOrder (int orderId, String username) {
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
                int newTotal = order.getLimitPrice() * this.size;
                setTotal(newTotal);

                return true;
            }
        }

        // order not found or not removed
        return false;
    }
}
