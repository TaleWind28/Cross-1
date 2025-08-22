package com.unipi.lab3.cross.main;

import com.unipi.lab3.cross.model.*;
import com.unipi.lab3.cross.model.orders.*;

import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.Iterator;
import java.util.Map;
import java.util.Comparator;

public class ServerMain {
    public static void main(String[] args) {

        // orders
        /*LimitOrder lo1 = new LimitOrder(1, "mario", "ask", 50, 100);

        LimitOrder lo2 = new LimitOrder(2, "luigi", "ask", 30, 100);

        LimitOrder lo3 = new LimitOrder(3, "peach", "ask", 20, 90);

        LimitOrder lo4 = new LimitOrder(4, "yoshi", "bid", 40, 80);

        LimitOrder lo5 = new LimitOrder(5, "toad", "bid", 60, 80);

        LimitOrder lo6 = new LimitOrder(6, "mario", "bid", 10, 70);

        OrderGroup og1 = new OrderGroup();

        og1.addOrder(lo1);
        og1.addOrder(lo2);

        OrderGroup og2 = new OrderGroup();
        og2.addOrder(lo3);

        OrderGroup og3 = new OrderGroup();
        og3.addOrder(lo4);
        og3.addOrder(lo5);

        OrderGroup og4 = new OrderGroup();
        og4.addOrder(lo6);

        ConcurrentSkipListMap<Integer, OrderGroup> askOrders = new ConcurrentSkipListMap<>();
        askOrders.put(100, og1);
        askOrders.put(90, og2);

        ConcurrentSkipListMap<Integer, OrderGroup> bidOrders = new ConcurrentSkipListMap<>(Comparator.reverseOrder());
        bidOrders.put(80, og3);
        bidOrders.put(70, og4);


        OrderBook orderBook = new OrderBook(askOrders, bidOrders, 0, 0, 0);

        orderBook.addLimitOrder(7, "mario", "ask", 70, 90);

        System.out.println("order Book:");

        orderBook.printOrderBook();

        System.out.println("best ask: " + orderBook.getBestAskPrice());
        System.out.println("best bid: " + orderBook.getBestBidPrice());        

        System.out.println("executing limit order: luigi, bid, size 50, limit price 95");

        orderBook.execLimitOrder("luigi", 120, 95, "bid");


        System.out.println("order Book after bid order:");

        orderBook.printOrderBook();

        System.out.println("best ask: " + orderBook.getBestAskPrice());
        System.out.println("best bid: " + orderBook.getBestBidPrice());

        // stop orders execution

        orderBook.addStopOrder("mario", 40, 30, "ask");
        orderBook.addStopOrder("peach", 30, 70, "bid");
        orderBook.addStopOrder("yoshi", 20, 90, "ask");
        orderBook.addStopOrder("toad", 10, 60, "bid");

        orderBook.getStopAsks().forEach(stopOrder -> {
            System.out.println("stop ask order - ID: " + stopOrder.getOrderId() + ", Username: " + stopOrder.getUsername() + ", Size: " + stopOrder.getSize() + ", Stop Price: " + stopOrder.getStopPrice());
        });
        orderBook.getStopBids().forEach(stopOrder -> {
            System.out.println("stop bid order - ID: " + stopOrder.getOrderId() + ", Username: " + stopOrder.getUsername() + ", Size: " + stopOrder.getSize() + ", Stop Price: " + stopOrder.getStopPrice());
        });

        orderBook.execStopOrders();

        System.out.println("order book after stop orders:");

        orderBook.printOrderBook();

        System.out.println("best ask: " + orderBook.getBestAskPrice());
        System.out.println("best bid: " + orderBook.getBestBidPrice());

        orderBook.getStopAsks().forEach(stopOrder -> {
            System.out.println("stop ask order - ID: " + stopOrder.getOrderId() + ", Username: " + stopOrder.getUsername() + ", Size: " + stopOrder.getSize() + ", Stop Price: " + stopOrder.getStopPrice());
        });
        orderBook.getStopBids().forEach(stopOrder -> {
            System.out.println("stop bid order - ID: " + stopOrder.getOrderId() + ", Username: " + stopOrder.getUsername() + ", Size: " + stopOrder.getSize() + ", Stop Price: " + stopOrder.getStopPrice());
        });

        // market orders execution

        orderBook.execMarketOrder(150, "ask", "market", "costi", 0);


        System.out.println("order book after market order:");
        orderBook.printOrderBook();
        System.out.println("best ask: " + orderBook.getBestAskPrice());
        System.out.println("best bid: " + orderBook.getBestBidPrice());*/
        

        // self trading case -> negative spread ???

        /*LimitOrder lo1 = new LimitOrder(1, "mario", "ask", 50, 100);

        LimitOrder lo2 = new LimitOrder(2, "mario", "bid", 30, 90);

        OrderGroup og1 = new OrderGroup();
        og1.addOrder(lo1);

        OrderGroup og2 = new OrderGroup();
        og2.addOrder(lo2);

        ConcurrentSkipListMap<Integer, OrderGroup> askOrders = new ConcurrentSkipListMap<>();
        askOrders.put(100, og1);

        ConcurrentSkipListMap<Integer, OrderGroup> bidOrders = new ConcurrentSkipListMap<>(Comparator.reverseOrder());
        bidOrders.put(90, og2);

        OrderBook orderBook = new OrderBook(askOrders, bidOrders, 0, 0, 0);

        System.out.println("Order Book:");
        orderBook.printOrderBook();
        System.out.println("best ask: " + orderBook.getBestAskPrice());
        System.out.println("best bid: " + orderBook.getBestBidPrice());

        orderBook.execLimitOrder("mario", 50, 101, "bid");

        System.out.println("Order Book after bid order:");
        orderBook.printOrderBook();
        System.out.println("best ask: " + orderBook.getBestAskPrice());
        System.out.println("best bid: " + orderBook.getBestBidPrice());*/


    }
}
