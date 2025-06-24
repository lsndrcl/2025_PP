package com.myapp.auth;

import com.myapp.DataManager;
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
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

/**
 * Handles user authentication, registration, and persistence.
 * Uses local JSON files to store user data.
 */
public class UserManager {
    private static final String USERS_FILE = "users.json";
    
    private final Map<String, User> users;
    private final Path usersFilePath;
    
    /**
     * Creates a new UserManager and loads existing users if available.
     */
    public UserManager() {
        this.users = new HashMap<>();
        
        // Initialize data directories
        DataManager.initializeDataDirectories();
        
        this.usersFilePath = Paths.get(DataManager.DATA_DIR, USERS_FILE);
        
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
                    String transactionFile = user.getUsername() + "_transactions.json";
                    user.getAccount().exportTransactionsToJson(DataManager.DATA_DIR + "/" + transactionFile);
                    accountJson.put("transactionsFile", transactionFile);
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
                
                // Save purchase prices
                JSONObject purchasePricesJson = new JSONObject();
                for (Map.Entry<String, Double> entry : user.getPortfolio().getPurchasePrices().entrySet()) {
                    purchasePricesJson.put(entry.getKey(), entry.getValue());
                }
                portfolioJson.put("purchasePrices", purchasePricesJson);
                
                userJson.put("portfolio", portfolioJson);
            }
            
            usersArray.put(userJson);
        }
        
        // Save data using DataManager
        DataManager.saveJsonData(usersArray, usersFilePath);
    }

    /**
     * Loads user data from a JSON file into the application's user map.
     * <p>
     * This method performs the following tasks:
     * <ul>
     *   <li>Checks if the users file exists; if not, it returns immediately.</li>
     *   <li>Reads and parses the users JSON file using {@link DataManager#loadJsonData(Path)}.</li>
     *   <li>For each user JSON object, extracts username and password hash and creates a {@link User} instance.</li>
     *   <li>Loads account data if available:
     *     <ul>
     *       <li>Deposits initial balance if positive.</li>
     *       <li>Imports transactions from the referenced JSON file, if it exists, replacing the initial deposit.</li>
     *     </ul>
     *   </li>
     *   <li>Loads portfolio data if available:
     *     <ul>
     *       <li>Reads holdings and purchase prices from JSON objects.</li>
     *       <li>Uses reflection to directly set the private holdings and purchasePrices maps in the user's portfolio.</li>
     *     </ul>
     *   </li>
     *   <li>Adds each constructed user to the in-memory users map keyed by username.</li>
     *   <li>If any exceptions occur during loading, logs the error and attempts to restore user data from a backup file, retrying the load once restored.</li>
     * </ul>
     * <p>
     * This method assumes:
     * <ul>
     *   <li>The users file path is stored in {@code usersFilePath}.</li>
     *   <li>JSON structure conforms to the expected schema with fields for user, account, and portfolio data.</li>
     *   <li>Transaction importing and portfolio manipulation are handled by respective class methods and reflection.</li>
     * </ul>
     *
     * @throws RuntimeException if the user data file is malformed or cannot be read, unless restored from backup successfully
     */
    private void loadUsers() {
        // Check if users file exists
        if (!Files.exists(usersFilePath)) {
            return; // No users to load
        }
        
        try {
            // Read and parse the file using DataManager
            String content = DataManager.loadJsonData(usersFilePath);
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
                        Path transactionsPath = Paths.get(DataManager.DATA_DIR, transactionsFile);
                        
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
                    
                    // Create maps for holdings and purchase prices
                    Map<String, Double> holdings = new HashMap<>();
                    Map<String, Double> purchasePrices = new HashMap<>();
                    
                    // Load purchase prices if available
                    if (portfolioJson.has("purchasePrices")) {
                        JSONObject pricesJson = portfolioJson.getJSONObject("purchasePrices");
                        for (String coinSymbol : pricesJson.keySet()) {
                            purchasePrices.put(coinSymbol, pricesJson.getDouble(coinSymbol));
                        }
                    }
                    
                    // Load holdings
                    if (portfolioJson.has("holdings")) {
                        JSONObject holdingsJson = portfolioJson.getJSONObject("holdings");
                        
                        // Add each holding
                        for (String coinSymbol : holdingsJson.keySet()) {
                            double amount = holdingsJson.getDouble(coinSymbol);
                            holdings.put(coinSymbol, amount);
                        }
                    }
                    
                    // Directly set the holdings and purchase prices using reflection
                    try {
                        java.lang.reflect.Field holdingsField = user.getPortfolio().getClass().getDeclaredField("holdings");
                        holdingsField.setAccessible(true);
                        Map<String, Double> portfolioHoldings = (Map<String, Double>) holdingsField.get(user.getPortfolio());
                        portfolioHoldings.clear();
                        portfolioHoldings.putAll(holdings);
                        
                        java.lang.reflect.Field pricesField = user.getPortfolio().getClass().getDeclaredField("purchasePrices");
                        pricesField.setAccessible(true);
                        Map<String, Double> portfolioPrices = (Map<String, Double>) pricesField.get(user.getPortfolio());
                        portfolioPrices.clear();
                        portfolioPrices.putAll(purchasePrices);
                    } catch (Exception e) {
                        System.err.println("Error setting portfolio data: " + e.getMessage());
                    }
                }
                
                // Add to users map
                users.put(username, user);
            }
            
        } catch (Exception e) {
            System.err.println("Error loading users: " + e.getMessage());
            e.printStackTrace();
            
            // Try to restore from backup if loading fails
            if (DataManager.restoreFromBackup(USERS_FILE)) {
                System.out.println("Restored users data from backup. Attempting to load again...");
                loadUsers(); // Try loading again
            }
        }
    }
    
    /**
     * Creates a backup of the current users file if it exists
     */
    private void createBackup() {
        try {
            if (Files.exists(usersFilePath)) {
                DataManager.createBackup(usersFilePath);
            }
        } catch (IOException e) {
            System.err.println("Failed to create backup: " + e.getMessage());
        }
    }
    
    /**
     * Saves all user data on application exit
     */
    public void saveAllUserData() {
        try {
            // Save users file
            saveUsers();
            
            // Save each user's transactions
            for (User user : users.values()) {
                if (user.getAccount() != null && !user.getAccount().getTransactions().isEmpty()) {
                    String transactionFile = user.getUsername() + "_transactions.json";
                    Path transactionPath = Paths.get(DataManager.DATA_DIR, transactionFile);
                    
                    // Convert transactions to JSON array
                    JSONArray transactionsArray = new JSONArray();
                    for (com.myapp.Transaction tx : user.getAccount().getTransactions()) {
                        JSONObject txJson = new JSONObject();
                        txJson.put("transactionId", tx.getTransactionId());
                        txJson.put("timestamp", tx.getTimestamp().toString());
                        txJson.put("type", tx.getType().toString());
                        txJson.put("amount", tx.getAmount());
                        txJson.put("description", tx.getDescription());
                        
                        transactionsArray.put(txJson);
                    }
                    
                    // Save using DataManager
                    DataManager.saveJsonData(transactionsArray, transactionPath);
                }
            }
            
            System.out.println("All user data saved successfully");
        } catch (IOException e) {
            System.err.println("Error saving user data: " + e.getMessage());
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