package com.myapp;

public class Position {
    private String symbol;
    private double amount;
    private double entryPrice;
    private PositionType type;
    private long timestamp;

    public Position(String symbol, double amount, double entryPrice, PositionType type, long timestamp) {
        this.symbol = symbol;
        this.amount = amount;
        this.entryPrice = entryPrice;
        this.type = type;
        this.timestamp = timestamp;
    }

    public String getSymbol() { return symbol; }
    public double getAmount() { return amount; }
    public double getEntryPrice() { return entryPrice; }
    public PositionType getType() { return type; }
    public long getTimestamp() { return timestamp; }

    public void setAmount(double amount) {
        this.amount = amount;
    }



}