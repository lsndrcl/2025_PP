package com.myapp.UI;
import com.myapp.User;
import javax.swing.*;
import java.awt.*;

public class MainFrame extends JFrame {
    private final AccountPanel accountPanel;
    private final TradingPanel tradingPanel;

    public MainFrame(User user) {
        super("JavaBankCrypto");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 600);
        setLayout(new BorderLayout());

        accountPanel = new AccountPanel(user.getAccount());
        tradingPanel = new TradingPanel(user.getPortfolio());

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Account", accountPanel);
        tabs.addTab("Trading", tradingPanel);

        add(tabs, BorderLayout.CENTER);
        setLocationRelativeTo(null);
    }
}
