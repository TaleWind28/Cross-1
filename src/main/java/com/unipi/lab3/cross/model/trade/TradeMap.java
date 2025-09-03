package com.unipi.lab3.cross.model.trade;

import java.util.TreeMap;
import java.time.LocalDate;
import java.util.*;

public class TradeMap {
    
    private TreeMap<LocalDate, LinkedList<Trade>> dailyTrades;

    public TradeMap() {
        this.dailyTrades = new TreeMap<>();
    }

    public TradeMap(TreeMap<LocalDate, LinkedList<Trade>> dailyTrades) {
        this.dailyTrades = dailyTrades;
    }

    public TreeMap<LocalDate, LinkedList<Trade>> getDailyTrades() {
        return this.dailyTrades;
    }

    public LinkedList<Trade> getTradesByDate(LocalDate date) {
        if (!this.dailyTrades.containsKey(date)) {
            return new LinkedList<>();
        }

        return this.dailyTrades.get(date);
    }

    public Trade getTrade (int tradeId) {
        for (Map.Entry<LocalDate, LinkedList<Trade>> entry : this.dailyTrades.entrySet()) {
            for (Trade trade : entry.getValue()) {
                if (trade.getOrderId() == tradeId) {
                    return trade;
                }
            }
        }

        return null;
    }

    public void addTrade(LocalDate date, Trade trade) {
        if (!this.dailyTrades.containsKey(date)) {
            LinkedList<Trade> trades = new LinkedList<>();
            trades.addFirst(trade);
            this.dailyTrades.put(date, trades);
        }
        else {
            this.dailyTrades.get(date).addFirst(trade);
        }
    }  
}