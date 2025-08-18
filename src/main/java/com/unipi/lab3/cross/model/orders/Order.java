package com.unipi.lab3.cross.model.orders;

/**
 * class representing a generic order, with generic attributes
 * can be extended to represent Limit, Market or Stop orders
*/

public class Order {
    private int orderID;
    private String username;

    private String type; // ask o bid

    private int size;

    // date can be useful?

    // what else?

    public Order (int orderID, String username, String type, int size) {
        this.orderID = orderID;
        this.username = username;
        this.type = type;
        this.size = size;
    }

    public int getOrderId () {
        return this.orderID;
    }

    public String getUsername () {
        return this.username;
    }

    public String getType () {
        return this.type;
    }

    public int getSize () {
        return this.size;
    }

    public void setSize (int size) {
        this.size = size;
    }

}