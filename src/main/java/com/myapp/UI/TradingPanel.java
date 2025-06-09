package com.myapp.UI;

import javax.swing.*;
import java.awt.*;
import java.util.Map;
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

    public TradingPanel(Portfolio portfolio) {
        this.portfolio = portfolio;
        setLayout(new BorderLayout());

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

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(buyButton);
        buttonPanel.add(sellButton);
        buttonPanel.add(refreshButton);
        buttonPanel.add(recommendButton);

        outputArea = new JTextArea(10, 40);
        outputArea.setEditable(false);
        JScrollPane scroll = new JScrollPane(outputArea);

        add(topPanel, BorderLayout.NORTH);
        add(buttonPanel, BorderLayout.CENTER);
        add(scroll, BorderLayout.SOUTH);

        // Action Listeners
        buyButton.addActionListener(e -> {
            String coin = (String) coinBox.getSelectedItem();
            double fiatAmount;
            try {
                fiatAmount = Double.parseDouble(amountField.getText());
            } catch (NumberFormatException ex) {
                outputArea.append("Invalid amount format.\n");
                return;
            }

            showLoadingDialog("Buying " + coin + "...", () -> {
                try {
                    CryptoService service = new CryptoService();
                    double price = service.getCurrentPrices().get(coin);
                    double quantity = fiatAmount / price;
                    portfolio.buyCrypto(coin, quantity, price);
                    SwingUtilities.invokeLater(() ->
                            outputArea.append(String.format("Bought %.6f %s at $%.2f each (Total $%.2f)\n", quantity, coin, price, fiatAmount)));
                } catch (Exception ex) {
                    SwingUtilities.invokeLater(() ->
                            outputArea.append("Buy Error: " + ex.getMessage() + "\n"));
                }
            });
        });

        sellButton.addActionListener(e -> {
            String coin = (String) coinBox.getSelectedItem();
            double quantity;
            try {
                quantity = Double.parseDouble(amountField.getText());
            } catch (NumberFormatException ex) {
                outputArea.append("Invalid quantity format.\n");
                return;
            }

            showLoadingDialog("Selling " + coin + "...", () -> {
                try {
                    CryptoService service = new CryptoService();
                    double price = service.getCurrentPrices().get(coin);
                    portfolio.sellCrypto(coin, quantity, price);
                    double total = quantity * price;
                    SwingUtilities.invokeLater(() ->
                            outputArea.append(String.format("Sold %.6f %s at $%.2f each (Total $%.2f)\n", quantity, coin, price, total)));
                } catch (Exception ex) {
                    SwingUtilities.invokeLater(() ->
                            outputArea.append("Sell Error: " + ex.getMessage() + "\n"));
                }
            });
        });

        refreshButton.addActionListener(e -> {
            showLoadingDialog("Fetching current prices...", () -> {
                try {
                    Map<String, Double> prices = new CryptoService().getCurrentPrices();
                    SwingUtilities.invokeLater(() -> {
                        outputArea.append("Current Prices:\n");
                        for (Map.Entry<String, Double> entry : prices.entrySet()) {
                            outputArea.append(entry.getKey() + ": $" + entry.getValue() + "\n");
                        }
                    });
                } catch (Exception ex) {
                    SwingUtilities.invokeLater(() ->
                            outputArea.append("Price Fetch Error: " + ex.getMessage() + "\n"));
                }
            });
        });

        recommendButton.addActionListener(e -> {
            showLoadingDialog("Running machine learning model...", () -> {
                try {
                    CryptoAdvisor advisor = new CryptoAdvisor(new CryptoService(), 14);
                    String best = advisor.recommendCoin();
                    SwingUtilities.invokeLater(() ->
                            outputArea.append("Recommended Coin: " + best + "\n"));
                } catch (Exception ex) {
                    SwingUtilities.invokeLater(() ->
                            outputArea.append("Recommendation Error: " + ex.getMessage() + "\n"));
                }
            });
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
}
