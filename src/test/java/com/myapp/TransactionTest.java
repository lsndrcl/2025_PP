package com.myapp;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;

/**
 * Unit tests for the Transaction class.
 */
public class TransactionTest {
    
    private static final TransactionType TEST_TYPE = TransactionType.DEPOSIT;
    private static final double TEST_AMOUNT = 100.0;
    private static final String TEST_DESCRIPTION = "Test transaction";
    
    @Test
    void testTransactionCreation() {
        Transaction transaction = new Transaction(TEST_TYPE, TEST_AMOUNT, TEST_DESCRIPTION);
        
        assertNotNull(transaction);
        assertEquals(TEST_TYPE, transaction.getType());
        assertEquals(TEST_AMOUNT, transaction.getAmount(), 0.001);
        assertEquals(TEST_DESCRIPTION, transaction.getDescription());
        assertNotNull(transaction.getTransactionId());
        assertNotNull(transaction.getTimestamp());
    }
    
    @Test
    void testTransactionTimestamp() {
        LocalDateTime before = LocalDateTime.now().minusSeconds(1);
        Transaction transaction = new Transaction(TEST_TYPE, TEST_AMOUNT, TEST_DESCRIPTION);
        LocalDateTime after = LocalDateTime.now().plusSeconds(1);
        
        // Verify timestamp is within the expected range
        assertTrue(transaction.getTimestamp().isAfter(before) || transaction.getTimestamp().isEqual(before));
        assertTrue(transaction.getTimestamp().isBefore(after) || transaction.getTimestamp().isEqual(after));
    }
    
    @Test
    void testTransactionIdUniqueness() {
        Transaction transaction1 = new Transaction(TEST_TYPE, TEST_AMOUNT, TEST_DESCRIPTION);
        Transaction transaction2 = new Transaction(TEST_TYPE, TEST_AMOUNT, TEST_DESCRIPTION);
        
        // Verify transaction IDs are unique
        assertNotEquals(transaction1.getTransactionId(), transaction2.getTransactionId());
    }
    
    @Test
    void testAllTransactionTypes() {
        // Test creating transactions with all available transaction types
        for (TransactionType type : TransactionType.values()) {
            Transaction transaction = new Transaction(type, TEST_AMOUNT, TEST_DESCRIPTION);
            assertEquals(type, transaction.getType());
        }
    }
    
    @Test
    void testToString() {
        Transaction transaction = new Transaction(TEST_TYPE, TEST_AMOUNT, TEST_DESCRIPTION);
        String toString = transaction.toString();
        
        // Verify toString contains important information
        assertTrue(toString.contains(TEST_DESCRIPTION));
        assertTrue(toString.contains(String.valueOf((int)TEST_AMOUNT)));
    }
} 