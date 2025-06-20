package com.myapp;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the TransactionType enum.
 */
public class TransactionTypeTest {
    
    @Test
    void testEnumValues() {
        // Verify all expected enum values exist
        TransactionType[] types = TransactionType.values();
        assertEquals(5, types.length);
        
        // Verify specific enum values
        assertEquals(TransactionType.DEPOSIT, TransactionType.valueOf("DEPOSIT"));
        assertEquals(TransactionType.WITHDRAWAL, TransactionType.valueOf("WITHDRAWAL"));
        assertEquals(TransactionType.TRANSFER, TransactionType.valueOf("TRANSFER"));
        assertEquals(TransactionType.CRYPTO_PURCHASE, TransactionType.valueOf("CRYPTO_PURCHASE"));
        assertEquals(TransactionType.CRYPTO_SALE, TransactionType.valueOf("CRYPTO_SALE"));
    }
    
    @Test
    void testEnumOrdinals() {
        // Verify enum ordinals are as expected
        assertEquals(0, TransactionType.DEPOSIT.ordinal());
        assertEquals(1, TransactionType.WITHDRAWAL.ordinal());
        assertEquals(2, TransactionType.TRANSFER.ordinal());
        assertEquals(3, TransactionType.CRYPTO_PURCHASE.ordinal());
        assertEquals(4, TransactionType.CRYPTO_SALE.ordinal());
    }
    
    @Test
    void testToString() {
        // Verify toString method returns the expected string representation
        assertEquals("DEPOSIT", TransactionType.DEPOSIT.toString());
        assertEquals("WITHDRAWAL", TransactionType.WITHDRAWAL.toString());
        assertEquals("TRANSFER", TransactionType.TRANSFER.toString());
        assertEquals("CRYPTO_PURCHASE", TransactionType.CRYPTO_PURCHASE.toString());
        assertEquals("CRYPTO_SALE", TransactionType.CRYPTO_SALE.toString());
    }
    
    @Test
    void testValueOf() {
        // Test valueOf method with valid values
        assertEquals(TransactionType.DEPOSIT, TransactionType.valueOf("DEPOSIT"));
        assertEquals(TransactionType.WITHDRAWAL, TransactionType.valueOf("WITHDRAWAL"));
        
        // Test valueOf method with invalid value
        assertThrows(IllegalArgumentException.class, () -> {
            TransactionType.valueOf("INVALID_TYPE");
        });
    }
} 