package com.myapp;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents a user's crypto portfolio, tracking owned cryptocurrencies and transactions.
 */
public class Portfolio {
    private final Account account;
    private final Map<String, Double> holdings;  // Symbol -> Amount
    private final Map<String, Double> purchasePrices;  // Symbol -> Average Purchase Price

    /**
     * Creates a new portfolio linked to a user account.
     * @param account The user's account for fiat transactions
     */
    public Portfolio(Account account) {
        this.account = account;
        this.holdings = new HashMap<>();
        this.purchasePrices = new HashMap<>();
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
     * Gets a copy of the average purchase prices for each holding.
     * @return Map of crypto symbol to average purchase price
     */
    public Map<String, Double> getPurchasePrices() {
        return new HashMap<>(purchasePrices);
    }
    
    /**
     * Gets the average purchase price for a specific coin.
     * @param symbol The crypto symbol
     * @return The average purchase price, or 0.0 if not found
     */
    public double getAveragePurchasePrice(String symbol) {
        return purchasePrices.getOrDefault(symbol, 0.0);
    }
    
    /**
     * Buys a cryptocurrency using fiat from the linked account.
     * Fetches the current price automatically.
     * 
     * @param symbol The crypto symbol (e.g., "BTC")
     * @param fiatAmount The fiat amount to spend
     * @return The amount of crypto purchased
     * @throws Exception if the price cannot be fetched or the transaction fails
     */
    public double buyCrypto(String symbol, double fiatAmount) throws Exception {
        if (fiatAmount <= 0) throw new IllegalArgumentException("Amount must be positive");
        
        // Get current price
        CryptoService service = new CryptoService();
        double price = service.getCurrentPrices().get(symbol);
        if (price <= 0) throw new IllegalStateException("Invalid price for " + symbol);
        
        // Calculate crypto amount
        double cryptoAmount = fiatAmount / price;
        
        // Execute the purchase
        buyCrypto(symbol, cryptoAmount, price);
        
        return cryptoAmount;
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
        account.withdraw(totalCost, "Purchase of " + amount + " " + symbol, TransactionType.CRYPTO_PURCHASE);
        
        // Update holdings
        double currentAmount = holdings.getOrDefault(symbol, 0.0);
        double currentAvgPrice = purchasePrices.getOrDefault(symbol, 0.0);
        
        // Calculate new average purchase price (weighted average)
        double newTotalAmount = currentAmount + amount;
        double newAvgPrice;
        
        if (currentAmount > 0) {
            // Calculate weighted average: (currentAmount * currentPrice + newAmount * newPrice) / totalAmount
            newAvgPrice = ((currentAmount * currentAvgPrice) + (amount * price)) / newTotalAmount;
        } else {
            newAvgPrice = price;
        }
        
        holdings.put(symbol, newTotalAmount);
        purchasePrices.put(symbol, newAvgPrice);
    }
    
    /**
     * Sells a cryptocurrency, converting to fiat in the linked account.
     * Fetches the current price automatically.
     * 
     * @param symbol The crypto symbol (e.g., "BTC")
     * @param cryptoAmount The amount of crypto to sell
     * @return The fiat amount received
     * @throws Exception if the price cannot be fetched or the transaction fails
     */
    public double sellCrypto(String symbol, double cryptoAmount) throws Exception {
        if (cryptoAmount <= 0) throw new IllegalArgumentException("Amount must be positive");
        
        // Get current price
        CryptoService service = new CryptoService();
        double price = service.getCurrentPrices().get(symbol);
        if (price <= 0) throw new IllegalStateException("Invalid price for " + symbol);
        
        // Calculate fiat value
        double fiatValue = cryptoAmount * price;
        
        // Execute the sale
        sellCrypto(symbol, cryptoAmount, price);
        
        return fiatValue;
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
        account.deposit(totalValue, "Sale of " + amount + " " + symbol, TransactionType.CRYPTO_SALE);
        
        // Update holdings
        double newAmount = currentAmount - amount;
        if (newAmount > 0) {
            holdings.put(symbol, newAmount);
            // Purchase price remains the same when selling
        } else {
            holdings.remove(symbol);
            purchasePrices.remove(symbol);
        }
    }
    
    /**
     * Calculates the total value of the portfolio using current market prices.
     * @return The total portfolio value in fiat
     * @throws Exception if prices cannot be fetched
     */
    public double calculateTotalValue() throws Exception {
        if (holdings.isEmpty()) {
            return 0.0;
        }
        
        CryptoService service = new CryptoService();
        Map<String, Double> prices = service.getCurrentPrices();
        
        double totalValue = 0.0;
        for (Map.Entry<String, Double> entry : holdings.entrySet()) {
            String symbol = entry.getKey();
            double amount = entry.getValue();
            double price = prices.getOrDefault(symbol, 0.0);
            totalValue += (amount * price);
        }
        
        return totalValue;
    }
    
    /**
     * Calculates the profit/loss for a specific coin.
     * @param symbol The crypto symbol
     * @return The profit/loss percentage (positive for profit, negative for loss)
     * @throws Exception if prices cannot be fetched
     */
    public double calculateProfitLossPercent(String symbol) throws Exception {
        if (!holdings.containsKey(symbol) || !purchasePrices.containsKey(symbol)) {
            return 0.0;
        }
        
        CryptoService service = new CryptoService();
        Map<String, Double> prices = service.getCurrentPrices();
        
        double currentPrice = prices.getOrDefault(symbol, 0.0);
        double purchasePrice = purchasePrices.get(symbol);
        
        if (purchasePrice <= 0 || currentPrice <= 0) {
            return 0.0;
        }
        
        return ((currentPrice - purchasePrice) / purchasePrice) * 100.0;
    }
}

