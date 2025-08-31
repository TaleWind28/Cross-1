package com.unipi.lab3.cross.json.response;

public class OrderResponse extends Response{
    private int orderID;

    public OrderResponse(int orderID) {
        this.orderID = orderID;
    }

    public int getOrderID() {
        return orderID;
    }
}
