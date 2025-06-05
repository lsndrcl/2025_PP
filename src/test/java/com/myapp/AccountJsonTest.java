package com.myapp;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Unit tests for the JSON import/export functionality of the Account class.
 */
public class AccountJsonTest {
    
    private Account account;
    
    @TempDir
    Path tempDir;
    
    @BeforeEach
    void setUp() {
        // Create a new Account instance before each test
        account = new Account();
    }
    
    @Test
    void testImportTransactionsFromJson() throws IOException {
        // Create a test JSON file with transactions
        Path jsonFile = tempDir.resolve("test-transactions.json");
        JSONArray testData = new JSONArray();
        
        // Add a deposit transaction
        JSONObject deposit = new JSONObject();
        deposit.put("type", "DEPOSIT");
        deposit.put("amount", 200.0);
        deposit.put("description", "Test deposit");
        testData.put(deposit);
        
        // Add a withdrawal transaction
        JSONObject withdrawal = new JSONObject();
        withdrawal.put("type", "WITHDRAWAL");
        withdrawal.put("amount", 50.0);
        withdrawal.put("description", "Test withdrawal");
        testData.put(withdrawal);
        
        // Write the test data to the file
        Files.write(jsonFile, testData.toString().getBytes());
        
        // Import the transactions
        account.importTransactionsFromJson(jsonFile.toString());
        
        // Verify the account state
        assertEquals(150.0, account.getBalance(), 0.001);
        
        List<Transaction> transactions = account.getTransactions();
        assertEquals(2, transactions.size());
        
        // Verify the first transaction (deposit)
        Transaction tx1 = transactions.get(0);
        assertEquals(TransactionType.DEPOSIT, tx1.getType());
        assertEquals(200.0, tx1.getAmount(), 0.001);
        assertEquals("Test deposit", tx1.getDescription());
        
        // Verify the second transaction (withdrawal)
        Transaction tx2 = transactions.get(1);
        assertEquals(TransactionType.WITHDRAWAL, tx2.getType());
        assertEquals(50.0, tx2.getAmount(), 0.001);
        assertEquals("Test withdrawal", tx2.getDescription());
    }
    
    @Test
    void testExportTransactionsToJson() throws IOException {
        // Add some transactions
        account.deposit(300.0, "First deposit");
        account.withdraw(75.0, "First withdrawal");
        
        // Export the transactions
        Path exportFile = tempDir.resolve("export-transactions.json");
        account.exportTransactionsToJson(exportFile.toString());
        
        // Read the exported file
        String jsonContent = new String(Files.readAllBytes(exportFile));
        JSONArray exportedData = new JSONArray(jsonContent);
        
        // Verify the exported data
        assertEquals(2, exportedData.length());
        
        // Verify the first transaction (deposit)
        JSONObject tx1 = exportedData.getJSONObject(0);
        assertEquals("DEPOSIT", tx1.getString("type"));
        assertEquals(300.0, tx1.getDouble("amount"), 0.001);
        assertEquals("First deposit", tx1.getString("description"));
        
        // Verify the second transaction (withdrawal)
        JSONObject tx2 = exportedData.getJSONObject(1);
        assertEquals("WITHDRAWAL", tx2.getString("type"));
        assertEquals(75.0, tx2.getDouble("amount"), 0.001);
        assertEquals("First withdrawal", tx2.getString("description"));
    }
    
    @Test
    void testRoundTripImportExport() throws IOException {
        // Create test data
        Path importFile = tempDir.resolve("import.json");
        JSONArray testData = new JSONArray();
        
        JSONObject tx1 = new JSONObject();
        tx1.put("type", "DEPOSIT");
        tx1.put("amount", 500.0);
        tx1.put("description", "Initial deposit");
        testData.put(tx1);
        
        Files.write(importFile, testData.toString().getBytes());
        
        // Import the data
        account.importTransactionsFromJson(importFile.toString());
        
        // Export the data
        Path exportFile = tempDir.resolve("export.json");
        account.exportTransactionsToJson(exportFile.toString());
        
        // Read the exported file
        String jsonContent = new String(Files.readAllBytes(exportFile));
        JSONArray exportedData = new JSONArray(jsonContent);
        
        // Verify that the exported data contains the imported transaction
        assertEquals(1, exportedData.length());
        JSONObject exportedTx = exportedData.getJSONObject(0);
        assertEquals("DEPOSIT", exportedTx.getString("type"));
        assertEquals(500.0, exportedTx.getDouble("amount"), 0.001);
        assertEquals("Initial deposit", exportedTx.getString("description"));
    }
    
    @Test
    void testImportInvalidTransaction() throws IOException {
        // Create a test JSON file with an invalid transaction
        Path jsonFile = tempDir.resolve("invalid-transaction.json");
        JSONArray testData = new JSONArray();
        
        // Add a deposit
        JSONObject deposit = new JSONObject();
        deposit.put("type", "DEPOSIT");
        deposit.put("amount", 100.0);
        deposit.put("description", "Valid deposit");
        testData.put(deposit);
        
        // Add a withdrawal that exceeds the balance
        JSONObject invalidWithdrawal = new JSONObject();
        invalidWithdrawal.put("type", "WITHDRAWAL");
        invalidWithdrawal.put("amount", 200.0);
        invalidWithdrawal.put("description", "Invalid withdrawal");
        testData.put(invalidWithdrawal);
        
        // Write the test data to the file
        Files.write(jsonFile, testData.toString().getBytes());
        
        // Attempt to import the transactions
        assertThrows(IllegalStateException.class, () -> {
            account.importTransactionsFromJson(jsonFile.toString());
        });
        
        // Verify that the first transaction was processed but the account state was rolled back
        assertEquals(0.0, account.getBalance(), 0.001);
        assertEquals(0, account.getTransactions().size());
    }
} 