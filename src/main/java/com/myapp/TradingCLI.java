package com.myapp;
import java.util.*;
import com.myapp.UI.TransactionFilter;

/**
 * Command-line interface (CLI) for interacting with the crypto trading application.
 * Provides user interaction for buying, selling, portfolio management, and transaction history.
 */
public class TradingCLI {
    private final Portfolio portfolio;
    private final CryptoService cryptoService;
    private final Scanner scanner;
    private final Account account;

    /**
     * Creates a new TradingCLI instance bound to the specified portfolio.
     *
     * @param portfolio The portfolio to manage and interact with.
     */
    public TradingCLI(Portfolio portfolio) {
        this.portfolio = portfolio;
        this.cryptoService = new CryptoService();
        this.scanner = new Scanner(System.in);
        this.account = portfolio.getAccount();
    }

    /**
     * Starts the CLI event loop, displaying the menu and handling user commands
     * until the user chooses to exit.
     */
    public void start() {
        System.out.println("Welcome to the Crypto Trading CLI!");

        while (true) {
            showMainMenu();
            String input = scanner.nextLine().trim().toLowerCase();

            try {
                switch (input) {
                    case "1" -> buyCrypto();
                    case "2" -> sellCrypto();
                    case "3" -> showPrices();
                    case "4" -> showPortfolio();
                    case "5" -> recommendCoin();
                    case "6" -> openShort();
                    case "7" -> closeShort();
                    case "8" -> handleDeposit();
                    case "9" -> handleWithdraw();
                    case "10" -> showTransactions(account.getTransactions());
                    case "11" -> handleFilter();
                    case "0" -> {
                        System.out.println("Exiting...");
                        return;
                    }
                    default -> System.out.println("Invalid option. Try again.");
                }
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
    }

    private void showMainMenu() {
        System.out.println("\n--- MENU ---");
        System.out.println("1. Buy Crypto");
        System.out.println("2. Sell Crypto");
        System.out.println("3. Show Current Prices");
        System.out.println("4. Show Portfolio");
        System.out.println("5. Recommend Coin (ML)");
        System.out.println("6. Open Short Position");
        System.out.println("7. Close Short Position");
        System.out.println("8. Deposit");
        System.out.println("9. Withdraw");
        System.out.println("10. Show all transactions");
        System.out.println("11. Show filtered transactions");
        System.out.println("0. Exit");
        System.out.print("Choose an option: ");
    }

    private void handleDeposit() {
        try {
            System.out.print("Enter amount to deposit: ");
            double amount = Double.parseDouble(scanner.nextLine());

            System.out.print("Enter description: ");
            String description = scanner.nextLine();

            account.deposit(amount, description.isEmpty() ? "Deposit" : description);
            System.out.println("Deposit successful. New balance: " + account.getBalance());
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private void handleWithdraw() {
        try {
            System.out.print("Enter amount to withdraw: ");
            double amount = Double.parseDouble(scanner.nextLine());

            System.out.print("Enter description: ");
            String description = scanner.nextLine();

            account.withdraw(amount, description.isEmpty() ? "Withdrawal" : description);
            System.out.println("Withdrawal successful. New balance: " + account.getBalance());
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private void buyCrypto() throws Exception {
        System.out.print("Enter coin symbol to buy: ");
        String coin = scanner.nextLine().trim().toUpperCase();

        System.out.print("Enter amount in fiat (USD): ");
        double amount = Double.parseDouble(scanner.nextLine());

        portfolio.buyCrypto(coin, amount);
        System.out.println("Bought " + coin + " for $" + amount);
    }

    private void sellCrypto() throws Exception {
        System.out.print("Enter coin symbol to sell: ");
        String coin = scanner.nextLine().trim().toUpperCase();

        double balance = portfolio.getHoldings().getOrDefault(coin, 0.0);
        if (balance <= 0) {
            System.out.println("You don't hold any " + coin);
            return;
        }

        System.out.printf("You have %.8f %s. How much do you want to sell? (Enter 0 for all): ", balance, coin);
        double amount = Double.parseDouble(scanner.nextLine());

        if (amount == 0) amount = balance;

        double value = portfolio.sellCrypto(coin, amount);
        System.out.printf("Sold %.8f %s for $%.2f\n", amount, coin, value);
    }

    private void showPrices() throws Exception {
        Map<String, Double> prices = cryptoService.getCurrentPrices();
        System.out.println("\n--- Current Prices ---");
        prices.forEach((k, v) -> System.out.printf("%s: $%.4f\n", k, v));
    }

    private void showPortfolio() {
        System.out.println("\n--- Portfolio Holdings ---");
        portfolio.getHoldings().forEach((k, v) -> System.out.printf("%s: %.8f\n", k, v));

        System.out.println("\n--- Short Positions ---");
        portfolio.getAllPositions().forEach((coin, positions) -> {
            for (Position pos : positions) {
                if (pos.getType() == PositionType.SHORT) {
                    System.out.printf("%s: %.8f at entry $%.4f\n", coin, pos.getAmount(), pos.getEntryPrice());
                }
            }
        });
    }

    private void recommendCoin() throws Exception {
        System.out.println("Running ML-based recommendation...");
        CryptoAdvisor advisor = new CryptoAdvisor(cryptoService, 14, true);
        String best = advisor.recommendCoin(new java.util.concurrent.atomic.AtomicBoolean(false));
        System.out.println("Recommended Coin: " + best);
    }

    private void openShort() throws Exception {
        System.out.print("Enter coin to short: ");
        String coin = scanner.nextLine().trim().toUpperCase();

        System.out.print("Enter amount to short: ");
        double amount = Double.parseDouble(scanner.nextLine());

        double price = cryptoService.getCurrentPrices().get(coin);
        portfolio.openShortPosition(coin, amount, price);

        System.out.printf("Opened short: %.8f %s at $%.4f\n", amount, coin, price);
    }

    private void closeShort() throws Exception {
        System.out.print("Enter coin to close short: ");
        String coin = scanner.nextLine().trim().toUpperCase();

        double availableShort = portfolio.getShortPositions().getOrDefault(coin, 0.0);
        if (availableShort <= 0) {
            System.out.println("No short positions for " + coin);
            return;
        }

        System.out.printf("You have %.8f %s shorted. How much to close? (Enter 0 for all): ", availableShort, coin);
        double amount = Double.parseDouble(scanner.nextLine());

        if (amount == 0) amount = availableShort;

        double price = cryptoService.getCurrentPrices().get(coin);
        double pnl = portfolio.closeShortPosition(coin, amount, price);

        System.out.printf("Closed short %.8f %s at $%.4f, PnL: $%.2f\n", amount, coin, price, pnl);
    }

    private void showTransactions(List<Transaction> transactions) {
        if (transactions.isEmpty()) {
            System.out.println("No transactions found.");
            return;
        }
        System.out.println("\nTransactions:");
        for (Transaction t : transactions) {
            System.out.printf("ID: %s | Time: %s | Type: %s | Amount: %.2f | Description: %s%n",
                    t.getTransactionId(),
                    t.getTimestamp(),
                    t.getType(),
                    t.getAmount(),
                    t.getDescription());
        }
    }

    private void handleFilter() {
        System.out.println("Filter by Transaction Type:");
        System.out.println("1. DEPOSIT");
        System.out.println("2. WITHDRAWAL");
        System.out.println("3. TRANSFER");
        System.out.println("4. CRYPTO_PURCHASE");
        System.out.println("5. CRYPTO_SALE");
        System.out.println("6. CRYPTO_SHORT_OPEN");
        System.out.println("7. CRYPTO_SHORT_CLOSE");
        System.out.print("Select option (or 0 to cancel): ");

        try {
            int option = Integer.parseInt(scanner.nextLine());
            if (option == 0) {
                System.out.println("Filter cancelled.");
                return;
            }

            TransactionType selectedType = null;
            switch (option) {
                case 1 -> selectedType = TransactionType.DEPOSIT;
                case 2 -> selectedType = TransactionType.WITHDRAWAL;
                case 3 -> selectedType = TransactionType.TRANSFER;
                case 4 -> selectedType = TransactionType.CRYPTO_PURCHASE;
                case 5 -> selectedType = TransactionType.CRYPTO_SALE;
                case 6 -> selectedType = TransactionType.CRYPTO_SHORT_OPEN;
                case 7 -> selectedType = TransactionType.CRYPTO_SHORT_CLOSE;
                default -> {
                    System.out.println("Invalid option.");
                    return;
                }
            }

            // Create a filter instance and set the type
            TransactionFilter filter = new TransactionFilter();
            filter.setType(selectedType);

            // Get filtered transactions
            List<Transaction> filteredTransactions = account.searchTransactions(filter);

            // Display filtered transactions
            System.out.println("Filtered transactions of type: " + selectedType);
            for (Transaction t : filteredTransactions) {
                System.out.println(t); // Adjust this if needed for custom display
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid input. Please enter a number.");
        }
    }
}
