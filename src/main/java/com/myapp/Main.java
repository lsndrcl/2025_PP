package com.myapp;

import com.myapp.auth.UserManager;

import java.util.List;
import java.util.Map;
import java.util.Scanner;

/**
 * Main application entry point with a console-based user interface.
 */
public class Main {
    private static final Scanner scanner = new Scanner(System.in);
    private static final UserManager userManager = new UserManager();
    private static User currentUser = null;
    
    public static void main(String[] args) {
        System.out.println("Welcome to the Java Bank Crypto Application!");
        
        while (true) {
            if (currentUser == null) {
                showLoginMenu();
            } else {
                showMainMenu();
            }
        }
    }
    
    /**
     * Displays the login/signup menu.
     */
    private static void showLoginMenu() {
        System.out.println("\n===== Authentication =====");
        System.out.println("1. Login");
        System.out.println("2. Register");
        System.out.println("3. Exit");
        System.out.print("Choose an option (1-3): ");
        
        String choice = scanner.nextLine();
        
        switch (choice) {
            case "1":
                login();
                break;
            case "2":
                register();
                break;
            case "3":
                System.out.println("Thank you for using Java Bank Crypto Application!");
                System.exit(0);
                break;
            default:
                System.out.println("Invalid option. Please try again.");
        }
    }
    
    /**
     * Displays the main application menu for logged-in users.
     */
    private static void showMainMenu() {
        System.out.println("\n===== Main Menu =====");
        System.out.println("Welcome, " + currentUser.getUsername() + "!");
        System.out.println("Account Balance: $" + String.format("%.2f", currentUser.getAccount().getBalance()));
        System.out.println();
        System.out.println("1. View Account Details");
        System.out.println("2. Deposit Funds");
        System.out.println("3. Withdraw Funds");
        System.out.println("4. View Transaction History");
        System.out.println("5. View Portfolio");
        System.out.println("6. Trade Cryptocurrency");
        System.out.println("7. Logout");
        System.out.println("8. Exit");
        System.out.print("Choose an option (1-8): ");
        
        String choice = scanner.nextLine();
        
        try {
            switch (choice) {
                case "1":
                    viewAccountDetails();
                    break;
                case "2":
                    depositFunds();
                    break;
                case "3":
                    withdrawFunds();
                    break;
                case "4":
                    viewTransactionHistory();
                    break;
                case "5":
                    viewPortfolio();
                    break;
                case "6":
                    tradeCrypto();
                    break;
                case "7":
                    logout();
                    break;
                case "8":
                    saveAndExit();
                    break;
                default:
                    System.out.println("Invalid option. Please try again.");
            }
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
    
    /**
     * Handles the login process.
     */
    private static void login() {
        System.out.println("\n===== Login =====");
        System.out.print("Username: ");
        String username = scanner.nextLine();
        System.out.print("Password: ");
        String password = scanner.nextLine();
        
        User user = userManager.loginUser(username, password);
        
        if (user != null) {
            currentUser = user;
            System.out.println("Login successful!");
        } else {
            System.out.println("Invalid username or password. Please try again.");
        }
    }
    
    /**
     * Handles the registration process.
     */
    private static void register() {
        System.out.println("\n===== Register =====");
        System.out.print("Username: ");
        String username = scanner.nextLine();
        System.out.print("Password: ");
        String password = scanner.nextLine();
        System.out.print("Confirm Password: ");
        String confirmPassword = scanner.nextLine();
        
        if (!password.equals(confirmPassword)) {
            System.out.println("Passwords do not match. Please try again.");
            return;
        }
        
        try {
            User newUser = userManager.registerUser(username, password);
            
            if (newUser != null) {
                System.out.println("Registration successful! You can now login.");
            } else {
                System.out.println("Username already exists. Please try a different username.");
            }
        } catch (Exception e) {
            System.out.println("Error during registration: " + e.getMessage());
        }
    }
    
    /**
     * Logs out the current user.
     */
    private static void logout() {
        try {
            userManager.saveUsers();
            currentUser = null;
            System.out.println("Logout successful!");
        } catch (Exception e) {
            System.out.println("Error during logout: " + e.getMessage());
        }
    }
    
    /**
     * Saves all user data and exits the application.
     */
    private static void saveAndExit() {
        try {
            userManager.saveUsers();
            System.out.println("Thank you for using Java Bank Crypto Application!");
            System.exit(0);
        } catch (Exception e) {
            System.out.println("Error saving data: " + e.getMessage());
        }
    }
    
    /**
     * Displays account details.
     */
    private static void viewAccountDetails() {
        System.out.println("\n===== Account Details =====");
        System.out.println("Username: " + currentUser.getUsername());
        System.out.println("Account Balance: $" + String.format("%.2f", currentUser.getAccount().getBalance()));
        
        // Display portfolio summary
        Map<String, Double> holdings = currentUser.getPortfolio().getHoldings();
        if (!holdings.isEmpty()) {
            System.out.println("\nCrypto Holdings:");
            for (Map.Entry<String, Double> entry : holdings.entrySet()) {
                System.out.println(entry.getKey() + ": " + entry.getValue());
            }
        } else {
            System.out.println("\nNo cryptocurrency holdings yet.");
        }
        
        // Pause for user to read
        System.out.print("\nPress Enter to continue...");
        scanner.nextLine();
    }
    
    /**
     * Handles depositing funds into the account.
     */
    private static void depositFunds() {
        System.out.println("\n===== Deposit Funds =====");
        System.out.print("Enter amount to deposit: $");
        
        try {
            double amount = Double.parseDouble(scanner.nextLine());
            
            if (amount <= 0) {
                System.out.println("Amount must be positive.");
                return;
            }
            
            System.out.print("Description (optional): ");
            String description = scanner.nextLine();
            
            if (description.isEmpty()) {
                description = "Deposit";
            }
            
            currentUser.getAccount().deposit(amount, description);
            userManager.saveUsers();
            
            System.out.println("Successfully deposited $" + String.format("%.2f", amount));
            System.out.println("New balance: $" + String.format("%.2f", currentUser.getAccount().getBalance()));
        } catch (NumberFormatException e) {
            System.out.println("Invalid amount. Please enter a valid number.");
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
        
        // Pause for user to read
        System.out.print("\nPress Enter to continue...");
        scanner.nextLine();
    }
    
    /**
     * Handles withdrawing funds from the account.
     */
    private static void withdrawFunds() {
        System.out.println("\n===== Withdraw Funds =====");
        System.out.println("Current balance: $" + String.format("%.2f", currentUser.getAccount().getBalance()));
        System.out.print("Enter amount to withdraw: $");
        
        try {
            double amount = Double.parseDouble(scanner.nextLine());
            
            if (amount <= 0) {
                System.out.println("Amount must be positive.");
                return;
            }
            
            System.out.print("Description (optional): ");
            String description = scanner.nextLine();
            
            if (description.isEmpty()) {
                description = "Withdrawal";
            }
            
            currentUser.getAccount().withdraw(amount, description);
            userManager.saveUsers();
            
            System.out.println("Successfully withdrew $" + String.format("%.2f", amount));
            System.out.println("New balance: $" + String.format("%.2f", currentUser.getAccount().getBalance()));
        } catch (NumberFormatException e) {
            System.out.println("Invalid amount. Please enter a valid number.");
        } catch (IllegalStateException e) {
            System.out.println("Insufficient balance for this withdrawal.");
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
        
        // Pause for user to read
        System.out.print("\nPress Enter to continue...");
        scanner.nextLine();
    }
    
    /**
     * Displays transaction history.
     */
    private static void viewTransactionHistory() {
        System.out.println("\n===== Transaction History =====");
        List<Transaction> transactions = currentUser.getAccount().getTransactions();
        
        if (transactions.isEmpty()) {
            System.out.println("No transactions found.");
        } else {
            System.out.println("| Type       | Amount    | Description               | Date                  |");
            System.out.println("|------------|-----------|---------------------------|------------------------|");
            
            for (Transaction tx : transactions) {
                System.out.printf("| %-10s | $%-8.2f | %-25s | %-22s |\n",
                        tx.getType(),
                        tx.getAmount(),
                        tx.getDescription(),
                        tx.getTimestamp().toString());
            }
        }
        
        // Pause for user to read
        System.out.print("\nPress Enter to continue...");
        scanner.nextLine();
    }
    
    /**
     * Displays the user's cryptocurrency portfolio.
     */
    private static void viewPortfolio() {
        System.out.println("\n===== Crypto Portfolio =====");
        Map<String, Double> holdings = currentUser.getPortfolio().getHoldings();
        
        if (holdings.isEmpty()) {
            System.out.println("Your portfolio is empty. Use the Trade option to buy cryptocurrency.");
        } else {
            System.out.println("| Coin    | Amount          |");
            System.out.println("|---------|-----------------|");
            
            for (Map.Entry<String, Double> entry : holdings.entrySet()) {
                System.out.printf("| %-7s | %-15.8f |\n", entry.getKey(), entry.getValue());
            }
        }
        
        // Pause for user to read
        System.out.print("\nPress Enter to continue...");
        scanner.nextLine();
    }
    
    /**
     * Handles cryptocurrency trading.
     */
    private static void tradeCrypto() {
        System.out.println("\n===== Trade Cryptocurrency =====");
        System.out.println("1. Buy Cryptocurrency");
        System.out.println("2. Sell Cryptocurrency");
        System.out.println("3. Back to Main Menu");
        System.out.print("Choose an option (1-3): ");
        
        String choice = scanner.nextLine();
        
        switch (choice) {
            case "1":
                buyCrypto();
                break;
            case "2":
                sellCrypto();
                break;
            case "3":
                return;
            default:
                System.out.println("Invalid option. Please try again.");
        }
    }
    
    /**
     * Handles buying cryptocurrency.
     */
    private static void buyCrypto() {
        System.out.println("\n===== Buy Cryptocurrency =====");
        System.out.println("Available balance: $" + String.format("%.2f", currentUser.getAccount().getBalance()));
        
        // For demonstration purposes, we'll use a simple list of supported coins
        System.out.println("\nSupported coins: BTC, ETH, BNB, SOL, ADA, XRP");
        
        System.out.print("Enter coin symbol: ");
        String symbol = scanner.nextLine().toUpperCase();
        
        // Validate coin symbol
        if (!isValidCoin(symbol)) {
            System.out.println("Invalid coin symbol. Please try again.");
            return;
        }
        
        System.out.print("Enter current price per coin (USD): $");
        try {
            double price = Double.parseDouble(scanner.nextLine());
            
            if (price <= 0) {
                System.out.println("Price must be positive.");
                return;
            }
            
            System.out.print("Enter amount to spend (USD): $");
            double spendAmount = Double.parseDouble(scanner.nextLine());
            
            if (spendAmount <= 0) {
                System.out.println("Amount must be positive.");
                return;
            }
            
            if (spendAmount > currentUser.getAccount().getBalance()) {
                System.out.println("Insufficient funds for this purchase.");
                return;
            }
            
            // Calculate amount of coins to buy
            double coinAmount = spendAmount / price;
            
            // Execute the trade
            currentUser.getPortfolio().buyCrypto(symbol, coinAmount, price);
            userManager.saveUsers();
            
            System.out.println("Successfully purchased " + String.format("%.8f", coinAmount) + " " + symbol);
            System.out.println("New balance: $" + String.format("%.2f", currentUser.getAccount().getBalance()));
        } catch (NumberFormatException e) {
            System.out.println("Invalid number format. Please enter valid numbers.");
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
        
        // Pause for user to read
        System.out.print("\nPress Enter to continue...");
        scanner.nextLine();
    }
    
    /**
     * Handles selling cryptocurrency.
     */
    private static void sellCrypto() {
        System.out.println("\n===== Sell Cryptocurrency =====");
        Map<String, Double> holdings = currentUser.getPortfolio().getHoldings();
        
        if (holdings.isEmpty()) {
            System.out.println("Your portfolio is empty. Nothing to sell.");
            
            // Pause for user to read
            System.out.print("\nPress Enter to continue...");
            scanner.nextLine();
            return;
        }
        
        System.out.println("Your holdings:");
        for (Map.Entry<String, Double> entry : holdings.entrySet()) {
            System.out.println(entry.getKey() + ": " + String.format("%.8f", entry.getValue()));
        }
        
        System.out.print("\nEnter coin symbol to sell: ");
        String symbol = scanner.nextLine().toUpperCase();
        
        if (!holdings.containsKey(symbol)) {
            System.out.println("You don't own any " + symbol + ".");
            
            // Pause for user to read
            System.out.print("\nPress Enter to continue...");
            scanner.nextLine();
            return;
        }
        
        double maxAmount = holdings.get(symbol);
        System.out.println("You own " + String.format("%.8f", maxAmount) + " " + symbol);
        
        System.out.print("Enter current price per coin (USD): $");
        try {
            double price = Double.parseDouble(scanner.nextLine());
            
            if (price <= 0) {
                System.out.println("Price must be positive.");
                return;
            }
            
            System.out.print("Enter amount to sell (or type 'all' for all): ");
            String amountInput = scanner.nextLine();
            
            double sellAmount;
            if (amountInput.equalsIgnoreCase("all")) {
                sellAmount = maxAmount;
            } else {
                sellAmount = Double.parseDouble(amountInput);
                
                if (sellAmount <= 0) {
                    System.out.println("Amount must be positive.");
                    return;
                }
                
                if (sellAmount > maxAmount) {
                    System.out.println("You don't have that much " + symbol + ".");
                    return;
                }
            }
            
            // Execute the trade
            currentUser.getPortfolio().sellCrypto(symbol, sellAmount, price);
            userManager.saveUsers();
            
            double valueReceived = sellAmount * price;
            System.out.println("Successfully sold " + String.format("%.8f", sellAmount) + " " + symbol);
            System.out.println("Received: $" + String.format("%.2f", valueReceived));
            System.out.println("New balance: $" + String.format("%.2f", currentUser.getAccount().getBalance()));
        } catch (NumberFormatException e) {
            System.out.println("Invalid number format. Please enter valid numbers.");
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
        
        // Pause for user to read
        System.out.print("\nPress Enter to continue...");
        scanner.nextLine();
    }
    
    /**
     * Checks if a coin symbol is valid.
     * 
     * @param symbol Coin symbol to validate
     * @return true if valid, false otherwise
     */
    private static boolean isValidCoin(String symbol) {
        // For demonstration purposes, we'll accept only these coins
        String[] validCoins = {"BTC", "ETH", "BNB", "SOL", "ADA", "XRP"};
        
        for (String coin : validCoins) {
            if (coin.equalsIgnoreCase(symbol)) {
                return true;
            }
        }
        
        return false;
    }
} 