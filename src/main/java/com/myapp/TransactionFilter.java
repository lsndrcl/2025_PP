package com.myapp;

/**
 * Interface for filtering transactions.
 */
public interface TransactionFilter {
    /**
     * Tests whether a transaction matches the filter criteria.
     * @param transaction The transaction to test
     * @return true if the transaction matches, false otherwise
     */
    boolean matches(Transaction transaction);
} 