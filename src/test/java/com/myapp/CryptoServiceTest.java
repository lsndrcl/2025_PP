package com.myapp;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Map;

/**
 * Unit tests for the CryptoService class.
 * Note: These tests use reflection to test internal methods without making actual API calls.
 */
public class CryptoServiceTest {
    
    private CryptoService cryptoService;
    
    @TempDir
    File tempDir;
    
    @BeforeEach
    void setUp() throws Exception {
        // Create a new CryptoService instance
        cryptoService = new CryptoService();
    }
    
    @Test
    void testGetCoinIdToSymbolMap() {
        Map<String, String> map = cryptoService.getCoinIdToSymbolMap();
        
        // Verify map contains expected entries
        assertFalse(map.isEmpty());
        assertEquals("BTC", map.get("bitcoin"));
        assertEquals("ETH", map.get("ethereum"));
        assertEquals("ADA", map.get("cardano"));
    }
    
    @Test
    void testReadWriteCache() throws Exception {
        // Create a test file and data
        File testFile = new File(tempDir, "test_read_write.json");
        String testData = "{\"test\":\"data\"}";
        
        // Write data to file
        try (FileWriter writer = new FileWriter(testFile)) {
            writer.write(testData);
        }
        
        // Read data back using reflection
        Method readFromCache = CryptoService.class.getDeclaredMethod("readFromCache", File.class);
        readFromCache.setAccessible(true);
        
        String readData = (String) readFromCache.invoke(cryptoService, testFile);
        
        // Verify data was read correctly
        assertEquals(testData, readData);
    }
    
    @Test
    void testParsePricesFromJson() throws Exception {
        // Get the private method using reflection
        Method parsePricesFromJson = CryptoService.class.getDeclaredMethod("parsePricesFromJson", org.json.JSONObject.class);
        parsePricesFromJson.setAccessible(true);
        
        // Create test JSON data
        String jsonStr = "{\"bitcoin\":{\"usd\":50000.0},\"ethereum\":{\"usd\":3000.0}}";
        org.json.JSONObject json = new org.json.JSONObject(jsonStr);
        
        // Parse prices
        @SuppressWarnings("unchecked")
        Map<String, Double> prices = (Map<String, Double>) parsePricesFromJson.invoke(cryptoService, json);
        
        // Verify parsed prices
        assertEquals(2, prices.size());
        assertEquals(50000.0, prices.get("BTC"), 0.001);
        assertEquals(3000.0, prices.get("ETH"), 0.001);
    }
} 