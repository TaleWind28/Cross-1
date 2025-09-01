package com.unipi.lab3.cross.json.request;

public class OrderValues implements Values {
    private String type;
    private int size;
    private int price;

    public OrderValues(String type, int size, int price) {
        this.type = type;
        this.size = size;
        this.price = price;
    }

    public String getType () {
        return type;
    }

    public int getSize () {
        return size;
    }

    public int getPrice () {
        return price;
    }

    public void setType (String type) {
        this.type = type;
    }

    public void setSize (int size) {
        this.size = size;
    }

    public void setPrice (int price) {
        this.price = price;
    }

    public String toString () {
        return "{type: " + this.type + ", size: " + this.size + ", price: " + this.price + "}";
    }

    public String marketToString () {
        return "{type: " + this.type + ", size: " + this.size + "}";
    }

}
