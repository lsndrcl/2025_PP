package com.myapp.UI;

import com.myapp.TransactionType;

import javax.swing.*;
import java.awt.*;

public class TransactionFilterDialog extends JDialog {
    private TransactionFilter filter;
    private JComboBox<TransactionType> typeBox;
    private JTextField minField, maxField;

    public TransactionFilterDialog(Frame parent) {
        super(parent, "Filter Transactions", true);
        setLayout(new GridLayout(4, 2));

        typeBox = new JComboBox<>(TransactionType.values());
        minField = new JTextField();
        maxField = new JTextField();

        add(new JLabel("Type:")); add(typeBox);
        add(new JLabel("Min Amount:")); add(minField);
        add(new JLabel("Max Amount:")); add(maxField);

        JButton apply = new JButton("Apply");
        add(apply);

        apply.addActionListener(e -> {
            filter = new TransactionFilter();
            try {
                if (!minField.getText().isEmpty())
                    filter.setMinAmount(Double.parseDouble(minField.getText()));
                if (!maxField.getText().isEmpty())
                    filter.setMaxAmount(Double.parseDouble(maxField.getText()));
                filter.setType((TransactionType) typeBox.getSelectedItem());
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Invalid input", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            dispose();
        });

        pack();
        setLocationRelativeTo(parent);
    }

    public TransactionFilter getFilter() {
        return filter;
    }
}
