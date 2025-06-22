package com.myapp.UI;
import com.myapp.User;
import com.myapp.auth.UserManager;
import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class MainFrame extends JFrame {
    private final AccountPanel accountPanel;
    private final TradingPanel tradingPanel;
    private final User currentUser;
    private final UserManager userManager;

    public MainFrame(User user, UserManager userManager) {
        super("JavaBankCrypto");
        this.currentUser = user;
        this.userManager = userManager;
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 700); // Increased height for better visibility
        setMinimumSize(new Dimension(800, 600)); // Set minimum size to ensure components are visible
        setLayout(new BorderLayout());

        // Add window listener to save data on exit
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                saveDataOnExit();
            }
        });

        // Create header panel with username display
        JPanel headerPanel = createHeaderPanel();
        add(headerPanel, BorderLayout.NORTH);

        accountPanel = new AccountPanel(user.getAccount());
        tradingPanel = new TradingPanel(user.getPortfolio());

        // Create tabbed pane with custom styling
        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(new Font("Arial", Font.BOLD, 14));
        tabs.addTab("Account", new ImageIcon(), accountPanel, "View and manage your account");
        tabs.addTab("Trading", new ImageIcon(), tradingPanel, "Buy and sell cryptocurrencies");
        
        // Set preferred size for tabs to ensure they're visible
        tabs.setPreferredSize(new Dimension(800, 600));

        add(tabs, BorderLayout.CENTER);
        
        // Create a status bar
        JPanel statusBar = new JPanel(new BorderLayout());
        statusBar.setBorder(BorderFactory.createEtchedBorder());
        JLabel statusLabel = new JLabel("Ready");
        statusBar.add(statusLabel, BorderLayout.WEST);
        add(statusBar, BorderLayout.SOUTH);
        
        // Center the window on screen
        setLocationRelativeTo(null);
        
        // Set UI properties to improve appearance
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            SwingUtilities.updateComponentTreeUI(this);
        } catch (Exception e) {
            System.err.println("Could not set system look and feel: " + e.getMessage());
        }
    }
    
    /**
     * Creates a header panel with username display and other potential controls
     */
    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(new BorderLayout());
        headerPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createEtchedBorder(),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        
        // App name on the left
        JLabel appNameLabel = new JLabel("JavaBankCrypto");
        appNameLabel.setFont(new Font("Arial", Font.BOLD, 18));
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
        
        // Add logout button with improved styling
        JButton logoutButton = new JButton("Logout");
        logoutButton.setFocusPainted(false);
        logoutButton.addActionListener(e -> logout());
        userInfoPanel.add(logoutButton);
        
        headerPanel.add(userInfoPanel, BorderLayout.EAST);
        
        return headerPanel;
    }
    
    /**
     * Handles user logout
     */
    private void logout() {
        // Save data before logout
        saveDataOnExit();
        
        // Close this window
        dispose();
        
        // Show login dialog again
        SwingUtilities.invokeLater(() -> {
            LoginDialog loginDialog = new LoginDialog(null, userManager);
            loginDialog.setVisible(true);
            
            User user = loginDialog.getLoggedInUser();
            if (user != null) {
                MainFrame app = new MainFrame(user, userManager);
                app.setVisible(true);
            } else {
                System.exit(0); // Exit if login canceled
            }
        });
    }
    
    /**
     * Saves all user data before exit
     */
    private void saveDataOnExit() {
        try {
            userManager.saveAllUserData();
            System.out.println("Data saved successfully");
        } catch (Exception ex) {
            System.err.println("Error saving data: " + ex.getMessage());
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this,
                "There was an error saving your data: " + ex.getMessage(),
                "Save Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
