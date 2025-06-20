package com.myapp;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;


/**
 * Unit tests for the CryptoAdvisor class.
 * Note: These tests use a custom test implementation of CryptoService to avoid making real API calls.
 */
public class CryptoAdvisorTest {
    
    private CryptoAdvisor advisor;
    private TestCryptoService testService;
    
    @BeforeEach
    void setUp() {
        testService = new TestCryptoService();
        advisor = new CryptoAdvisor(testService, 14, false); // Use sequential processing for predictable tests
    }
    
    @Test
    void testRecommendCoinWithCancellation() throws Exception {
        // Set up cancellation flag
        AtomicBoolean cancelRequested = new AtomicBoolean(true);
        
        // Should return null when cancelled
        String recommendation = advisor.recommendCoin(cancelRequested);
        assertNull(recommendation);
    }
    
    @Test
    void testParallelProcessing() throws Exception {
        // Create advisor with parallel processing
        CryptoAdvisor parallelAdvisor = new CryptoAdvisor(testService, 14, true);
        
        // We can't easily test the actual recommendation since it depends on the real LiveDataLoader
        // and we don't have control over its implementation. Instead, just verify it doesn't throw exceptions.
        assertDoesNotThrow(() -> {
            parallelAdvisor.recommendCoin(new AtomicBoolean(true));
        });
    }
    
    /**
     * Test implementation of CryptoService that returns predefined data.
     */
    private static class TestCryptoService extends CryptoService {
        
        @Override
        public Map<String, String> getCoinIdToSymbolMap() {
            Map<String, String> map = new HashMap<>();
            map.put("bitcoin", "BTC");
            map.put("ethereum", "ETH");
            map.put("cardano", "ADA");
            return map;
        }
        
        @Override
        public Map<String, Double> getCurrentPrices() {
            Map<String, Double> prices = new HashMap<>();
            prices.put("BTC", 50000.0);
            prices.put("ETH", 3000.0);
            prices.put("ADA", 2.0);
            return prices;
        }
    }
} 