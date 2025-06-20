package com.myapp.UI;

import com.myapp.CryptoService;
import com.myapp.Portfolio;
import com.myapp.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import javax.swing.*;
import java.awt.*;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Unit tests for the TradingPanel class.
 * Note: These tests use reflection to test internal methods without displaying UI.
 */
public class TradingPanelTest {
    
    private TradingPanel tradingPanel;
    private User testUser;
    private Portfolio portfolio;
    
    @BeforeEach
    void setUp() {
        testUser = new User("testUser", "hashedPassword");
        portfolio = testUser.getPortfolio();
        tradingPanel = new TradingPanel(portfolio);
    }
    
    @Test
    void testPanelInitialization() {
        // Verify panel is initialized
        assertNotNull(tradingPanel);
    }
    
    @Test
    void testCoinBox() throws Exception {
        // Get the private field using reflection
        Field coinBox = TradingPanel.class.getDeclaredField("coinBox");
        coinBox.setAccessible(true);
        JComboBox<String> comboBox = (JComboBox<String>) coinBox.get(tradingPanel);
        
        // Verify combo box is initialized with the expected items
        assertNotNull(comboBox);
        assertEquals(3, comboBox.getItemCount());
        assertTrue(comboBox.getItemAt(0).equals("BTC") || 
                   comboBox.getItemAt(1).equals("BTC") || 
                   comboBox.getItemAt(2).equals("BTC"));
        assertTrue(comboBox.getItemAt(0).equals("ETH") || 
                   comboBox.getItemAt(1).equals("ETH") || 
                   comboBox.getItemAt(2).equals("ETH"));
        assertTrue(comboBox.getItemAt(0).equals("ADA") || 
                   comboBox.getItemAt(1).equals("ADA") || 
                   comboBox.getItemAt(2).equals("ADA"));
    }
    
    @Test
    void testAmountField() throws Exception {
        // Get the private field using reflection
        Field amountField = TradingPanel.class.getDeclaredField("amountField");
        amountField.setAccessible(true);
        JTextField field = (JTextField) amountField.get(tradingPanel);
        
        // Verify field is initialized
        assertNotNull(field);
    }
    
    @Test
    void testOutputArea() throws Exception {
        // Get the private field using reflection
        Field outputArea = TradingPanel.class.getDeclaredField("outputArea");
        outputArea.setAccessible(true);
        JTextArea area = (JTextArea) outputArea.get(tradingPanel);
        
        // Verify area is initialized
        assertNotNull(area);
    }
    
    @Test
    void testPortfolioReference() throws Exception {
        // Get the private field using reflection
        Field portfolioField = TradingPanel.class.getDeclaredField("portfolio");
        portfolioField.setAccessible(true);
        Portfolio panelPortfolio = (Portfolio) portfolioField.get(tradingPanel);
        
        // Verify portfolio reference is correct
        assertSame(portfolio, panelPortfolio);
    }
    
    @Test
    void testCancelRequestedFlag() throws Exception {
        // Get the private field using reflection
        Field cancelRequested = TradingPanel.class.getDeclaredField("cancelRequested");
        cancelRequested.setAccessible(true);
        AtomicBoolean flag = (AtomicBoolean) cancelRequested.get(tradingPanel);
        
        // Verify flag is initialized to false
        assertNotNull(flag);
        assertFalse(flag.get());
    }
    
    @Test
    void testCurrencyFormat() throws Exception {
        // Get the private field using reflection
        Field currencyFormat = TradingPanel.class.getDeclaredField("currencyFormat");
        currencyFormat.setAccessible(true);
        java.text.NumberFormat format = (java.text.NumberFormat) currencyFormat.get(tradingPanel);
        
        // Verify format is initialized
        assertNotNull(format);
    }
} 