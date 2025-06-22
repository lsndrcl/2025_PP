package com.myapp.UI;

import com.myapp.Account;
import com.myapp.Transaction;
import com.myapp.TransactionFilter;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.Currency;
import java.util.List;
import java.util.Locale;

public class AccountPanel extends JPanel {
    private final Account account;
    private final JTable table;
    private final DefaultTableModel model;
    private final JLabel balanceLabel;
    private NumberFormat currencyFormat;
    
    // Currency conversion rates (approximate)
    private static final double USD_TO_EUR = 0.93;
    private static final double USD_TO_GBP = 0.79;
    
    // Current currency selection
    private String currentCurrency = "USD";

    public AccountPanel(Account account) {
        this.account = account;
        setLayout(new BorderLayout());
        
        // Initialize currency formatter
        currencyFormat = NumberFormat.getCurrencyInstance(Locale.US);
        currencyFormat.setMinimumFractionDigits(2);
        currencyFormat.setMaximumFractionDigits(2);

        // Create top panel with GridBagLayout for better control
        JPanel topPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // Balance label
        balanceLabel = new JLabel();
        updateBalanceLabel();
        balanceLabel.setFont(new Font("Arial", Font.BOLD, 14));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.2;
        gbc.anchor = GridBagConstraints.WEST;
        topPanel.add(balanceLabel, gbc);
        
        // Currency selector
        JPanel currencyPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        JLabel currencyLabel = new JLabel("Currency:");
        String[] currencies = {"USD", "EUR", "GBP"};
        JComboBox<String> currencySelector = new JComboBox<>(currencies);
        currencySelector.setPreferredSize(new Dimension(80, 25));
        currencySelector.addActionListener(e -> {
            currentCurrency = (String) currencySelector.getSelectedItem();
            updateCurrencyFormat(currentCurrency);
            updateBalanceLabel();
            refreshTable(account.getTransactions());
        });
        
        currencyPanel.add(currencyLabel);
        currencyPanel.add(currencySelector);
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.weightx = 0.2;
        topPanel.add(currencyPanel, gbc);
        
        // Create button panel with FlowLayout to keep buttons visible
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new WrapLayout(FlowLayout.RIGHT, 5, 0)); // Using WrapLayout for better wrapping
        
        // Create buttons with consistent size
        Dimension buttonSize = new Dimension(100, 30);
        
        JButton depositBtn = new JButton("Deposit");
        depositBtn.setPreferredSize(buttonSize);
        
        JButton withdrawBtn = new JButton("Withdraw");
        withdrawBtn.setPreferredSize(buttonSize);
        
        JButton importBtn = new JButton("Import JSON");
        importBtn.setPreferredSize(buttonSize);
        
        JButton exportBtn = new JButton("Export JSON");
        exportBtn.setPreferredSize(buttonSize);
        
        JButton filterBtn = new JButton("Filter");
        filterBtn.setPreferredSize(buttonSize);
        
        JButton recentBtn = new JButton("Recent 5");
        recentBtn.setPreferredSize(buttonSize);
        
        JButton allBtn = new JButton("All");
        allBtn.setPreferredSize(buttonSize);

        buttonPanel.add(depositBtn);
        buttonPanel.add(withdrawBtn);
        buttonPanel.add(importBtn);
        buttonPanel.add(exportBtn);
        buttonPanel.add(filterBtn);
        buttonPanel.add(recentBtn);
        buttonPanel.add(allBtn);
        
        gbc.gridx = 2;
        gbc.gridy = 0;
        gbc.weightx = 0.6;
        gbc.anchor = GridBagConstraints.EAST;
        topPanel.add(buttonPanel, gbc);
        
        add(topPanel, BorderLayout.NORTH);

        // Center: table
        model = new DefaultTableModel(new String[]{"ID", "Time", "Type", "Amount", "Description"}, 0);
        table = new JTable(model);
        
        // Custom renderer for amount column
        DefaultTableCellRenderer amountRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, 
                    boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                
                if (value instanceof Double) {
                    double amount = (Double) value;
                    setText(formatCurrency(amount));
                }
                return c;
            }
        };
        amountRenderer.setHorizontalAlignment(SwingConstants.RIGHT);
        table.getColumnModel().getColumn(3).setCellRenderer(amountRenderer);
        
        add(new JScrollPane(table), BorderLayout.CENTER);

        // Button actions
        depositBtn.addActionListener(e -> handleDeposit());
        withdrawBtn.addActionListener(e -> handleWithdraw());
        importBtn.addActionListener(e -> handleImport());
        exportBtn.addActionListener(e -> handleExport());
        filterBtn.addActionListener(e -> openFilterDialog());
        recentBtn.addActionListener(e -> refreshTable(account.getRecentTransactions(5)));
        allBtn.addActionListener(e -> refreshTable(account.getTransactions()));

        refreshTable(account.getTransactions());
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
        currencyFormat.setMaximumFractionDigits(2);
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
     * Formats a value according to the current currency format
     * @param value The value to format
     * @return Formatted currency string
     */
    private String formatCurrency(double value) {
        return currencyFormat.format(convertCurrency(value));
    }

    private void updateBalanceLabel() {
        balanceLabel.setText("Balance: " + formatCurrency(account.getBalance()));
    }

    private void handleDeposit() {
        String input = JOptionPane.showInputDialog(this, "Enter amount to deposit:");
        if (input != null) {
            try {
                double amount = Double.parseDouble(input);
                String desc = JOptionPane.showInputDialog(this, "Enter description:");
                account.deposit(amount, desc != null ? desc : "Deposit");
                refreshTable(account.getTransactions());
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Error: " + e.getMessage(), "Invalid Input", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void handleWithdraw() {
        String input = JOptionPane.showInputDialog(this, "Enter amount to withdraw:");
        if (input != null) {
            try {
                double amount = Double.parseDouble(input);
                String desc = JOptionPane.showInputDialog(this, "Enter description:");
                account.withdraw(amount, desc != null ? desc : "Withdrawal");
                refreshTable(account.getTransactions());
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Error: " + e.getMessage(), "Invalid Input", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void handleImport() {
        JFileChooser chooser = new JFileChooser();
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                account.importTransactionsFromJson(chooser.getSelectedFile().getAbsolutePath());
                refreshTable(account.getTransactions());
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Failed to import transactions:\n" + ex.getMessage(), "Import Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void handleExport() {
        JFileChooser chooser = new JFileChooser();
        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                account.exportTransactionsToJson(chooser.getSelectedFile().getAbsolutePath());
                JOptionPane.showMessageDialog(this, "Export successful!");
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Failed to export transactions:\n" + ex.getMessage(), "Export Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void openFilterDialog() {
        TransactionFilterDialog dialog = new TransactionFilterDialog(null);
        dialog.setVisible(true);

        com.myapp.TransactionFilter filter = dialog.getFilter();
        if (filter != null) {
            List<Transaction> filtered = account.searchTransactions(filter);
            refreshTable(filtered);
        }
    }

    private void refreshTable(List<Transaction> transactions) {
        model.setRowCount(0);
        for (Transaction t : transactions) {
            model.addRow(new Object[]{
                    t.getTransactionId(),
                    t.getTimestamp(),
                    t.getType(),
                    t.getAmount(), // Will be formatted by the custom renderer
                    t.getDescription()
            });
        }
        updateBalanceLabel();
    }
    
    /**
     * A FlowLayout subclass that supports wrapping of components
     */
    private class WrapLayout extends FlowLayout {
        public WrapLayout() {
            super();
        }

        public WrapLayout(int align) {
            super(align);
        }

        public WrapLayout(int align, int hgap, int vgap) {
            super(align, hgap, vgap);
        }

        @Override
        public Dimension preferredLayoutSize(Container target) {
            return layoutSize(target, true);
        }

        @Override
        public Dimension minimumLayoutSize(Container target) {
            return layoutSize(target, false);
        }

        private Dimension layoutSize(Container target, boolean preferred) {
            synchronized (target.getTreeLock()) {
                int targetWidth = target.getSize().width;
                
                if (targetWidth == 0)
                    targetWidth = Integer.MAX_VALUE;
                
                int hgap = getHgap();
                int vgap = getVgap();
                Insets insets = target.getInsets();
                int horizontalInsetsAndGap = insets.left + insets.right + (hgap * 2);
                int maxWidth = targetWidth - horizontalInsetsAndGap;

                Dimension dim = new Dimension(0, 0);
                int rowWidth = 0;
                int rowHeight = 0;

                int nmembers = target.getComponentCount();

                for (int i = 0; i < nmembers; i++) {
                    Component m = target.getComponent(i);
                    
                    if (m.isVisible()) {
                        Dimension d = preferred ? m.getPreferredSize() : m.getMinimumSize();
                        
                        if (rowWidth + d.width > maxWidth) {
                            addRow(dim, rowWidth, rowHeight);
                            rowWidth = 0;
                            rowHeight = 0;
                        }
                        
                        if (rowWidth != 0) {
                            rowWidth += hgap;
                        }
                        
                        rowWidth += d.width;
                        rowHeight = Math.max(rowHeight, d.height);
                    }
                }
                
                addRow(dim, rowWidth, rowHeight);
                
                dim.width += horizontalInsetsAndGap;
                dim.height += insets.top + insets.bottom + vgap * 2;
                
                return dim;
            }
        }

        private void addRow(Dimension dim, int rowWidth, int rowHeight) {
            dim.width = Math.max(dim.width, rowWidth);
            
            if (dim.height > 0) {
                dim.height += getVgap();
            }
            
            dim.height += rowHeight;
        }
    }
}
