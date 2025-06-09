package com.myapp;

import com.myapp.UI.LoginDialog;
import com.myapp.UI.MainFrame;
import com.myapp.auth.UserManager;
import javax.swing.*;
// import java.util.Scanner; // Uncomment for console use

public class Main {
    // private static final Scanner scanner = new Scanner(System.in);
    private static final UserManager userManager = new UserManager();

    public static void main(String[] args) {
        // === GUI Mode ===
        SwingUtilities.invokeLater(() -> {
            LoginDialog loginDialog = new LoginDialog(null, userManager);
            loginDialog.setVisible(true);

            User user = loginDialog.getLoggedInUser();
            if (user != null) {
                MainFrame app = new MainFrame(user);
                app.setVisible(true);
            } else {
                System.exit(0); // Exit if login canceled
            }
        });

        /* === Console Mode (Commented Out) ===
        System.out.println("Welcome to the Java Bank Crypto Application!");
        while (true) {
            if (currentUser == null) {
                showLoginMenu();
            } else {
                showMainMenu();
            }
        }
        */
    }

    /* ===== Console Mode Variables & Methods (Commented Out) =====

    private static User currentUser = null;

    private static void showLoginMenu() {
        System.out.println("\n===== Authentication =====");
        System.out.println("1. Login");
        System.out.println("2. Register");
        System.out.println("3. Exit");
        System.out.print("Choose an option (1-3): ");

        String choice = scanner.nextLine();

        switch (choice) {
            case "1": login(); break;
            case "2": register(); break;
            case "3":
                System.out.println("Thank you for using Java Bank Crypto Application!");
                System.exit(0);
                break;
            default: System.out.println("Invalid option. Please try again.");
        }
    }

    private static void showMainMenu() {
        System.out.println("\n===== Main Menu =====");
        System.out.println("Welcome, " + currentUser.getUsername() + "!");
        System.out.println("Account Balance: $" + String.format("%.2f", currentUser.getAccount().getBalance()));
        System.out.println("1. View Account Details");
        System.out.println("2. Deposit Funds");
        System.out.println("3. Withdraw Funds");
        System.out.println("4. View Transaction History");
        System.out.println("5. View Portfolio");
        System.out.println("6. Trade Cryptocurrency");
        System.out.println("7. Logout");
        System.out.println("8. Exit");
        System.out.print("Choose an option (1-8): ");

        String choice = scanner.nextLine();

        try {
            switch (choice) {
                case "1": viewAccountDetails(); break;
                case "2": depositFunds(); break;
                case "3": withdrawFunds(); break;
                case "4": viewTransactionHistory(); break;
                case "5": viewPortfolio(); break;
                case "6": tradeCrypto(); break;
                case "7": logout(); break;
                case "8": saveAndExit(); break;
                default: System.out.println("Invalid option. Please try again.");
            }
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    // Add other console methods here like login(), register(), depositFunds(), etc.
    */

}
