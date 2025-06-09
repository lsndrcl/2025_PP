package com.myapp.UI;

import com.myapp.Account;
import com.myapp.Transaction;
import com.myapp.TransactionFilter;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.IOException;
import java.util.List;

public class AccountPanel extends JPanel {
    private final Account account;
    private final JTable table;
    private final DefaultTableModel model;
    private final JLabel balanceLabel;

    public AccountPanel(Account account) {
        this.account = account;
        setLayout(new BorderLayout());

        // Top: balance + buttons
        JPanel topPanel = new JPanel(new BorderLayout());
        balanceLabel = new JLabel();
        updateBalanceLabel();

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton depositBtn = new JButton("Deposit");
        JButton withdrawBtn = new JButton("Withdraw");
        JButton importBtn = new JButton("Import JSON");
        JButton exportBtn = new JButton("Export JSON");
        JButton filterBtn = new JButton("Filter");
        JButton recentBtn = new JButton("Recent 5");
        JButton allBtn = new JButton("All");

        buttonPanel.add(depositBtn);
        buttonPanel.add(withdrawBtn);
        buttonPanel.add(importBtn);
        buttonPanel.add(exportBtn);
        buttonPanel.add(filterBtn);
        buttonPanel.add(recentBtn);
        buttonPanel.add(allBtn);

        topPanel.add(balanceLabel, BorderLayout.WEST);
        topPanel.add(buttonPanel, BorderLayout.EAST);
        add(topPanel, BorderLayout.NORTH);

        // Center: table
        model = new DefaultTableModel(new String[]{"ID", "Time", "Type", "Amount", "Description"}, 0);
        table = new JTable(model);
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

    private void updateBalanceLabel() {
        balanceLabel.setText("Balance: $" + String.format("%.2f", account.getBalance()));
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

        TransactionFilter filter = (TransactionFilter) dialog.getFilter();
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
                    t.getAmount(),
                    t.getDescription()
            });
        }
        updateBalanceLabel();
    }
}
