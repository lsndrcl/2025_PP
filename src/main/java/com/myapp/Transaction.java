package com.myapp;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Represents a financial transaction with a unique ID, timestamp, type, amount, and description.
 */
public class Transaction {
    /** Unique identifier for this transaction */
    private final String transactionId;
    /** Timestamp of when this transaction was created */
    private final LocalDateTime timestamp;
    /** Type of transaction (e.g., DEPOSIT, WITHDRAWAL) */
    private final TransactionType type;
    /** Monetary amount involved in this transaction */
    private final double amount;
    /** Description or note about this transaction */
    private final String description;

    /**
     * Constructs a new Transaction with the specified type, amount, and description.
     * Generates a unique transaction ID and sets the timestamp to the current time.
     *
     * @param type The type of transaction (e.g., DEPOSIT, WITHDRAWAL)
     * @param amount The amount involved in the transaction
     * @param description A brief description of the transaction
     */
    public Transaction(TransactionType type, double amount, String description) {
        this.transactionId = UUID.randomUUID().toString();
        this.timestamp = LocalDateTime.now();
        this.type = type;
        this.amount = amount;
        this.description = description;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public TransactionType getType() {
        return type;
    }

    public double getAmount() {
        return amount;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return "Transaction{" +
                "description='" + description + '\'' +
                ", amount=" + amount +
                '}';
    }
}