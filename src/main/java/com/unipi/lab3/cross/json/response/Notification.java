package com.unipi.lab3.cross.json.response;

import java.util.*;

import com.unipi.lab3.cross.model.trade.Trade;

public class Notification {
    private static final String NOTIFICATION_TYPE = "closedTrades";

    private final String notification = NOTIFICATION_TYPE;
    private final LinkedList<Trade> trades;

    public Notification (LinkedList<Trade> trades) {
        this.trades = new LinkedList<>(trades);
    }

    public String getNotification () {
        return this.notification;
    }

    public LinkedList<Trade> getTrades () {
        return this.trades;
    }

}