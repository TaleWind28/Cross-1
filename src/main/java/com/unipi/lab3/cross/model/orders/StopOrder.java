package com.unipi.lab3.cross.model.orders;

public class StopOrder extends Order{

    private int stopPrice;

    public StopOrder (int orderID, String username, String type, int size, int stopPrice) {
        super(orderID, username, type, size);
        this.stopPrice = stopPrice;
    }

    public int getStopPrice () {
        return stopPrice;
    }

}
