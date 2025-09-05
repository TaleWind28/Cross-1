package com.unipi.lab3.cross.json.response;

import com.unipi.lab3.cross.model.OrderBook;

public class OrderBookResponse extends Response {
    
    OrderBook orderBook;

    public OrderBookResponse() {}

    public OrderBookResponse(OrderBook orderBook) {
        this.orderBook = orderBook;
    }

    public OrderBook getOrderBook() {
        return orderBook;
    }    
}
