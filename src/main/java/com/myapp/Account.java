package com.myapp;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Represents a user's fiat account, tracking balance and transaction history.
 */
public class Account {
    private double balance;
    private final List<com.myapp.Transaction> transactions;

    /**
     * Creates a new empty account.
     */
    public Account() {
        this.balance = 0.0;
        this.transactions = new ArrayList<>();
    }

    public double getBalance() {
        return balance;
    }

    public List<com.myapp.Transaction> getTransactions() {
        return new ArrayList<>(transactions);
    }

    /**
     * Deposits an amount into the account.
     * @param amount Amount to deposit
     * @param description Description of the transaction
     */
    public void deposit(double amount, String description) {
        if (amount <= 0) throw new IllegalArgumentException("Deposit amount must be positive.");
        balance += amount;
        transactions.add(new com.myapp.Transaction(com.myapp.TransactionType.DEPOSIT, amount, description));
    }

    /**
     * Withdraws an amount from the account.
     * @param amount Amount to withdraw
     * @param description Description of the transaction
     */
    public void withdraw(double amount, String description) {
        if (amount <= 0) throw new IllegalArgumentException("Withdrawal amount must be positive.");
        if (amount > balance) throw new IllegalStateException("Insufficient balance.");
        balance -= amount;
        transactions.add(new Transaction(TransactionType.WITHDRAWAL, amount, description));
    }

    public void deposit(double amount, String description, TransactionType type) {
        if (amount <= 0) throw new IllegalArgumentException("Deposit amount must be positive.");
        balance += amount;
        transactions.add(new Transaction(type, amount, description));
    }

    public void withdraw(double amount, String description, TransactionType type) {
        if (amount <= 0) throw new IllegalArgumentException("Withdrawal amount must be positive.");
        if (amount > balance) throw new IllegalStateException("Insufficient balance.");
        balance -= amount;
        transactions.add(new Transaction(type, amount, description));
    }

    /**
     * Returns the N most recent transactions.
     * @param count Number of transactions to return
     * @return List of recent transactions
     */
    public List<Transaction> getRecentTransactions(int count) {
        return transactions.stream()
                .skip(Math.max(0, transactions.size() - count))
                .collect(Collectors.toList());
    }

    /**
     * Searches transactions using a custom filter.
     * @param filter A com.myapp.UI.TransactionFilter predicate
     * @return List of matching transactions
     */
    public List<Transaction> searchTransactions(TransactionFilter filter) {
        return transactions.stream()
                .filter(filter::matches)
                .collect(Collectors.toList());
    }
    
    /**
     * Imports transactions from a JSON file and updates the account balance accordingly.
     * The JSON file should contain an array of transaction objects with the following structure:
     * {
     *   "type": "DEPOSIT" | "WITHDRAWAL" | "TRANSFER" | "CRYPTO_PURCHASE" | "CRYPTO_SALE",
     *   "amount": 100.0,
     *   "description": "Transaction description"
     * }
     * 
     * @param filePath Path to the JSON file
     * @throws IOException If the file cannot be read or parsed
     */
    public void importTransactionsFromJson(String filePath) throws IOException {
        String jsonContent;
        try {
            // Use DataManager to load the JSON data
            Path path = Paths.get(filePath);
            jsonContent = DataManager.loadJsonData(path);
        } catch (IOException e) {
            throw new IOException("Failed to load transactions from " + filePath + ": " + e.getMessage());
        }
        
        JSONArray jsonArray = new JSONArray(jsonContent);
        
        // Store the original balance and transactions for rollback in case of error
        double originalBalance = this.balance;
        List<Transaction> originalTransactions = new ArrayList<>(this.transactions);
        
        try {
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject txJson = jsonArray.getJSONObject(i);
                String typeStr = txJson.getString("type");
                double amount = txJson.getDouble("amount");
                String description = txJson.getString("description");
                
                TransactionType type = TransactionType.valueOf(typeStr);
                
                // Update balance based on transaction type
                switch (type) {
                    case DEPOSIT, CRYPTO_SALE -> balance += amount;
                    case WITHDRAWAL, CRYPTO_PURCHASE -> {
                        if (amount > balance) {
                            throw new IllegalStateException("Insufficient balance for transaction: " + description);
                        }
                        balance -= amount;
                    }
                    case TRANSFER -> {
                        // For transfers, the description should indicate direction
                        if (description.startsWith("Incoming")) {
                            balance += amount;
                        } else if (description.startsWith("Outgoing")) {
                            if (amount > balance) {
                                throw new IllegalStateException("Insufficient balance for transaction: " + description);
                            }
                            balance -= amount;
                        } else {
                            throw new IllegalArgumentException("Transfer description must start with 'Incoming' or 'Outgoing'");
                        }
                    }
                }
                
                // Add the transaction to the history
                transactions.add(new Transaction(type, amount, description));
            }
        } catch (Exception e) {
            // Rollback to original state if any transaction fails
            this.balance = originalBalance;
            this.transactions.clear();
            this.transactions.addAll(originalTransactions);
            throw e; // Re-throw the exception
        }
    }
    
    /**
     * Exports all transactions to a JSON file.
     * 
     * @param filePath Path to save the JSON file
     * @throws IOException If the file cannot be written
     */
    public void exportTransactionsToJson(String filePath) throws IOException {
        JSONArray jsonArray = new JSONArray();
        
        for (Transaction tx : transactions) {
            JSONObject txJson = new JSONObject();
            txJson.put("transactionId", tx.getTransactionId());
            txJson.put("timestamp", tx.getTimestamp().toString());
            txJson.put("type", tx.getType().toString());
            txJson.put("amount", tx.getAmount());
            txJson.put("description", tx.getDescription());
            
            jsonArray.put(txJson);
        }
        
        // Use DataManager to save the JSON data
        Path path = Paths.get(filePath);
        DataManager.saveJsonData(jsonArray, path);
    }
}

