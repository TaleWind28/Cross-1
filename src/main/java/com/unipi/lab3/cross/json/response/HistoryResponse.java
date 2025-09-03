package com.unipi.lab3.cross.json.response;

import java.util.ArrayList;
import java.time.YearMonth;

import com.unipi.lab3.cross.model.trade.DailyTradingStats;

public class HistoryResponse extends Response {

    private YearMonth date;
    private ArrayList<DailyTradingStats> stats;

    public HistoryResponse() {}

    public HistoryResponse(YearMonth date, ArrayList<DailyTradingStats> stats) {
        this.date = date;
        this.stats = new ArrayList<>(stats);
    }

    public YearMonth getDate() {
        return date;
    }

    public ArrayList<DailyTradingStats> getStats() {
        return stats;
    }
}
