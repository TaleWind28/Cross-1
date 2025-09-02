package com.unipi.lab3.cross.json.response;

import com.unipi.lab3.cross.json.request.*;

public class OrderResponse extends Response implements Values{
    private int orderID;

    public OrderResponse() {}

    public OrderResponse(int orderID) {
        this.orderID = orderID;
    }

    public int getOrderID() {
        return orderID;
    }
}
