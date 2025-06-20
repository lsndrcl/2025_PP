package com.myapp.auth;

import com.myapp.User;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * Handles user authentication, registration, and persistence.
 * Uses local JSON files to store user data.
 */
public class UserManager {
    private static final String USERS_FILE = "users.json";
    private static final String DATA_DIR = "data";
    
    private final Map<String, User> users;
    private final Path usersFilePath;
    
    /**
     * Creates a new UserManager and loads existing users if available.
     */
    public UserManager() {
        this.users = new HashMap<>();
        
        // Ensure data directory exists
        File dataDir = new File(DATA_DIR);
        if (!dataDir.exists()) {
            dataDir.mkdir();
        }
        
        this.usersFilePath = Paths.get(DATA_DIR, USERS_FILE);
        
        // Load existing users
        loadUsers();
    }
    
    /**
     * Registers a new user with the specified username and password.
     * 
     * @param username Username for the new account
     * @param password Plain text password
     * @return The newly created User object if successful, null if username already exists
     * @throws Exception If registration fails
     */
    public User registerUser(String username, String password) throws Exception {
        // Check if username already exists
        if (users.containsKey(username)) {
            return null; // Username already taken
        }
        
        // Generate salt and hash password
        String salt = generateSalt();
        String passwordHash = hashPassword(password, salt);
        
        // Create new user with salted hash
        User newUser = new User(username, passwordHash + ":" + salt);
        
        // Add to users map
        users.put(username, newUser);
        
        // Save to file
        saveUsers();
        
        return newUser;
    }
    
    /**
     * Authenticates a user with the given credentials.
     * 
     * @param username Username to authenticate
     * @param password Plain text password to check
     * @return The User object if authentication succeeds, null otherwise
     */
    public User loginUser(String username, String password) {
        // Check if user exists
        if (!users.containsKey(username)) {
            return null; // User not found
        }
        
        User user = users.get(username);
        String storedHash = user.getPasswordHash();
        
        // Split hash and salt
        String[] parts = storedHash.split(":");
        if (parts.length != 2) {
            return null; // Invalid hash format
        }
        
        String storedHashValue = parts[0];
        String salt = parts[1];
        
        // Hash the provided password with the stored salt
        try {
            String computedHash = hashPassword(password, salt);
            
            // Check if hashes match
            if (computedHash.equals(storedHashValue)) {
                return user; // Authentication successful
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return null; // Authentication failed
    }
    
    /**
     * Gets a user by username.
     * 
     * @param username Username to look up
     * @return The User object if found, null otherwise
     */
    public User getUser(String username) {
        return users.get(username);
    }
    
    /**
     * Saves all users to the JSON file.
     * 
     * @throws IOException If saving fails
     */
    public void saveUsers() throws IOException {
        JSONArray usersArray = new JSONArray();
        
        for (User user : users.values()) {
            JSONObject userJson = new JSONObject();
            userJson.put("username", user.getUsername());
            userJson.put("passwordHash", user.getPasswordHash());
            
            // Create account JSON if it exists
            if (user.getAccount() != null) {
                JSONObject accountJson = new JSONObject();
                accountJson.put("balance", user.getAccount().getBalance());
                
                // Add transactions if they exist
                if (!user.getAccount().getTransactions().isEmpty()) {
                    user.getAccount().exportTransactionsToJson(DATA_DIR + "/" + user.getUsername() + "_transactions.json");
                    accountJson.put("transactionsFile", user.getUsername() + "_transactions.json");
                }
                
                userJson.put("account", accountJson);
            }
            
            // Add portfolio information if it exists
            if (user.getPortfolio() != null && !user.getPortfolio().getHoldings().isEmpty()) {
                JSONObject portfolioJson = new JSONObject();
                
                // Convert holdings to JSON
                JSONObject holdingsJson = new JSONObject();
                for (Map.Entry<String, Double> entry : user.getPortfolio().getHoldings().entrySet()) {
                    holdingsJson.put(entry.getKey(), entry.getValue());
                }
                
                portfolioJson.put("holdings", holdingsJson);
                userJson.put("portfolio", portfolioJson);
            }
            
            usersArray.put(userJson);
        }
        
        // Write to file
        Files.write(usersFilePath, usersArray.toString().getBytes());
    }
    
    /**
     * Loads users from the JSON file.
     */
    private void loadUsers() {
        // Check if users file exists
        if (!Files.exists(usersFilePath)) {
            return; // No users to load
        }
        
        try {
            // Read and parse the file
            String content = new String(Files.readAllBytes(usersFilePath));
            JSONArray usersArray = new JSONArray(content);
            
            for (int i = 0; i < usersArray.length(); i++) {
                JSONObject userJson = usersArray.getJSONObject(i);
                
                String username = userJson.getString("username");
                String passwordHash = userJson.getString("passwordHash");
                
                // Create the user
                User user = new User(username, passwordHash);
                
                // Load account data if available
                if (userJson.has("account")) {
                    JSONObject accountJson = userJson.getJSONObject("account");
                    double balance = accountJson.getDouble("balance");
                    
                    // Set the balance - only deposit if positive amount
                    if (balance > 0) {
                        user.getAccount().deposit(balance, "Initial balance");
                    }
                    
                    // Load transactions if available
                    if (accountJson.has("transactionsFile")) {
                        String transactionsFile = accountJson.getString("transactionsFile");
                        Path transactionsPath = Paths.get(DATA_DIR, transactionsFile);
                        
                        if (Files.exists(transactionsPath)) {
                            // Clear the initial deposit transaction
                            user = new User(username, passwordHash);
                            
                            // Import transactions
                            user.getAccount().importTransactionsFromJson(transactionsPath.toString());
                        }
                    }
                }
                
                // Load portfolio data if available
                if (userJson.has("portfolio")) {
                    JSONObject portfolioJson = userJson.getJSONObject("portfolio");
                    
                    if (portfolioJson.has("holdings")) {
                        JSONObject holdingsJson = portfolioJson.getJSONObject("holdings");
                        
                        // Add each holding
                        for (String coinSymbol : holdingsJson.keySet()) {
                            double amount = holdingsJson.getDouble(coinSymbol);
                            // We don't know the price at which it was bought, so use 1.0 as default
                            user.getPortfolio().buyCrypto(coinSymbol, amount, 1.0);
                        }
                    }
                }
                
                // Add to users map
                users.put(username, user);
            }
            
        } catch (Exception e) {
            System.err.println("Error loading users: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Generates a random salt for password hashing.
     * 
     * @return Base64 encoded salt string
     */
    private String generateSalt() {
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[16];
        random.nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }
    
    /**
     * Hashes a password with the given salt using SHA-256.
     * 
     * @param password Plain text password
     * @param salt Salt string (Base64 encoded)
     * @return Hashed password
     * @throws NoSuchAlgorithmException If SHA-256 is not available
     */
    private String hashPassword(String password, String salt) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        md.update(Base64.getDecoder().decode(salt));
        byte[] hashedPassword = md.digest(password.getBytes());
        return Base64.getEncoder().encodeToString(hashedPassword);
    }
} 