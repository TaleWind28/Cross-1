package com.unipi.lab3.cross.model.orders;

public class LimitOrder extends Order {

    private int limitPrice;

    public LimitOrder (int orderID, String username, String type, String orderType, int size, int limitPrice) {
        super(orderID, username, type, orderType, size);
        this.limitPrice = limitPrice;
    }

    public int getLimitPrice () {
        return limitPrice;
    }    
}
