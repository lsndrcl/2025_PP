package com.myapp.UI;

import com.myapp.User;
import com.myapp.auth.UserManager;

import javax.swing.*;
import java.awt.*;

public class LoginDialog extends JDialog {
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton loginButton, registerButton;
    private User loggedInUser;

    public LoginDialog(Frame parent, UserManager userManager) {
        super(parent, "Login", true);
        setLayout(new GridLayout(3, 2));

        add(new JLabel("Username:"));
        usernameField = new JTextField();
        add(usernameField);

        add(new JLabel("Password:"));
        passwordField = new JPasswordField();
        add(passwordField);

        loginButton = new JButton("Login");
        registerButton = new JButton("Register");
        add(loginButton);
        add(registerButton);

        loginButton.addActionListener(e -> {
            String user = usernameField.getText();
            String pass = new String(passwordField.getPassword());
            loggedInUser = userManager.loginUser(user, pass);  // FIXED

            if (loggedInUser != null) {
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Login failed", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        registerButton.addActionListener(e -> {
            String user = usernameField.getText();
            String pass = new String(passwordField.getPassword());
            try {
                User registered = userManager.registerUser(user, pass);  // FIXED
                if (registered != null) {
                    JOptionPane.showMessageDialog(this, "Registered successfully!", "Info", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(this, "Username already exists", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Registration failed: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        pack();
        setLocationRelativeTo(parent);
    }

    public User getLoggedInUser() {
        return loggedInUser;
    }
}
