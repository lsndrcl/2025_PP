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
    private static final int MIN_USERNAME_LENGTH = 3;
    private static final int MIN_PASSWORD_LENGTH = 6;

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
            loggedInUser = userManager.loginUser(user, pass);

            if (loggedInUser != null) {
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Login failed", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        registerButton.addActionListener(e -> {
            String user = usernameField.getText();
            String pass = new String(passwordField.getPassword());
            
            // Validate username and password
            if (user.length() < MIN_USERNAME_LENGTH) {
                JOptionPane.showMessageDialog(this, 
                    "Username must be at least " + MIN_USERNAME_LENGTH + " characters long", 
                    "Invalid Input", JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            if (pass.length() < MIN_PASSWORD_LENGTH) {
                JOptionPane.showMessageDialog(this, 
                    "Password must be at least " + MIN_PASSWORD_LENGTH + " characters long", 
                    "Invalid Input", JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            try {
                User registered = userManager.registerUser(user, pass);
                if (registered != null) {
                    // Set the registered user as logged in and close dialog
                    loggedInUser = registered;
                    JOptionPane.showMessageDialog(this, 
                        "Registration successful! Welcome to the app.", 
                        "Success", JOptionPane.INFORMATION_MESSAGE);
                    dispose(); // Close the dialog to proceed to main app
                } else {
                    JOptionPane.showMessageDialog(this, 
                        "Username already exists. Please choose another username.", 
                        "Registration Failed", JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, 
                    "Registration failed: " + ex.getMessage(), 
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        // Add help text about requirements
        JPanel helpPanel = new JPanel();
        helpPanel.setLayout(new BorderLayout());
        JLabel helpText = new JLabel("<html><small>Username must be at least " + MIN_USERNAME_LENGTH + 
                                    " characters.<br>Password must be at least " + MIN_PASSWORD_LENGTH + 
                                    " characters.</small></html>");
        helpPanel.add(helpText, BorderLayout.CENTER);
        
        // Expand the layout to accommodate the help text
        setLayout(new BorderLayout());
        JPanel formPanel = new JPanel(new GridLayout(3, 2));
        formPanel.add(new JLabel("Username:"));
        formPanel.add(usernameField);
        formPanel.add(new JLabel("Password:"));
        formPanel.add(passwordField);
        formPanel.add(loginButton);
        formPanel.add(registerButton);
        
        add(formPanel, BorderLayout.CENTER);
        add(helpPanel, BorderLayout.SOUTH);

        pack();
        setSize(350, 180); // Set a reasonable size for the dialog
        setLocationRelativeTo(parent);
    }

    public User getLoggedInUser() {
        return loggedInUser;
    }
}
