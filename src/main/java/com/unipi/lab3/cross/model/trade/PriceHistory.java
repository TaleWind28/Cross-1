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

    public void printPriceHistory (ArrayList<DailyTradingStats> history) {
        System.out.printf("%-12s %-12s %-12s %-12s %-12s%n", "Date", "Open", "Close", "Max", "Min");
        System.out.println("---------------------------------------------------------------");
        for (DailyTradingStats stats : history) {
            if (stats.getOpenPrice() == 0 && stats.getClosePrice() == 0 && stats.getMaxPrice() == 0 && stats.getMinPrice() == 0)
                continue;
            else
                System.out.printf("%-12s %-12.2f %-12.2f %-12.2f %-12.2f%n", stats.getDate().toString(), stats.getOpenPrice()/1000, stats.getClosePrice()/1000, stats.getMaxPrice()/1000, stats.getMinPrice()/1000);
        }
    } 
}
