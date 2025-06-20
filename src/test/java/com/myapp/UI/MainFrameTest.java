package com.myapp.UI;

import com.myapp.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import javax.swing.*;
import java.awt.*;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Unit tests for the MainFrame class.
 * Note: These tests use reflection to test internal methods without displaying UI.
 */
public class MainFrameTest {
    
    private MainFrame mainFrame;
    private User testUser;
    
    @BeforeEach
    void setUp() {
        testUser = new User("testUser", "hashedPassword");
        mainFrame = new MainFrame(testUser);
        
        // Make the frame not visible
        mainFrame.setVisible(false);
    }
    
    @Test
    void testFrameInitialization() {
        // Verify frame is initialized
        assertNotNull(mainFrame);
        assertEquals("JavaBankCrypto", mainFrame.getTitle());
    }
    
    @Test
    void testUserReference() throws Exception {
        // Get the private field using reflection
        Field currentUserField = MainFrame.class.getDeclaredField("currentUser");
        currentUserField.setAccessible(true);
        User frameUser = (User) currentUserField.get(mainFrame);
        
        // Verify user reference is correct
        assertSame(testUser, frameUser);
    }
    
    @Test
    void testHeaderPanel() throws Exception {
        // Get the createHeaderPanel method using reflection
        Method createHeaderPanel = MainFrame.class.getDeclaredMethod("createHeaderPanel");
        createHeaderPanel.setAccessible(true);
        
        // Invoke the method
        JPanel headerPanel = (JPanel) createHeaderPanel.invoke(mainFrame);
        
        // Verify header panel is created
        assertNotNull(headerPanel);
    }
    
    @Test
    void testAccountPanelInitialization() throws Exception {
        // Get the private field using reflection
        Field accountPanel = MainFrame.class.getDeclaredField("accountPanel");
        accountPanel.setAccessible(true);
        AccountPanel panel = (AccountPanel) accountPanel.get(mainFrame);
        
        // Verify account panel is initialized
        assertNotNull(panel);
    }
    
    @Test
    void testTradingPanelInitialization() throws Exception {
        // Get the private field using reflection
        Field tradingPanel = MainFrame.class.getDeclaredField("tradingPanel");
        tradingPanel.setAccessible(true);
        TradingPanel panel = (TradingPanel) tradingPanel.get(mainFrame);
        
        // Verify trading panel is initialized
        assertNotNull(panel);
    }
} 