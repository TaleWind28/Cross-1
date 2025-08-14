package com.unipi.lab3.cross.model.orders;

public class MarketOrder extends Order{

    public MarketOrder (int orderID, String username, String type, String orderType, int size) {
        super(orderID, username, type, orderType, size);
    }
    
}
