package com.unipi.lab3.cross.model.orders;

public class LimitOrder extends Order {

    private int limitPrice;

    public LimitOrder (int orderID, String username, String type, int size, int limitPrice) {
        super(orderID, username, type, size);
        this.limitPrice = limitPrice;
    }

    public int getLimitPrice () {
        return limitPrice;
    }    
}
