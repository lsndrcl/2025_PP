package com.myapp.UI;

import com.myapp.CryptoService;
import com.myapp.Portfolio;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.NumberFormat;
import java.util.Currency;
import java.util.Locale;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Panel that displays the user's crypto portfolio with real-time price updates.
 */
public class PortfolioBalancePanel extends JPanel {
    private final Portfolio portfolio;
    private final JTable holdingsTable;
    private final DefaultTableModel tableModel;
    private final JLabel totalValueLabel;
    private final JLabel fiatBalanceLabel;
    private final JLabel lastUpdatedLabel;
    private NumberFormat currencyFormat;
    private final NumberFormat percentFormat;
    private Timer refreshTimer;
    private final int REFRESH_INTERVAL = 60000; // 60 seconds
    
    // Currency conversion rates (approximate)
    private static final double USD_TO_EUR = 0.93;
    private static final double USD_TO_GBP = 0.79;
    
    // Current currency selection
    private String currentCurrency = "USD";
    
    /**
     * Creates a new portfolio balance panel.
     * @param portfolio The user's portfolio
     */
    public PortfolioBalancePanel(Portfolio portfolio) {
        this.portfolio = portfolio;
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Initialize formatters
        currencyFormat = NumberFormat.getCurrencyInstance(Locale.US);
        currencyFormat.setMinimumFractionDigits(2);
        currencyFormat.setMaximumFractionDigits(8);
        
        percentFormat = NumberFormat.getPercentInstance();
        percentFormat.setMinimumFractionDigits(2);
        percentFormat.setMaximumFractionDigits(2);
        
        // Create header panel with currency selection
        JPanel headerPanel = new JPanel(new BorderLayout());
        JLabel titleLabel = new JLabel("Portfolio Balance", SwingConstants.LEFT);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        headerPanel.add(titleLabel, BorderLayout.WEST);
        
        // Create currency selection panel
        JPanel currencyPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JLabel currencyLabel = new JLabel("Currency:");
        String[] currencies = {"USD", "EUR", "GBP"};
        JComboBox<String> currencySelector = new JComboBox<>(currencies);
        currencySelector.addActionListener(e -> {
            currentCurrency = (String) currencySelector.getSelectedItem();
            updateCurrencyFormat(currentCurrency);
            refreshData(); // Refresh to show new currency
        });
        
        // Create refresh button
        JButton refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(e -> refreshData());
        
        currencyPanel.add(currencyLabel);
        currencyPanel.add(currencySelector);
        currencyPanel.add(refreshButton);
        
        headerPanel.add(currencyPanel, BorderLayout.EAST);
        
        add(headerPanel, BorderLayout.NORTH);
        
        // Create table for holdings
        String[] columnNames = {"Coin", "Amount", "Purchase Price", "Current Price", "Value", "Profit/Loss"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Make all cells non-editable
            }
            
            @Override
            public Class<?> getColumnClass(int column) {
                if (column == 5) return Double.class; // For profit/loss column
                return Object.class;
            }
        };
        
        holdingsTable = new JTable(tableModel);
        holdingsTable.setRowHeight(25);
        holdingsTable.getTableHeader().setReorderingAllowed(false);
        
        // Custom renderer for profit/loss column
        holdingsTable.getColumnModel().getColumn(5).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, 
                    boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                
                if (value instanceof Double) {
                    double profitLoss = (Double) value;
                    setText(percentFormat.format(profitLoss / 100.0));
                    
                    // Set color based on profit/loss
                    if (profitLoss > 0) {
                        setForeground(new Color(0, 128, 0)); // Green
                    } else if (profitLoss < 0) {
                        setForeground(new Color(192, 0, 0)); // Red
                    } else {
                        setForeground(table.getForeground());
                    }
                }
                return c;
            }
        });
        
        JScrollPane scrollPane = new JScrollPane(holdingsTable);
        add(scrollPane, BorderLayout.CENTER);
        
        // Create footer panel with totals
        JPanel footerPanel = new JPanel(new GridLayout(3, 1));
        
        totalValueLabel = new JLabel("Total Portfolio Value: $0.00");
        totalValueLabel.setFont(new Font("Arial", Font.BOLD, 14));
        
        fiatBalanceLabel = new JLabel("Fiat Balance: $0.00");
        fiatBalanceLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        
        lastUpdatedLabel = new JLabel("Last updated: Never");
        lastUpdatedLabel.setFont(new Font("Arial", Font.ITALIC, 12));
        
        footerPanel.add(totalValueLabel);
        footerPanel.add(fiatBalanceLabel);
        footerPanel.add(lastUpdatedLabel);
        
        add(footerPanel, BorderLayout.SOUTH);
        
        // Initial data load
        refreshData();
        
        // Set up automatic refresh
        startAutoRefresh();
    }
    
    /**
     * Updates the currency formatter based on the selected currency
     * @param currencyCode The currency code (USD, EUR, GBP)
     */
    private void updateCurrencyFormat(String currencyCode) {
        Locale locale;
        switch (currencyCode) {
            case "EUR":
                locale = Locale.GERMANY;
                break;
            case "GBP":
                locale = Locale.UK;
                break;
            case "USD":
            default:
                locale = Locale.US;
                break;
        }
        
        currencyFormat = NumberFormat.getCurrencyInstance(locale);
        currencyFormat.setMinimumFractionDigits(2);
        currencyFormat.setMaximumFractionDigits(8);
    }
    
    /**
     * Converts a USD value to the currently selected currency
     * @param usdValue The value in USD
     * @return The value in the selected currency
     */
    private double convertCurrency(double usdValue) {
        switch (currentCurrency) {
            case "EUR":
                return usdValue * USD_TO_EUR;
            case "GBP":
                return usdValue * USD_TO_GBP;
            case "USD":
            default:
                return usdValue;
        }
    }
    
    /**
     * Refreshes the portfolio data with current prices.
     */
    public void refreshData() {
        SwingUtilities.invokeLater(() -> {
            try {
                // Clear existing rows
                tableModel.setRowCount(0);
                
                Map<String, Double> holdings = portfolio.getHoldings();
                if (holdings.isEmpty()) {
                    lastUpdatedLabel.setText("Last updated: " + java.time.LocalDateTime.now().toString());
                    totalValueLabel.setText("Total Portfolio Value: " + currencyFormat.format(0));
                    fiatBalanceLabel.setText("Fiat Balance: " + 
                            currencyFormat.format(convertCurrency(portfolio.getAccount().getBalance())));
                    return;
                }
                
                CryptoService service = new CryptoService();
                Map<String, Double> prices = service.getCurrentPrices();
                
                double totalValue = 0.0;
                
                // Add rows for each holding
                for (Map.Entry<String, Double> entry : holdings.entrySet()) {
                    String symbol = entry.getKey();
                    double amount = entry.getValue();
                    double purchasePrice = portfolio.getAveragePurchasePrice(symbol);
                    double currentPrice = prices.getOrDefault(symbol, 0.0);
                    
                    // Convert prices to selected currency
                    double convertedPurchasePrice = convertCurrency(purchasePrice);
                    double convertedCurrentPrice = convertCurrency(currentPrice);
                    double convertedValue = amount * convertedCurrentPrice;
                    
                    double profitLossPercent = 0.0;
                    
                    if (purchasePrice > 0 && currentPrice > 0) {
                        profitLossPercent = ((currentPrice - purchasePrice) / purchasePrice) * 100.0;
                    }
                    
                    totalValue += convertedValue;
                    
                    Object[] row = {
                        symbol,
                        String.format("%.8f", amount),
                        formatPrice(convertedPurchasePrice),
                        formatPrice(convertedCurrentPrice),
                        currencyFormat.format(convertedValue),
                        profitLossPercent
                    };
                    
                    tableModel.addRow(row);
                }
                
                // Update summary labels
                totalValueLabel.setText("Total Portfolio Value: " + currencyFormat.format(totalValue));
                fiatBalanceLabel.setText("Fiat Balance: " + 
                        currencyFormat.format(convertCurrency(portfolio.getAccount().getBalance())));
                lastUpdatedLabel.setText("Last updated: " + java.time.LocalDateTime.now().toString());
                
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, 
                        "Error refreshing portfolio data: " + ex.getMessage(),
                        "Refresh Error", 
                        JOptionPane.ERROR_MESSAGE);
            }
        });
    }
    
    /**
     * Formats a price value with appropriate precision.
     */
    private String formatPrice(double price) {
        Currency currency = currencyFormat.getCurrency();
        String symbol = currency.getSymbol();
        
        if (price < 0.01) {
            return String.format("%s%.8f", symbol, price);
        } else if (price < 1.0) {
            return String.format("%s%.6f", symbol, price);
        } else if (price < 1000) {
            return String.format("%s%.4f", symbol, price);
        } else {
            return String.format("%s%.2f", symbol, price);
        }
    }
    
    /**
     * Starts the automatic refresh timer.
     */
    private void startAutoRefresh() {
        if (refreshTimer != null) {
            refreshTimer.cancel();
        }
        
        refreshTimer = new Timer(true);
        refreshTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                refreshData();
            }
        }, REFRESH_INTERVAL, REFRESH_INTERVAL);
    }
    
    /**
     * Stops the automatic refresh timer.
     */
    public void stopAutoRefresh() {
        if (refreshTimer != null) {
            refreshTimer.cancel();
            refreshTimer = null;
        }
    }
    
    @Override
    public void removeNotify() {
        stopAutoRefresh();
        super.removeNotify();
    }
} 