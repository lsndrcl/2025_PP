package com.myapp.UI;
import com.myapp.User;
import javax.swing.*;
import java.awt.*;

public class MainFrame extends JFrame {
    private final AccountPanel accountPanel;
    private final TradingPanel tradingPanel;
    private final User currentUser;

    public MainFrame(User user) {
        super("JavaBankCrypto");
        this.currentUser = user;
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 600);
        setLayout(new BorderLayout());

        // Create header panel with username display
        JPanel headerPanel = createHeaderPanel();
        add(headerPanel, BorderLayout.NORTH);

        accountPanel = new AccountPanel(user.getAccount());
        tradingPanel = new TradingPanel(user.getPortfolio());

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Account", accountPanel);
        tabs.addTab("Trading", tradingPanel);

        add(tabs, BorderLayout.CENTER);
        setLocationRelativeTo(null);
    }
    
    /**
     * Creates a header panel with username display and other potential controls
     */
    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(new BorderLayout());
        headerPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        
        // App name on the left
        JLabel appNameLabel = new JLabel("JavaBankCrypto");
        appNameLabel.setFont(new Font("Arial", Font.BOLD, 16));
        headerPanel.add(appNameLabel, BorderLayout.WEST);
        
        // User info on the right
        JPanel userInfoPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        
        // Username with icon (text-based)
        JLabel userIcon = new JLabel("👤");
        userIcon.setFont(new Font("Arial", Font.PLAIN, 16));
        
        JLabel usernameLabel = new JLabel("Logged in as: " + currentUser.getUsername());
        usernameLabel.setFont(new Font("Arial", Font.BOLD, 14));
        
        userInfoPanel.add(userIcon);
        userInfoPanel.add(usernameLabel);
        
        // Add logout button
        JButton logoutButton = new JButton("Logout");
        logoutButton.addActionListener(e -> {
            int choice = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to logout?",
                "Confirm Logout",
                JOptionPane.YES_NO_OPTION
            );
            
            if (choice == JOptionPane.YES_OPTION) {
                dispose();
                // Show login dialog again
                SwingUtilities.invokeLater(() -> {
                    try {
                        // Create a new login dialog
                        com.myapp.auth.UserManager userManager = new com.myapp.auth.UserManager();
                        LoginDialog loginDialog = new LoginDialog(null, userManager);
                        loginDialog.setVisible(true);
                        
                        User user = loginDialog.getLoggedInUser();
                        if (user != null) {
                            MainFrame app = new MainFrame(user);
                            app.setVisible(true);
                        } else {
                            System.exit(0);
                        }
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(null, 
                            "Error during logout: " + ex.getMessage(), 
                            "Error", 
                            JOptionPane.ERROR_MESSAGE);
                        System.exit(1);
                    }
                });
            }
        });
        
        userInfoPanel.add(Box.createHorizontalStrut(10));
        userInfoPanel.add(logoutButton);
        
        headerPanel.add(userInfoPanel, BorderLayout.EAST);
        
        // Add a separator line
        JSeparator separator = new JSeparator();
        JPanel separatorPanel = new JPanel(new BorderLayout());
        separatorPanel.add(separator, BorderLayout.CENTER);
        separatorPanel.setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 0));
        
        JPanel combinedPanel = new JPanel(new BorderLayout());
        combinedPanel.add(headerPanel, BorderLayout.CENTER);
        combinedPanel.add(separatorPanel, BorderLayout.SOUTH);
        
        return combinedPanel;
    }
}
