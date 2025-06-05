package com.myapp;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;
import java.util.List;

/**
 * Unit tests for the TransactionFilter functionality.
 */
public class TransactionFilterTest {
    
    private Account account;
    
    @BeforeEach
    void setUp() {
        // Create a new Account instance and add some test transactions
        account = new Account();
        account.deposit(100.0, "First deposit");
        account.withdraw(25.0, "First withdrawal");
        account.deposit(200.0, "Second deposit");
        account.withdraw(50.0, "Second withdrawal");
    }
    
    @Test
    void testFilterByType() {
        // Create a filter that matches only deposits
        TransactionFilter depositFilter = new TransactionFilter() {
            @Override
            public boolean matches(Transaction transaction) {
                return transaction.getType() == TransactionType.DEPOSIT;
            }
        };
        
        // Test the filter
        List<Transaction> deposits = account.searchTransactions(depositFilter);
        assertEquals(2, deposits.size());
        for (Transaction t : deposits) {
            assertEquals(TransactionType.DEPOSIT, t.getType());
        }
    }
    
    @Test
    void testFilterByAmount() {
        // Create a filter that matches transactions with amount > 50
        TransactionFilter largeAmountFilter = new TransactionFilter() {
            @Override
            public boolean matches(Transaction transaction) {
                return transaction.getAmount() > 50.0;
            }
        };
        
        // Test the filter
        List<Transaction> largeTransactions = account.searchTransactions(largeAmountFilter);
        assertEquals(2, largeTransactions.size()); // Only two transactions have amount > 50: 100.0 and 200.0
        for (Transaction t : largeTransactions) {
            assertTrue(t.getAmount() > 50.0);
        }
    }
    
    @Test
    void testFilterByDescription() {
        // Create a filter that matches transactions with "Second" in the description
        TransactionFilter descriptionFilter = new TransactionFilter() {
            @Override
            public boolean matches(Transaction transaction) {
                return transaction.getDescription().contains("Second");
            }
        };
        
        // Test the filter
        List<Transaction> secondTransactions = account.searchTransactions(descriptionFilter);
        assertEquals(2, secondTransactions.size());
        for (Transaction t : secondTransactions) {
            assertTrue(t.getDescription().contains("Second"));
        }
    }
    
    @Test
    void testComplexFilter() {
        // Create a filter that matches deposits over 150
        TransactionFilter complexFilter = new TransactionFilter() {
            @Override
            public boolean matches(Transaction transaction) {
                return transaction.getType() == TransactionType.DEPOSIT && 
                       transaction.getAmount() > 150.0;
            }
        };
        
        // Test the filter
        List<Transaction> largeDeposits = account.searchTransactions(complexFilter);
        assertEquals(1, largeDeposits.size());
        Transaction t = largeDeposits.get(0);
        assertEquals(TransactionType.DEPOSIT, t.getType());
        assertTrue(t.getAmount() > 150.0);
        assertEquals("Second deposit", t.getDescription());
    }
} 