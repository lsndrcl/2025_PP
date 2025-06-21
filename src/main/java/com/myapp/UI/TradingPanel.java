package com.myapp.UI;

import javax.swing.*;
import java.awt.*;
import java.text.NumberFormat;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import com.myapp.Portfolio;
import com.myapp.CryptoAdvisor;
import com.myapp.CryptoService;

/**
 * com.myapp.UI.TradingPanel is a Swing-based GUI panel for simulating cryptocurrency trading operations.
 */
public class TradingPanel extends JPanel {
    private final Portfolio portfolio;
    private final JComboBox<String> coinBox;
    private final JTextField amountField;
    private final JTextArea outputArea;
    private SwingWorker<Void, Void> currentMLTask;
    private final AtomicBoolean cancelRequested = new AtomicBoolean(false);
    private final NumberFormat currencyFormat;

    public TradingPanel(Portfolio portfolio) {
        this.portfolio = portfolio;
        setLayout(new BorderLayout());
        
        // Initialize currency formatter
        currencyFormat = NumberFormat.getCurrencyInstance();
        currencyFormat.setMinimumFractionDigits(2);
        currencyFormat.setMaximumFractionDigits(8);

        JPanel topPanel = new JPanel(new GridLayout(3, 2));

        String[] coinSymbols;
        try {
            CryptoService service = new CryptoService();
            Map<String, Double> prices = service.getCurrentPrices();
            coinSymbols = prices.keySet().toArray(new String[0]);
        } catch (Exception e) {
            coinSymbols = new String[]{"BTC", "ETH", "ADA"}; // fallback
        }
        coinBox = new JComboBox<>(coinSymbols);

        amountField = new JTextField();

        topPanel.add(new JLabel("Coin:"));
        topPanel.add(coinBox);
        topPanel.add(new JLabel("Amount (Fiat or Quantity):"));
        topPanel.add(amountField);

        JButton buyButton = new JButton("Buy");
        JButton sellButton = new JButton("Sell");
        JButton refreshButton = new JButton("Refresh Prices");
        JButton recommendButton = new JButton("Recommend");
        JButton cancelButton = new JButton("Cancel ML");
        JButton clearButton = new JButton("Clear Output");
        JButton showBalanceButton = new JButton("Show Balance");

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(buyButton);
        buttonPanel.add(sellButton);
        buttonPanel.add(refreshButton);
        buttonPanel.add(recommendButton);
        buttonPanel.add(cancelButton);
        buttonPanel.add(clearButton);
        buttonPanel.add(showBalanceButton);

        outputArea = new JTextArea(10, 40);
        outputArea.setEditable(false);
        JScrollPane scroll = new JScrollPane(outputArea);

        add(topPanel, BorderLayout.NORTH);
        add(buttonPanel, BorderLayout.CENTER);
        add(scroll, BorderLayout.SOUTH);

        // Action Listeners
        buyButton.addActionListener(e -> {
            try {
                String coin = (String) coinBox.getSelectedItem();
                double amount = Double.parseDouble(amountField.getText());
                portfolio.buyCrypto(coin, amount);
                outputArea.append("Bought " + coin + " for $" + amount + "\n");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Buy Error", JOptionPane.ERROR_MESSAGE);
            }
        });


        /**
         * Handles the "Sell" button click event.
         *
         * This method performs the following steps:
         * 1. Retrieves the selected cryptocurrency from the coin dropdown.
         * 2. Checks the user's available balance of that cryptocurrency.
         * 3. If no balance is available, shows a warning and aborts.
         * 4. Presents the user with a dialog offering three options:
         *    - Sell All: Sells the entire balance of the selected coin.
         *    - Sell Specific Amount: Prompts the user to enter a specific amount to sell.
         *    - Cancel: Aborts the operation.
         * 5. If "Sell All" is chosen, executes a sale for the full balance and logs the result.
         * 6. If "Sell Specific Amount" is chosen:
         *    - Prompts the user to input a sell amount (showing max allowed).
         *    - Validates the input is a positive number within available balance.
         *    - If invalid input is detected, shows an error and aborts.
         *    - Executes the sale for the specified amount and logs the result.
         * 7. If "Cancel" is chosen or dialog is closed, the sale operation is aborted silently.
         * 8. Any exceptions during the process are caught and displayed in an error dialog.
         */
        sellButton.addActionListener(e -> {
            try {
                String coin = (String) coinBox.getSelectedItem();
                double availableBalance = portfolio.getHoldings().getOrDefault(coin, 0.0);

                if (availableBalance <= 0) {
                    JOptionPane.showMessageDialog(this,
                            "You do not have any " + coin + " to sell.",
                            "No Balance",
                            JOptionPane.WARNING_MESSAGE);
                    return;
                }

                String message = String.format("You have %.8f %s available.\nChoose an option:", availableBalance, coin);
                Object[] options = {"Sell All", "Sell Specific Amount", "Cancel"};
                int choice = JOptionPane.showOptionDialog(
                        this,
                        message,
                        "Sell " + coin,
                        JOptionPane.YES_NO_CANCEL_OPTION,
                        JOptionPane.QUESTION_MESSAGE,
                        null,
                        options,
                        options[2]
                );

                if (choice == JOptionPane.YES_OPTION) {
                    // Sell All
                    double value = portfolio.sellCrypto(coin, availableBalance);
                    outputArea.append(String.format("Sold all %.8f %s for $%.2f\n", availableBalance, coin, value));
                } else if (choice == JOptionPane.NO_OPTION) {
                    // Sell Specific Amount
                    String input = JOptionPane.showInputDialog(this,
                            String.format("Enter amount of %s to sell (max %.8f):", coin, availableBalance),
                            "0.0"
                    );

                    if (input == null) return; // User cancelled

                    double amount;
                    try {
                        amount = Double.parseDouble(input);
                        if (amount <= 0 || amount > availableBalance) {
                            JOptionPane.showMessageDialog(this,
                                    "Invalid amount. Please enter a positive number no greater than your available balance.",
                                    "Invalid Input",
                                    JOptionPane.ERROR_MESSAGE);
                            return;
                        }
                    } catch (NumberFormatException ex) {
                        JOptionPane.showMessageDialog(this,
                                "Please enter a valid number.",
                                "Input Error",
                                JOptionPane.ERROR_MESSAGE);
                        return;
                    }

                    double value = portfolio.sellCrypto(coin, amount);
                    outputArea.append(String.format("Sold %.8f %s for $%.2f\n", amount, coin, value));
                }
                // Cancel option does nothing
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Sell Error", JOptionPane.ERROR_MESSAGE);
            }
        });


        refreshButton.addActionListener(e -> {
            try {
                CryptoService service = new CryptoService();
                Map<String, Double> prices = service.getCurrentPrices();
                
                outputArea.append("\n--- Current Prices ---\n");
                for (Map.Entry<String, Double> entry : prices.entrySet()) {
                    outputArea.append(String.format("%s: %s\n", 
                            entry.getKey(), 
                            formatCryptoPrice(entry.getValue())));
                }
                outputArea.append("---------------------\n");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Price Refresh Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        recommendButton.addActionListener(e -> {
            // Reset cancel flag
            cancelRequested.set(false);
            
            // Enable cancel button
            cancelButton.setEnabled(true);
            recommendButton.setEnabled(false);
            
            showLoadingDialog("Running machine learning model...", () -> {
                try {
                    // Use the optimized CryptoAdvisor with parallel processing
                    CryptoAdvisor advisor = new CryptoAdvisor(new CryptoService(), 14, true);
                    long startTime = System.currentTimeMillis();
                    
                    // Check for cancellation periodically
                    currentMLTask = new SwingWorker<Void, Void>() {
                        @Override
                        protected Void doInBackground() throws Exception {
                            String best = advisor.recommendCoin(cancelRequested);
                            long endTime = System.currentTimeMillis();
                            long duration = endTime - startTime;
                            
                            if (!cancelRequested.get()) {
                                SwingUtilities.invokeLater(() -> {
                                    outputArea.append(String.format("Recommended Coin: %s (analysis completed in %.1f seconds)\n", 
                                            best, duration/1000.0));
                                });
                            } else {
                                SwingUtilities.invokeLater(() -> {
                                    outputArea.append("ML recommendation was cancelled by user\n");
                                });
                            }
                            return null;
                        }
                        
                        @Override
                        protected void done() {
                            recommendButton.setEnabled(true);
                            cancelButton.setEnabled(false);
                        }
                    };
                    
                    currentMLTask.execute();
                    
                } catch (Exception ex) {
                    SwingUtilities.invokeLater(() -> {
                        outputArea.append("Recommendation Error: " + ex.getMessage() + "\n");
                        recommendButton.setEnabled(true);
                        cancelButton.setEnabled(false);
                    });
                }
            });
        });
        
        // Cancel button initially disabled
        cancelButton.setEnabled(false);
        
        cancelButton.addActionListener(e -> {
            if (currentMLTask != null && !currentMLTask.isDone()) {
                cancelRequested.set(true);
                outputArea.append("Cancelling ML recommendation...\n");
            }
        });
        
        clearButton.addActionListener(e -> {
            outputArea.setText("");
        });

        showBalanceButton.addActionListener(e -> {
            try {
                Map<String, Double> holdings = portfolio.getHoldings();
                if (holdings.isEmpty()) {
                    outputArea.append("\nYou don't own any cryptocurrencies.\n");
                    return;
                }

                CryptoService service = new CryptoService();
                Map<String, Double> prices = service.getCurrentPrices();

                outputArea.append("\n--- Current Portfolio Holdings ---\n");
                double totalValue = 0.0;

                for (Map.Entry<String, Double> entry : holdings.entrySet()) {
                    String symbol = entry.getKey();
                    double amount = entry.getValue();
                    double price = prices.getOrDefault(symbol, 0.0);
                    double value = amount * price;
                    totalValue += value;

                    outputArea.append(String.format("%s: %.8f @ %s each = %s\n",
                            symbol, amount, formatCryptoPrice(price), currencyFormat.format(value)));
                }

                outputArea.append(String.format("Total Portfolio Value: %s\n", currencyFormat.format(totalValue)));
                outputArea.append("-----------------------------------\n");

                double fiatBalance = portfolio.getAccount().getBalance();
                outputArea.append(String.format("Fiat Balance: %s\n", currencyFormat.format(fiatBalance)));


            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Balance Error", JOptionPane.ERROR_MESSAGE);
            }
        });

    }

    private void showLoadingDialog(String message, Runnable task) {
        JDialog loadingDialog = new JDialog(SwingUtilities.getWindowAncestor(this), "Please Wait", Dialog.ModalityType.APPLICATION_MODAL);
        JLabel label = new JLabel(message, SwingConstants.CENTER);
        loadingDialog.add(label);
        loadingDialog.setSize(300, 100);
        loadingDialog.setLocationRelativeTo(this);
        loadingDialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);

        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() {
                task.run();
                return null;
            }

            @Override
            protected void done() {
                loadingDialog.dispose();
            }
        };

        SwingUtilities.invokeLater(() -> {
            worker.execute();
            loadingDialog.setVisible(true);
        });
    }
    
    /**
     * Format crypto price with appropriate precision based on value
     */
    private String formatCryptoPrice(double price) {
        if (price < 0.01) {
            return String.format("$%.8f", price);
        } else if (price < 1.0) {
            return String.format("$%.6f", price);
        } else if (price < 1000) {
            return String.format("$%.4f", price);
        } else {
            return String.format("$%.2f", price);
        }
    }
}
