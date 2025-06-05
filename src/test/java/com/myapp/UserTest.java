package com.myapp;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the User class.
 */
public class UserTest {
    
    private User user;
    private static final String TEST_USERNAME = "testUser";
    private static final String TEST_PASSWORD_HASH = "hashedPassword123";
    
    @BeforeEach
    void setUp() {
        // Create a new User instance before each test
        user = new User(TEST_USERNAME, TEST_PASSWORD_HASH);
    }
    
    @Test
    void testUserCreation() {
        // Test that the user is created with the correct username and password hash
        assertEquals(TEST_USERNAME, user.getUsername());
        assertEquals(TEST_PASSWORD_HASH, user.getPasswordHash());
    }
    
    @Test
    void testAccountInitialization() {
        // Test that the user has an initialized Account
        assertNotNull(user.getAccount());
    }
    
    @Test
    void testPortfolioInitialization() {
        // Test that the user has an initialized Portfolio
        assertNotNull(user.getPortfolio());
        // Test that the portfolio is associated with the correct account
        assertSame(user.getAccount(), user.getPortfolio().getAccount());
    }
} 