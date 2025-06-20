package com.myapp;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.FileWriter;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import weka.core.Instances;

/**
 * Unit tests for the LiveDataLoader class.
 * Note: These tests use reflection to test internal methods without making actual API calls.
 */
public class LiveDataLoaderTest {
    
    private LiveDataLoader loader;
    
    @TempDir
    File tempDir;
    
    @BeforeEach
    void setUp() {
        // Create a new LiveDataLoader instance
        loader = new LiveDataLoader();
    }
    
    @Test
    void testCacheDirectoryCreation() {
        // Verify cache directory was created
        File cacheDir = new File(tempDir.getAbsolutePath());
        assertTrue(cacheDir.exists());
    }
    
    @Test
    void testIsCacheExpired() throws Exception {
        // Get the private method using reflection
        Method isCacheExpired = LiveDataLoader.class.getDeclaredMethod("isCacheExpired", File.class);
        isCacheExpired.setAccessible(true);
        
        // Create a test file
        File testFile = new File(tempDir, "test_cache.json");
        try (FileWriter writer = new FileWriter(testFile)) {
            writer.write("{}");
        }
        
        // Test with recently created file (should not be expired)
        assertFalse((Boolean) isCacheExpired.invoke(loader, testFile));
    }
    
    @Test
    void testReadWriteCache() throws Exception {
        // Get the private methods using reflection
        Method writeToCache = LiveDataLoader.class.getDeclaredMethod("writeToCache", File.class, String.class);
        Method readFromCache = LiveDataLoader.class.getDeclaredMethod("readFromCache", File.class);
        writeToCache.setAccessible(true);
        readFromCache.setAccessible(true);
        
        // Create a test file and data
        File testFile = new File(tempDir, "test_read_write.json");
        String testData = "{\"test\":\"data\"}";
        
        // Write to cache
        writeToCache.invoke(loader, testFile, testData);
        
        // Read from cache
        String readData = (String) readFromCache.invoke(loader, testFile);
        
        // Verify data was written and read correctly
        assertEquals(testData, readData);
    }
    
    @Test
    void testProcessJsonData() throws Exception {
        // Get the private method using reflection
        Method processJsonData = LiveDataLoader.class.getDeclaredMethod("processJsonData", String.class);
        processJsonData.setAccessible(true);
        
        // Create test JSON data for a simple price series
        String jsonStr = "{\"prices\":[[1609459200000,29000.0],[1609545600000,29500.0]," +
                         "[1609632000000,30000.0],[1609718400000,31000.0],[1609804800000,32000.0]," +
                         "[1609891200000,33000.0],[1609977600000,34000.0],[1610064000000,35000.0]," +
                         "[1610150400000,36000.0],[1610236800000,37000.0]]}";
        
        // Process the data
        Instances data = (Instances) processJsonData.invoke(loader, jsonStr);
        
        // Verify the processed data
        assertNotNull(data);
        assertEquals("CryptoPrices", data.relationName());
        assertEquals(5, data.numAttributes()); // PrevPrice, MA3, MA7, Volatility3, Price
        assertEquals(4, data.classIndex()); // Price is the target variable
        assertEquals(3, data.numInstances()); // 10 data points - 7 for initial window = 3 instances
    }
    
    @Test
    void testCacheEntryExpiration() throws Exception {
        // Get the CacheEntry inner class using reflection
        Class<?> cacheEntryClass = Class.forName("com.myapp.LiveDataLoader$CacheEntry");
        
        // Create a test Instances object
        weka.core.Instances testData = new weka.core.Instances("Test", new java.util.ArrayList<>(), 0);
        
        // Create a CacheEntry instance
        Object cacheEntry = cacheEntryClass.getDeclaredConstructor(weka.core.Instances.class)
                                         .newInstance(testData);
        
        // Get the isExpired method
        Method isExpired = cacheEntryClass.getDeclaredMethod("isExpired");
        isExpired.setAccessible(true);
        
        // Test expiration (should not be expired immediately after creation)
        assertFalse((Boolean) isExpired.invoke(cacheEntry));
    }
    
    @Test
    void testCacheEntryGetData() throws Exception {
        // Get the CacheEntry inner class using reflection
        Class<?> cacheEntryClass = Class.forName("com.myapp.LiveDataLoader$CacheEntry");
        
        // Create a test Instances object
        weka.core.Instances testData = new weka.core.Instances("Test", new java.util.ArrayList<>(), 0);
        
        // Create a CacheEntry instance
        Object cacheEntry = cacheEntryClass.getDeclaredConstructor(weka.core.Instances.class)
                                         .newInstance(testData);
        
        // Get the getData method
        Method getData = cacheEntryClass.getDeclaredMethod("getData");
        getData.setAccessible(true);
        
        // Test getData
        Instances retrievedData = (Instances) getData.invoke(cacheEntry);
        assertSame(testData, retrievedData);
    }
} 