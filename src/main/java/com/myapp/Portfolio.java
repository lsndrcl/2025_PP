package com.myapp;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents a user's crypto portfolio, tracking owned cryptocurrencies and transactions.
 */
public class Portfolio {
    private final Account account;
    private final Map<String, Double> holdings;  // Symbol -> Amount

    /**
     * Creates a new portfolio linked to a user account.
     * @param account The user's account for fiat transactions
     */
    public Portfolio(Account account) {
        this.account = account;
        this.holdings = new HashMap<>();
    }

    /**
     * Gets the associated account.
     * @return The account associated with this portfolio
     */
    public Account getAccount() {
        return account;
    }

    /**
     * Gets a copy of the current crypto holdings.
     * @return Map of crypto symbol to amount held
     */
    public Map<String, Double> getHoldings() {
        return new HashMap<>(holdings);
    }

    /**
     * Buys a cryptocurrency using fiat from the linked account.
     * @param symbol The crypto symbol (e.g., "BTC")
     * @param amount The amount to buy
     * @param price The price per unit in fiat
     */
    public void buyCrypto(String symbol, double amount, double price) {
        if (amount <= 0) throw new IllegalArgumentException("Amount must be positive");
        if (price <= 0) throw new IllegalArgumentException("Price must be positive");
        
        double totalCost = amount * price;
        account.withdraw(totalCost, "Purchase of " + amount + " " + symbol);
        
        // Update holdings
        holdings.put(symbol, holdings.getOrDefault(symbol, 0.0) + amount);
    }

    /**
     * Sells a cryptocurrency, converting to fiat in the linked account.
     * @param symbol The crypto symbol (e.g., "BTC")
     * @param amount The amount to sell
     * @param price The price per unit in fiat
     */
    public void sellCrypto(String symbol, double amount, double price) {
        if (amount <= 0) throw new IllegalArgumentException("Amount must be positive");
        if (price <= 0) throw new IllegalArgumentException("Price must be positive");
        
        // Check if we have enough of the crypto
        double currentAmount = holdings.getOrDefault(symbol, 0.0);
        if (amount > currentAmount) {
            throw new IllegalStateException("Insufficient " + symbol + " balance");
        }
        
        double totalValue = amount * price;
        account.deposit(totalValue, "Sale of " + amount + " " + symbol);
        
        // Update holdings
        double newAmount = currentAmount - amount;
        if (newAmount > 0) {
            holdings.put(symbol, newAmount);
        } else {
            holdings.remove(symbol);
        }
    }
}

