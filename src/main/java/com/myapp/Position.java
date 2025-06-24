package com.myapp;

/**
 * Represents a trading position for a specific cryptocurrency.
 * Contains details about the coin symbol, amount held,
 * entry price, position type (LONG or SHORT), and timestamp of the position.
 */
public class Position {
    private String symbol;
    private double amount;
    private double entryPrice;
    private PositionType type;
    private long timestamp;

    /**
     * Constructs a new Position.
     *
     * @param symbol     The symbol of the cryptocurrency (e.g., "BTC").
     * @param amount     The amount of the cryptocurrency held.
     * @param entryPrice The price at which the position was opened.
     * @param type       The type of position (LONG or SHORT).
     * @param timestamp  The timestamp (epoch milliseconds) when the position was created.
     */
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