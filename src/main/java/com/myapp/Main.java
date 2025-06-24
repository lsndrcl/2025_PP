package com.myapp;

import com.myapp.UI.LoginDialog;
import com.myapp.UI.MainFrame;
import com.myapp.auth.UserManager;
import javax.swing.*;

/**
 * Entry point of the application.
 *
 * <p>This class supports two modes of operation:
 * <ul>
 *   <li>GUI mode: launches a Swing-based login dialog and main application window.</li>
 *   <li>CLI mode: starts a command-line interface for trading (comment/uncomment in code).</li>
 * </ul>
 * </p>
 */
public class Main {

    private static final UserManager userManager = new UserManager();

    public static void main(String[] args) {

        /**
         * Main method to start the application.
         *
         * <p>By default, launches the GUI mode asynchronously on the Event Dispatch Thread.
         * To use the CLI mode, comment out the GUI block and uncomment the CLI block.</p>
         *
         * @param args command-line arguments (not used)
         */
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


        /* ===CLI Mode== -> uncomment to use CLI
        Account account = new Account();
        Portfolio portfolio = new Portfolio(account);
        TradingCLI cli = new TradingCLI(portfolio);
        cli.start();
        */

    }
}
