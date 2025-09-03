package com.unipi.lab3.cross.model.trade;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.time.LocalDate;
import java.time.Year;
import java.time.YearMonth;

public class PriceHistory {

    public PriceHistory () {}

    public ArrayList<DailyTradingStats> getPriceHistory (YearMonth date, TradeMap map) {

        ArrayList<DailyTradingStats> history = new ArrayList<>();

        for (Map.Entry<LocalDate, LinkedList<Trade>> entry : map.getDailyTrades().entrySet()) {
            LocalDate tradeDate = entry.getKey();

            if (YearMonth.from(tradeDate).equals(date)) {

                DailyTradingStats stats = this.calculateDailyTradingStats(tradeDate, map);

                history.add(stats);
            }
        }

        return history;
    }

    public DailyTradingStats calculateDailyTradingStats (LocalDate date, TradeMap map) {
        LinkedList<Trade> trades = map.getTradesByDate(date);

        LinkedList<Trade> sortedTrades = new LinkedList<>(trades);
        sortedTrades.sort(Comparator.comparingLong(Trade::getTimestamp));

        // no trades for that day
        if (sortedTrades.isEmpty())
            return new DailyTradingStats(date, 0, 0, 0, 0);

        int openPrice = sortedTrades.getFirst().getPrice();
        int closePrice = sortedTrades.getLast().getPrice();

        int maxPrice = Collections.max(sortedTrades, Comparator.comparingInt(Trade::getPrice)).getPrice();
        int minPrice = Collections.min(sortedTrades, Comparator.comparingInt(Trade::getPrice)).getPrice();

        return new DailyTradingStats(date, openPrice, closePrice, maxPrice, minPrice);
    }

    
}
