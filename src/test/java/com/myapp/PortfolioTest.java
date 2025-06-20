package com.myapp;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Map;

/**
 * Unit tests for the Portfolio class.
 */
public class PortfolioTest {
    
    private Portfolio portfolio;
    private Account account;
    private static final double INITIAL_BALANCE = 1000.0;
    private static final String TEST_COIN = "BTC";
    
    @BeforeEach
    void setUp() {
        account = new Account();
        account.deposit(INITIAL_BALANCE, "Initial deposit");
        portfolio = new Portfolio(account);
    }
    
    @Test
    void testNewPortfolioHasNoHoldings() {
        assertTrue(portfolio.getHoldings().isEmpty());
    }
    
    @Test
    void testGetAccount() {
        assertEquals(account, portfolio.getAccount());
    }
    
    @Test
    void testBuyCrypto() {
        // Test buying crypto
        double amount = 0.5;
        double price = 100.0;
        portfolio.buyCrypto(TEST_COIN, amount, price);
        
        // Verify holdings
        Map<String, Double> holdings = portfolio.getHoldings();
        assertEquals(1, holdings.size());
        assertTrue(holdings.containsKey(TEST_COIN));
        assertEquals(amount, holdings.get(TEST_COIN), 0.001);
        
        // Verify account balance
        assertEquals(INITIAL_BALANCE - (amount * price), account.getBalance(), 0.001);
    }
    
    @Test
    void testInvalidBuyCrypto() {
        // Test buying with negative amount
        assertThrows(IllegalArgumentException.class, () -> {
            portfolio.buyCrypto(TEST_COIN, -1.0, 100.0);
        });
        
        // Test buying with zero amount
        assertThrows(IllegalArgumentException.class, () -> {
            portfolio.buyCrypto(TEST_COIN, 0.0, 100.0);
        });
        
        // Test buying with negative price
        assertThrows(IllegalArgumentException.class, () -> {
            portfolio.buyCrypto(TEST_COIN, 1.0, -100.0);
        });
        
        // Test buying with zero price
        assertThrows(IllegalArgumentException.class, () -> {
            portfolio.buyCrypto(TEST_COIN, 1.0, 0.0);
        });
        
        // Test buying with insufficient funds
        assertThrows(IllegalStateException.class, () -> {
            portfolio.buyCrypto(TEST_COIN, 1.0, INITIAL_BALANCE + 100.0);
        });
        
        // Verify no holdings were added
        assertTrue(portfolio.getHoldings().isEmpty());
    }
    
    @Test
    void testSellCrypto() {
        // First buy some crypto
        double buyAmount = 1.0;
        double buyPrice = 100.0;
        portfolio.buyCrypto(TEST_COIN, buyAmount, buyPrice);
        
        // Then sell part of it
        double sellAmount = 0.5;
        double sellPrice = 120.0;
        portfolio.sellCrypto(TEST_COIN, sellAmount, sellPrice);
        
        // Verify holdings
        Map<String, Double> holdings = portfolio.getHoldings();
        assertEquals(1, holdings.size());
        assertEquals(buyAmount - sellAmount, holdings.get(TEST_COIN), 0.001);
        
        // Verify account balance
        double expectedBalance = INITIAL_BALANCE - (buyAmount * buyPrice) + (sellAmount * sellPrice);
        assertEquals(expectedBalance, account.getBalance(), 0.001);
    }
    
    @Test
    void testSellAllCrypto() {
        // First buy some crypto
        double amount = 1.0;
        double buyPrice = 100.0;
        portfolio.buyCrypto(TEST_COIN, amount, buyPrice);
        
        // Then sell all of it
        double sellPrice = 120.0;
        portfolio.sellCrypto(TEST_COIN, amount, sellPrice);
        
        // Verify holdings are empty
        assertTrue(portfolio.getHoldings().isEmpty());
        
        // Verify account balance
        double expectedBalance = INITIAL_BALANCE - (amount * buyPrice) + (amount * sellPrice);
        assertEquals(expectedBalance, account.getBalance(), 0.001);
    }
    
    @Test
    void testInvalidSellCrypto() {
        // First buy some crypto
        double amount = 1.0;
        double price = 100.0;
        portfolio.buyCrypto(TEST_COIN, amount, price);
        
        // Test selling with negative amount
        assertThrows(IllegalArgumentException.class, () -> {
            portfolio.sellCrypto(TEST_COIN, -0.5, price);
        });
        
        // Test selling with zero amount
        assertThrows(IllegalArgumentException.class, () -> {
            portfolio.sellCrypto(TEST_COIN, 0.0, price);
        });
        
        // Test selling with negative price
        assertThrows(IllegalArgumentException.class, () -> {
            portfolio.sellCrypto(TEST_COIN, 0.5, -price);
        });
        
        // Test selling with zero price
        assertThrows(IllegalArgumentException.class, () -> {
            portfolio.sellCrypto(TEST_COIN, 0.5, 0.0);
        });
        
        // Test selling more than owned
        assertThrows(IllegalStateException.class, () -> {
            portfolio.sellCrypto(TEST_COIN, amount + 0.1, price);
        });
        
        // Test selling non-existent coin
        assertThrows(IllegalStateException.class, () -> {
            portfolio.sellCrypto("ETH", 0.1, price);
        });
        
        // Verify holdings weren't changed
        Map<String, Double> holdings = portfolio.getHoldings();
        assertEquals(1, holdings.size());
        assertEquals(amount, holdings.get(TEST_COIN), 0.001);
    }
    
    @Test
    void testMultipleCoins() {
        // Buy multiple coins
        portfolio.buyCrypto("BTC", 0.5, 100.0);
        portfolio.buyCrypto("ETH", 2.0, 50.0);
        portfolio.buyCrypto("ADA", 100.0, 0.5);
        
        // Verify holdings
        Map<String, Double> holdings = portfolio.getHoldings();
        assertEquals(3, holdings.size());
        assertEquals(0.5, holdings.get("BTC"), 0.001);
        assertEquals(2.0, holdings.get("ETH"), 0.001);
        assertEquals(100.0, holdings.get("ADA"), 0.001);
        
        // Verify account balance
        double expectedBalance = INITIAL_BALANCE - (0.5 * 100.0) - (2.0 * 50.0) - (100.0 * 0.5);
        assertEquals(expectedBalance, account.getBalance(), 0.001);
        
        // Sell one coin completely
        portfolio.sellCrypto("ETH", 2.0, 60.0);
        
        // Verify updated holdings
        holdings = portfolio.getHoldings();
        assertEquals(2, holdings.size());
        assertFalse(holdings.containsKey("ETH"));
        
        // Verify updated balance
        expectedBalance += 2.0 * 60.0;
        assertEquals(expectedBalance, account.getBalance(), 0.001);
    }
} 