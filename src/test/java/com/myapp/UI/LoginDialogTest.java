package com.myapp.UI;

import com.myapp.User;
import com.myapp.auth.UserManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import javax.swing.*;
import java.awt.*;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Unit tests for the LoginDialog class.
 * Note: These tests use reflection to test internal methods without displaying UI.
 */
public class LoginDialogTest {
    
    private LoginDialog loginDialog;
    private TestUserManager userManager;
    
    @BeforeEach
    void setUp() {
        userManager = new TestUserManager();
        loginDialog = new LoginDialog(null, userManager);
        
        // Make the dialog not visible
        loginDialog.setVisible(false);
    }
    
    @Test
    void testInitialState() {
        // Verify initial state
        assertNull(loginDialog.getLoggedInUser());
    }
    
    @Test
    void testUsernameField() throws Exception {
        // Get the private field using reflection
        Field usernameField = LoginDialog.class.getDeclaredField("usernameField");
        usernameField.setAccessible(true);
        JTextField field = (JTextField) usernameField.get(loginDialog);
        
        // Test setting text
        field.setText("testUser");
        assertEquals("testUser", field.getText());
    }
    
    @Test
    void testPasswordField() throws Exception {
        // Get the private field using reflection
        Field passwordField = LoginDialog.class.getDeclaredField("passwordField");
        passwordField.setAccessible(true);
        JPasswordField field = (JPasswordField) passwordField.get(loginDialog);
        
        // Test setting text
        field.setText("password123");
        assertEquals("password123", new String(field.getPassword()));
    }
    
    @Test
    void testLoginButton() throws Exception {
        // Get the private fields using reflection
        Field usernameField = LoginDialog.class.getDeclaredField("usernameField");
        Field passwordField = LoginDialog.class.getDeclaredField("passwordField");
        Field loginButton = LoginDialog.class.getDeclaredField("loginButton");
        usernameField.setAccessible(true);
        passwordField.setAccessible(true);
        loginButton.setAccessible(true);
        
        JTextField username = (JTextField) usernameField.get(loginDialog);
        JPasswordField password = (JPasswordField) passwordField.get(loginDialog);
        JButton button = (JButton) loginButton.get(loginDialog);
        
        // Set valid credentials
        username.setText("validUser");
        password.setText("validPass");
        
        // Simulate button click
        button.doClick();
        
        // Verify user is logged in
        assertNotNull(loginDialog.getLoggedInUser());
        assertEquals("validUser", loginDialog.getLoggedInUser().getUsername());
    }
    
    @Test
    void testLoginFailure() throws Exception {
        // Get the private fields using reflection
        Field usernameField = LoginDialog.class.getDeclaredField("usernameField");
        Field passwordField = LoginDialog.class.getDeclaredField("passwordField");
        Field loginButton = LoginDialog.class.getDeclaredField("loginButton");
        usernameField.setAccessible(true);
        passwordField.setAccessible(true);
        loginButton.setAccessible(true);
        
        JTextField username = (JTextField) usernameField.get(loginDialog);
        JPasswordField password = (JPasswordField) passwordField.get(loginDialog);
        JButton button = (JButton) loginButton.get(loginDialog);
        
        // Set invalid credentials
        username.setText("invalidUser");
        password.setText("invalidPass");
        
        // Replace JOptionPane.showMessageDialog with no-op to avoid UI
        UIManager.put("OptionPane.buttonTypes", new Object[] {});
        
        // Simulate button click
        button.doClick();
        
        // Verify user is not logged in
        assertNull(loginDialog.getLoggedInUser());
    }
    
    @Test
    void testRegisterButton() throws Exception {
        // Get the private fields using reflection
        Field usernameField = LoginDialog.class.getDeclaredField("usernameField");
        Field passwordField = LoginDialog.class.getDeclaredField("passwordField");
        Field registerButton = LoginDialog.class.getDeclaredField("registerButton");
        usernameField.setAccessible(true);
        passwordField.setAccessible(true);
        registerButton.setAccessible(true);
        
        JTextField username = (JTextField) usernameField.get(loginDialog);
        JPasswordField password = (JPasswordField) passwordField.get(loginDialog);
        JButton button = (JButton) registerButton.get(loginDialog);
        
        // Set valid credentials
        username.setText("newUser");
        password.setText("password123");
        
        // Replace JOptionPane.showMessageDialog with no-op to avoid UI
        UIManager.put("OptionPane.buttonTypes", new Object[] {});
        
        // Simulate button click
        button.doClick();
        
        // Verify user is registered and logged in
        assertNotNull(loginDialog.getLoggedInUser());
        assertEquals("newUser", loginDialog.getLoggedInUser().getUsername());
    }
    
    @Test
    void testRegisterFailureShortUsername() throws Exception {
        // Get the private fields using reflection
        Field usernameField = LoginDialog.class.getDeclaredField("usernameField");
        Field passwordField = LoginDialog.class.getDeclaredField("passwordField");
        Field registerButton = LoginDialog.class.getDeclaredField("registerButton");
        usernameField.setAccessible(true);
        passwordField.setAccessible(true);
        registerButton.setAccessible(true);
        
        JTextField username = (JTextField) usernameField.get(loginDialog);
        JPasswordField password = (JPasswordField) passwordField.get(loginDialog);
        JButton button = (JButton) registerButton.get(loginDialog);
        
        // Set invalid credentials (short username)
        username.setText("ab");
        password.setText("password123");
        
        // Replace JOptionPane.showMessageDialog with no-op to avoid UI
        UIManager.put("OptionPane.buttonTypes", new Object[] {});
        
        // Simulate button click
        button.doClick();
        
        // Verify user is not registered
        assertNull(loginDialog.getLoggedInUser());
    }
    
    @Test
    void testRegisterFailureShortPassword() throws Exception {
        // Get the private fields using reflection
        Field usernameField = LoginDialog.class.getDeclaredField("usernameField");
        Field passwordField = LoginDialog.class.getDeclaredField("passwordField");
        Field registerButton = LoginDialog.class.getDeclaredField("registerButton");
        usernameField.setAccessible(true);
        passwordField.setAccessible(true);
        registerButton.setAccessible(true);
        
        JTextField username = (JTextField) usernameField.get(loginDialog);
        JPasswordField password = (JPasswordField) passwordField.get(loginDialog);
        JButton button = (JButton) registerButton.get(loginDialog);
        
        // Set invalid credentials (short password)
        username.setText("validUser");
        password.setText("12345");
        
        // Replace JOptionPane.showMessageDialog with no-op to avoid UI
        UIManager.put("OptionPane.buttonTypes", new Object[] {});
        
        // Simulate button click
        button.doClick();
        
        // Verify user is not registered
        assertNull(loginDialog.getLoggedInUser());
    }
    
    /**
     * Test implementation of UserManager for testing LoginDialog.
     */
    private static class TestUserManager extends UserManager {
        @Override
        public User loginUser(String username, String password) {
            // Only allow "validUser" with "validPass"
            if ("validUser".equals(username) && "validPass".equals(password)) {
                return new User(username, "hashedPassword");
            }
            return null;
        }
        
        @Override
        public User registerUser(String username, String password) throws Exception {
            // Only allow usernames that don't exist and meet minimum requirements
            if (username.length() >= 3 && password.length() >= 6 && !"validUser".equals(username)) {
                return new User(username, "hashedPassword");
            }
            return null;
        }
    }
} 