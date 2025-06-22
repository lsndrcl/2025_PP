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
    private final PortfolioBalancePanel portfolioBalancePanel;

    public TradingPanel(Portfolio portfolio) {
        this.portfolio = portfolio;
        setLayout(new BorderLayout());
        
        // Initialize currency formatter
        currencyFormat = NumberFormat.getCurrencyInstance();
        currencyFormat.setMinimumFractionDigits(2);
        currencyFormat.setMaximumFractionDigits(8);

        // Create portfolio balance panel
        portfolioBalancePanel = new PortfolioBalancePanel(portfolio);
        
        // Create trading controls panel
        JPanel tradingControlsPanel = new JPanel(new BorderLayout());
        
        // Create input panel with GridBagLayout for better responsiveness
        JPanel inputPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        
        // Coin selection
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.2;
        inputPanel.add(new JLabel("Coin:"), gbc);
        
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.weightx = 0.8;
        String[] coinSymbols;
        try {
            CryptoService service = new CryptoService();
            Map<String, Double> prices = service.getCurrentPrices();
            coinSymbols = prices.keySet().toArray(new String[0]);
        } catch (Exception e) {
            coinSymbols = new String[]{"BTC", "ETH", "ADA"}; // fallback
        }
        coinBox = new JComboBox<>(coinSymbols);
        inputPanel.add(coinBox, gbc);
        
        // Amount input
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0.2;
        inputPanel.add(new JLabel("Amount (Fiat or Quantity):"), gbc);
        
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.weightx = 0.8;
        amountField = new JTextField(10);
        inputPanel.add(amountField, gbc);

        // Create button panel with FlowLayout to keep buttons visible
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));
        
        // Create buttons with consistent size
        Dimension buttonSize = new Dimension(120, 30);
        
        JButton buyButton = new JButton("Buy");
        buyButton.setPreferredSize(buttonSize);
        
        JButton sellButton = new JButton("Sell");
        sellButton.setPreferredSize(buttonSize);
        
        JButton refreshButton = new JButton("Refresh Prices");
        refreshButton.setPreferredSize(buttonSize);
        
        JButton recommendButton = new JButton("Recommend");
        recommendButton.setPreferredSize(buttonSize);
        
        JButton cancelButton = new JButton("Cancel ML");
        cancelButton.setPreferredSize(buttonSize);
        
        JButton clearButton = new JButton("Clear Output");
        clearButton.setPreferredSize(buttonSize);

        // Add buttons to panel
        buttonPanel.add(buyButton);
        buttonPanel.add(sellButton);
        buttonPanel.add(refreshButton);
        buttonPanel.add(recommendButton);
        buttonPanel.add(cancelButton);
        buttonPanel.add(clearButton);

        // Create output area
        outputArea = new JTextArea(8, 40); // Reduced height to save space
        outputArea.setEditable(false);
        JScrollPane scroll = new JScrollPane(outputArea);
        
        // Add components to trading controls panel
        tradingControlsPanel.add(inputPanel, BorderLayout.NORTH);
        tradingControlsPanel.add(buttonPanel, BorderLayout.CENTER);
        tradingControlsPanel.add(scroll, BorderLayout.SOUTH);
        
        // Create split pane with minimum sizes to ensure both panels are visible
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, 
                portfolioBalancePanel, tradingControlsPanel);
        splitPane.setResizeWeight(0.5);
        splitPane.setDividerLocation(250);
        
        // Set minimum sizes for components to ensure they remain visible
        portfolioBalancePanel.setMinimumSize(new Dimension(300, 200));
        tradingControlsPanel.setMinimumSize(new Dimension(300, 200));
        
        add(splitPane, BorderLayout.CENTER);

        // Action Listeners
        buyButton.addActionListener(e -> {
            try {
                String coin = (String) coinBox.getSelectedItem();
                double amount = Double.parseDouble(amountField.getText());
                portfolio.buyCrypto(coin, amount);
                outputArea.append("Bought " + coin + " for $" + amount + "\n");
                
                // Refresh portfolio display
                portfolioBalancePanel.refreshData();
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
                    
                    // Refresh portfolio display
                    portfolioBalancePanel.refreshData();
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
                    
                    // Refresh portfolio display
                    portfolioBalancePanel.refreshData();
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
                
                // Refresh portfolio display
                portfolioBalancePanel.refreshData();
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
