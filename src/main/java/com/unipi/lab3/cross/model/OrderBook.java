package com.unipi.lab3.cross.model;

import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.Iterator;
import java.util.Map;
import java.util.Collections;

import java.util.concurrent.atomic.AtomicInteger;

import com.unipi.lab3.cross.model.orders.*;

public class OrderBook {

    // map price - list ask limit orders
    // ascending order for keys
    private ConcurrentSkipListMap<Integer, OrderGroup> askOrders;
    
    // map price - list bid limit orders
    // descending order for keys
    private ConcurrentSkipListMap<Integer, OrderGroup> bidOrders;

    // stop orders
    private ConcurrentLinkedQueue<StopOrder> stopAsks;
    private ConcurrentLinkedQueue<StopOrder> stopBids;

    private int spread;

    private int bestAskPrice;
    private int bestBidPrice;

    private static final AtomicInteger idCounter = new AtomicInteger(100);

    public OrderBook (ConcurrentSkipListMap<Integer, OrderGroup> askOrders, ConcurrentSkipListMap<Integer, OrderGroup> bidOrders, int spread, int bestAskPrice, int bestBidPrice) {
        this.askOrders = askOrders;
        this.bidOrders = bidOrders;
        this.spread = spread;
        this.bestAskPrice = bestAskPrice;
        this.bestBidPrice = bestBidPrice;
    }

    private ConcurrentSkipListMap<Integer, OrderGroup> getLimitAsks () {
        return this.askOrders;
    }

    private ConcurrentSkipListMap<Integer, OrderGroup> getLimitBids () {
        return this.bidOrders;
    }

    private ConcurrentLinkedQueue<StopOrder> getStopAsks () {
        return this.stopAsks;
    }

    private ConcurrentLinkedQueue<StopOrder> getStopBids () {
        return this.stopBids;
    }

    private int getSpread () {
        return this.spread;
    }

    private int getBestAskPrice () {
        return this.bestAskPrice;
    }

    private int getBestBidPrice () {
        return this.bestBidPrice;
    }

    private int counterOrderId () {
        return idCounter.getAndIncrement();
    }
}