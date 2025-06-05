package com.myapp;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;
import java.util.List;

/**
 * Unit tests for the Account class.
 */
public class AccountTest {
    
    private Account account;
    
    @BeforeEach
    void setUp() {
        // Create a new Account instance before each test
        account = new Account();
    }
    
    @Test
    void testNewAccountHasZeroBalance() {
        assertEquals(0.0, account.getBalance(), 0.001);
    }
    
    @Test
    void testNewAccountHasNoTransactions() {
        assertTrue(account.getTransactions().isEmpty());
    }
    
    @Test
    void testDeposit() {
        // Test depositing a valid amount
        account.deposit(100.0, "Test deposit");
        assertEquals(100.0, account.getBalance(), 0.001);
        
        // Verify transaction was recorded
        List<Transaction> transactions = account.getTransactions();
        assertEquals(1, transactions.size());
        Transaction transaction = transactions.get(0);
        assertEquals(TransactionType.DEPOSIT, transaction.getType());
        assertEquals(100.0, transaction.getAmount(), 0.001);
        assertEquals("Test deposit", transaction.getDescription());
    }
    
    @Test
    void testInvalidDeposit() {
        // Test depositing a negative amount
        assertThrows(IllegalArgumentException.class, () -> {
            account.deposit(-50.0, "Negative deposit");
        });
        
        // Test depositing zero
        assertThrows(IllegalArgumentException.class, () -> {
            account.deposit(0.0, "Zero deposit");
        });
        
        // Verify no transactions were recorded
        assertEquals(0, account.getTransactions().size());
    }
    
    @Test
    void testWithdraw() {
        // First deposit some money
        account.deposit(200.0, "Initial deposit");
        
        // Test withdrawing a valid amount
        account.withdraw(50.0, "Test withdrawal");
        assertEquals(150.0, account.getBalance(), 0.001);
        
        // Verify transaction was recorded
        List<Transaction> transactions = account.getTransactions();
        assertEquals(2, transactions.size());
        Transaction transaction = transactions.get(1);
        assertEquals(TransactionType.WITHDRAWAL, transaction.getType());
        assertEquals(50.0, transaction.getAmount(), 0.001);
        assertEquals("Test withdrawal", transaction.getDescription());
    }
    
    @Test
    void testInvalidWithdraw() {
        // First deposit some money
        account.deposit(100.0, "Initial deposit");
        
        // Test withdrawing more than the balance
        assertThrows(IllegalStateException.class, () -> {
            account.withdraw(150.0, "Excessive withdrawal");
        });
        
        // Test withdrawing a negative amount
        assertThrows(IllegalArgumentException.class, () -> {
            account.withdraw(-50.0, "Negative withdrawal");
        });
        
        // Test withdrawing zero
        assertThrows(IllegalArgumentException.class, () -> {
            account.withdraw(0.0, "Zero withdrawal");
        });
        
        // Verify only the initial deposit was recorded
        assertEquals(1, account.getTransactions().size());
        assertEquals(100.0, account.getBalance(), 0.001);
    }
    
    @Test
    void testGetRecentTransactions() {
        // Add several transactions
        account.deposit(100.0, "First deposit");
        account.deposit(200.0, "Second deposit");
        account.withdraw(50.0, "First withdrawal");
        account.deposit(300.0, "Third deposit");
        
        // Test getting the 2 most recent transactions
        List<Transaction> recentTransactions = account.getRecentTransactions(2);
        assertEquals(2, recentTransactions.size());
        assertEquals("First withdrawal", recentTransactions.get(0).getDescription());
        assertEquals("Third deposit", recentTransactions.get(1).getDescription());
    }
} 