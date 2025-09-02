package com.unipi.lab3.cross.model;

import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.Iterator;
import java.util.Map;

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
        this.stopAsks = new ConcurrentLinkedQueue<>();
        this.stopBids = new ConcurrentLinkedQueue<>();

        updateBestPrices();
    }

    public ConcurrentSkipListMap<Integer, OrderGroup> getLimitAsks () {
        return this.askOrders;
    }

    public ConcurrentSkipListMap<Integer, OrderGroup> getLimitBids () {
        return this.bidOrders;
    }

    public ConcurrentLinkedQueue<StopOrder> getStopAsks () {
        return this.stopAsks;
    }

    public ConcurrentLinkedQueue<StopOrder> getStopBids () {
        return this.stopBids;
    }

    public int getSpread () {
        return this.spread;
    }

    public int getBestAskPrice () {
        return this.bestAskPrice;
    }

    public int getBestBidPrice () {
        return this.bestBidPrice;
    }

    public int counterOrderId () {
        return idCounter.getAndIncrement();
    }

    public int getAsksSize () {
        int totalSize = 0;

        for (OrderGroup group : this.askOrders.values()) {
            totalSize += group.getSize();
        }

        return totalSize;
    }

    public int getBidsSize () {
        int totalSize = 0;

        for (OrderGroup group : this.bidOrders.values()) {
            totalSize += group.getSize();
        }

        return totalSize;
    }

    public int getAvailableSize (String username, String type) {
        int availableSize = 0;

        ConcurrentSkipListMap<Integer, OrderGroup> selectedMap = type.equals("ask") ? this.askOrders : this.bidOrders;

        for (OrderGroup group : selectedMap.values()) {
            availableSize += group.getFilteredSize(username);
        }

        return availableSize;
    }
 
    private void updateBestPrices () {
        int oldAsk = this.bestAskPrice;
        int oldBid = this.bestBidPrice;
        
        // update best prices only if the maps aren't empty
        this.bestAskPrice = this.askOrders.isEmpty() ? 0 : this.askOrders.firstKey();
        this.bestBidPrice = this.bidOrders.isEmpty() ? 0 : this.bidOrders.firstKey();

        // both best prices must be valid to have spread
        if (this.bestAskPrice > 0 && this.bestBidPrice > 0) {
            this.spread = this.bestAskPrice - this.bestBidPrice;
        } 
        else {
            // if one of the prices is 0, spread is invalid
            this.spread = -1;
        }

        // execute stop orders if the map changed
        if (oldAsk != this.bestAskPrice || oldBid != this.bestBidPrice) {
            execStopOrders();
        }
    }

    // methods for execute a limit order
    public int execLimitOrder (String username, String type, int size, int price) {

        // check the type of the order
        if (type.equals("ask")) {
            return execAskOrder(username, size, price);
        }
        else if (type.equals("bid")) {
            return execBidOrder(username, size, price);
        }

        return -1;
    }

    // ask order
    public int execAskOrder (String username, int size, int price) {

        // create new order id
        int orderId = counterOrderId(); // generate unique order id

        int newSize = size;

        // try to match with existing bid orders
        Iterator<Map.Entry<Integer, OrderGroup>> iterator = this.bidOrders.entrySet().iterator();

        // iterate on bid list
        while (iterator.hasNext() && newSize > 0) {
            Map.Entry<Integer, OrderGroup> entry = iterator.next();

            Integer bidPrice = entry.getKey();
            OrderGroup bidGroup = entry.getValue();

            ConcurrentLinkedQueue<LimitOrder> bidLimitOrders = bidGroup.getLimitOrders();

            // check price condition
            if (bidPrice >= price) {
                newSize = matchingAlgorithm(bidGroup, bidLimitOrders, newSize, username);

                if (bidGroup.isEmpty()) {
                    // remove the group if it's empty
                    iterator.remove();
                }

            }
        }

        if (newSize == 0) {
            updateBestPrices();

            // order fully executed
            return orderId;
        }
        else if (newSize > 0) {
            // order not matched or partially executed
            // add the ask order to the order book

            if (newSize == size) {
                // order not matched
            }
            else {
                // order partially executed
            }
            addLimitOrder(orderId, "ask", username, newSize, price);
        }

        return orderId;
    }

    // bid order
    public int execBidOrder (String username, int size, int price) {
        // create new order id
        int orderId = counterOrderId(); // generate unique order id

        int newSize = size;

        // try to match with existing bid orders

        Iterator<Map.Entry<Integer, OrderGroup>> iterator = this.askOrders.entrySet().iterator();

        // iterate on bid list
        while (iterator.hasNext() && newSize > 0) {
            Map.Entry<Integer, OrderGroup> entry = iterator.next();

            // check price condition
            int askPrice = entry.getKey();
            OrderGroup askGroup = entry.getValue();

            ConcurrentLinkedQueue<LimitOrder> askLimitOrders = askGroup.getLimitOrders();

            if (askPrice <= price) {
                
                newSize = matchingAlgorithm(askGroup, askLimitOrders, newSize, username);

                if (askGroup.isEmpty()) {
                    iterator.remove();
                }        
            }
        }

        if (newSize == 0) {
            updateBestPrices();
            // order fully executed
            return orderId;
        }
        else if (newSize > 0) {
            // order not matched or partially executed
            // add the ask order to the order book

            if (newSize == size) {
                // order not matched
            }
            else {
                // order partially executed
            }

            addLimitOrder(orderId, "bid", username, newSize, price);
        }

        return orderId;
    }

    public int matchingAlgorithm (OrderGroup group, ConcurrentLinkedQueue<LimitOrder> orders, int size, String username) {

        // check in the order group
        Iterator<LimitOrder> iterator = orders.iterator();

        while (iterator.hasNext()) {
            // check for every order in the list
            LimitOrder order = iterator.next();

            if (!order.getUsername().equals(username)) {
                int orderSize = order.getSize();
                int orderPrice = order.getLimitPrice();

                if (orderSize < size) {
                    // order partially executed but opposite order executed
                    size -= orderSize;

                    // remove the opposite order from the group
                    iterator.remove();
                            
                    // update the group size e total
                    group.updateGroup(orderSize, orderPrice);
                }
                else if (orderSize == size) {
                    // both orders are fully executed

                    // remove the opposite order from the group
                    iterator.remove();
                    group.updateGroup(orderSize, orderPrice);

                    return 0;
                }
                else if (orderSize > size) {
                    // opposite order partially executed
                    order.setSize(orderSize - size);
                    
                    group.updateGroup(size, orderPrice);

                    // order fully executed
                    return 0;
                }
            }

        }

        return size;
    }

    // method to add a limit order
    public void addLimitOrder (int orderId, String username, String type, int size, int price) {
        // create a new LimitOrder object
        LimitOrder order = new LimitOrder(orderId, username, type, size, price);

        // select the right map
        ConcurrentSkipListMap<Integer, OrderGroup> selectedMap = type.equals("ask") ? this.askOrders : this.bidOrders;

        if (selectedMap.containsKey(price)) {
            // if the price already exists, add the order to the OrderGroup
            OrderGroup group = selectedMap.get(price);

            group.addOrder(order);
        } 
        else {
            // if the price doesn't exist, create a new OrderGroup
            OrderGroup newGroup = new OrderGroup();

            newGroup.addOrder(order);

            selectedMap.put(price, newGroup);
        }

        // update best prices + spread
        updateBestPrices();
    }

    // method to add a stop order
    public int addStopOrder (String username, int size, int price, String type) {
        // create new order id
        int orderId = counterOrderId(); // generate unique order id

        // create a new StopOrder object
        StopOrder order = new StopOrder(orderId, username, type, size, price);

        ConcurrentLinkedQueue<StopOrder> selectedQueue = type.equals("ask") ? this.stopAsks : this.stopBids;

        // add the stop order to the stop orders queue
        selectedQueue.add(order);

        return orderId;
    }

    // periodic check to match stop orders, executed when spread and best prices change
    public void execStopOrders () {
        // check stop asks with bid map
        // bestAskPrice is the lowest ask price
        // stopAsks active when bestBidPrice <= stopAskPrice

        Iterator<StopOrder> askIterator = this.stopAsks.iterator();

        while (askIterator.hasNext()) {
            StopOrder order = askIterator.next();
            int stopPrice = order.getStopPrice();

            if (!this.bidOrders.isEmpty() && this.bestBidPrice <= stopPrice) {
                // activate the stop order -> execute it as a market order

                int result = execMarketOrder (order.getSize(), "ask", "stop", order.getUsername(), order.getOrderId());

                if (result == order.getOrderId()) {
                    // order executed successfully

                    // remove stop order from the queue
                    askIterator.remove();
                }
                else {
                    // order not executed

                    askIterator.remove();
                }
            }
        }

        // check stop bids with ask map

        Iterator<StopOrder> bidIterator = this.stopBids.iterator();

        while (bidIterator.hasNext()) {
            StopOrder order = bidIterator.next();
            int stopPrice = order.getStopPrice();

            if (!this.askOrders.isEmpty() && this.bestAskPrice >= stopPrice) {

                int result = execMarketOrder (order.getSize(), "bid", "stop", order.getUsername(), order.getOrderId());

                if (result == order.getOrderId()) {
                    // order executed successfully

                    bidIterator.remove(); // remove stop order from the queue

                }
                else {
                    // order not executed

                    bidIterator.remove(); // remove stop order from the queue
                }

            }
        }

    }

    public int execMarketOrder (int size, String type, String orderType, String username, int id) {

        int orderId = 0;
        
        if (orderType.equals("stop")) {
            orderId = id;
        }
        else if (orderType.equals("market")) {
            orderId = counterOrderId(); // generate unique order id
        }

        String oppositeType = type.equals("ask") ? "bid" : "ask";

        ConcurrentSkipListMap<Integer, OrderGroup> selectedMap = type.equals("ask") ? this.bidOrders : this.askOrders;

        // check if the map is empty
        if (selectedMap.isEmpty()) {
            // failed order
            return -1;
        }

        // check if there's enough size to execute the order
        if (this.getAvailableSize(username, oppositeType) >= size) {
            // select the right map based on the order type

            int newSize = size;

            Iterator<Map.Entry<Integer, OrderGroup>> iterator = selectedMap.entrySet().iterator();

            while (iterator.hasNext() && newSize > 0) {
                Map.Entry<Integer, OrderGroup> entry = iterator.next();

                OrderGroup group = entry.getValue();

                ConcurrentLinkedQueue<LimitOrder> orders = group.getLimitOrders();

                newSize = matchingAlgorithm(group, orders, newSize, username);
                
                if (group.isEmpty()) {
                    // remove the group if it's empty
                    iterator.remove();
                }
            }

            updateBestPrices();

            return orderId;
        }
     
        // failed order
        return -1;
    }

    // method for removing an order
    public int cancelOrder (int orderId, String username) {

        // check if the order is in the ask map
        Iterator<OrderGroup> askIterator = this.askOrders.values().iterator();

        while (askIterator.hasNext()) {
            OrderGroup group = askIterator.next();

            if (group.removeOrder(orderId, username)) {

                if (group.isEmpty()) {
                    askIterator.remove();
                }

                updateBestPrices();

                // if the order has been removed successfully, return 100
                return 100;
            }
        }

        // check if the order is in the bid map
        Iterator<OrderGroup> bidIterator = this.bidOrders.values().iterator();

        while (bidIterator.hasNext()) {
            OrderGroup group = bidIterator.next();

            if (group.removeOrder(orderId, username)) {

                if (group.isEmpty()) {
                    bidIterator.remove();
                }

                updateBestPrices();
                return 100;
            }
        }

        // check if the order is in the stop ask queue
        Iterator<StopOrder> stopAskIterator = this.stopAsks.iterator();
        
        while (stopAskIterator.hasNext()) {
            StopOrder stopOrder = stopAskIterator.next();

            if (stopOrder.getOrderId() == orderId && stopOrder.getUsername().equals(username)) {
                // remove the stop order
                stopAskIterator.remove();

                updateBestPrices();

                return 100; // success
            }
        }

        // check if the order is in the stop bid queue
        Iterator<StopOrder> stopBidIterator = this.stopBids.iterator();
        while (stopBidIterator.hasNext()) {
            StopOrder stopOrder = stopBidIterator.next();

            if (stopOrder.getOrderId() == orderId && stopOrder.getUsername().equals(username)) {
                stopBidIterator.remove();

                updateBestPrices();

                return 100;
            }
        }

        // error -> order not found or not removed
        return 101;
    }

    public void printOrderBook () {
        System.out.println("Asks:");
        for (Map.Entry<Integer, OrderGroup> entry : this.askOrders.entrySet()) {
            Integer price = entry.getKey();
            OrderGroup group = entry.getValue();

            System.out.println("Price: " + price + ", Size: " + group.getSize() + ", Total: " + group.getTotal());
        }

        System.out.println("Spread: " + this.spread);

        System.out.println("Bids:");
        for (Map.Entry<Integer, OrderGroup> entry : this.bidOrders.entrySet()) {
            Integer price = entry.getKey();
            OrderGroup group = entry.getValue();

            System.out.println("Price: " + price + ", Size: " + group.getSize() + ", Total: " + group.getTotal());
        }
    }
}