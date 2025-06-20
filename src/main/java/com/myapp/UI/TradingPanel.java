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

        coinBox = new JComboBox<>(new String[]{"BTC", "ETH", "ADA"});
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

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(buyButton);
        buttonPanel.add(sellButton);
        buttonPanel.add(refreshButton);
        buttonPanel.add(recommendButton);
        buttonPanel.add(cancelButton);
        buttonPanel.add(clearButton);

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

        sellButton.addActionListener(e -> {
            try {
                String coin = (String) coinBox.getSelectedItem();
                double amount = Double.parseDouble(amountField.getText());
                double value = portfolio.sellCrypto(coin, amount);
                outputArea.append("Sold " + amount + " " + coin + " for $" + value + "\n");
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
