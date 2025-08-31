package com.unipi.lab3.cross.json.request;

public class HistoryValues implements Values {
    private int month;
    private int year;


    public HistoryValues (int month, int year) {
        this.month = month;
        this.year = year;
    }

    public int getMonth () {
        return month;
    }

    public int getYear () {
        return year;
    }

    public void setMonth (int month) {
        this.month = month;
    }

    public void setYear (int year) {
        this.year = year;
    }

    public String toString () {
        return "{month: " + this.month + "}";
    }
    
}
