package com.unipi.lab3.cross.model.orders;

public class MarketOrder extends Order{

    public MarketOrder (int orderID, String username, String type, int size) {
        super(orderID, username, type, size);
    }
    
    public String toString () {
        return "Order ID: " + this.getOrderId() + " Username: " + this.getUsername() + " Type: " + this.getType() + " Size: " + this.getSize() + " Market Order";
    }
}
