package com.myapp;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Provides cryptocurrency-related services such as price data retrieval and analysis.
 */
public class CryptoService {

    // CoinGecko ID to ticker symbol mapping
    private static final Map<String, String> coinIdToSymbol = Map.of(
            "bitcoin", "BTC",
            "ethereum", "ETH",
            "tether", "USDT",
            "binancecoin", "BNB",
            "solana", "SOL",
            "usd-coin", "USDC",
            "ripple", "XRP",
            "cardano", "ADA",
            "dogecoin", "DOGE",
            "avalanche-2", "AVAX"
    );
    
    // API rate limiting - CoinGecko free tier allows ~50 calls per minute
    private static final long API_CALL_INTERVAL_MS = 1200; // ~50 calls per minute
    private static final AtomicLong lastApiCallTime = new AtomicLong(0);
    
    // Cache directory and expiration
    private static final String CACHE_DIR = "data/cache";
    private static final int CACHE_EXPIRATION_MINUTES = 15; // Prices cache expires faster than historical data

    /**
     * Constructor ensures cache directory exists
     */
    public CryptoService() {
        File cacheDir = new File(CACHE_DIR);
        if (!cacheDir.exists()) {
            cacheDir.mkdirs();
        }
    }

    /**
     * Returns the map of CoinGecko IDs to their corresponding symbols.
     * @return Map of coin IDs to symbols
     */
    public Map<String, String> getCoinIdToSymbolMap() {
        return coinIdToSymbol;
    }

    /**
     * Fetches the current USD prices for supported cryptocurrencies from CoinGecko.
     * @return Map of coin symbols to their current USD prices
     * @throws Exception if the API request fails
     */
    public Map<String, Double> getCurrentPrices() throws Exception {
        // Check cache first
        File cacheFile = new File(CACHE_DIR + "/current_prices.json");
        if (cacheFile.exists() && !isCacheExpired(cacheFile, CACHE_EXPIRATION_MINUTES)) {
            System.out.println("Using cached current prices");
            String jsonData = readFromCache(cacheFile);
            return parsePricesFromJson(new JSONObject(jsonData));
        }
        
        Map<String, Double> prices = new HashMap<>();
        String ids = String.join(",", coinIdToSymbol.keySet());
        String urlStr = "https://api.coingecko.com/api/v3/simple/price?ids=" + ids + "&vs_currencies=usd";
        
        JSONObject json = fetchJsonFromUrl(urlStr);
        
        // Save to cache
        writeToCache(cacheFile, json.toString());
        
        return parsePricesFromJson(json);
    }
    
    /**
     * Parse prices from JSON response
     */
    private Map<String, Double> parsePricesFromJson(JSONObject json) {
        Map<String, Double> prices = new HashMap<>();
        
        for (Map.Entry<String, String> entry : coinIdToSymbol.entrySet()) {
            String coinId = entry.getKey();
            String symbol = entry.getValue();
            if (json.has(coinId)) {
                double price = json.getJSONObject(coinId).getDouble("usd");
                prices.put(symbol, price);
            }
        }
        return prices;
    }

    /**
     * Fetches historical prices over a number of days for all supported cryptocurrencies.
     * Uses batch requests where possible and caches results.
     * 
     * @param lookbackDays Number of days of historical data
     * @return Map of coin symbols to a list of their daily prices
     */
    public Map<String, List<Double>> getHistoricalPrices(int lookbackDays) {
        Map<String, List<Double>> history = new HashMap<>();
        
        // Check if we have a cached batch result
        File batchCacheFile = new File(CACHE_DIR + "/historical_prices_" + lookbackDays + ".json");
        if (batchCacheFile.exists() && !isCacheExpired(batchCacheFile, CACHE_EXPIRATION_MINUTES * 4)) {
            try {
                System.out.println("Using cached historical prices");
                String jsonData = readFromCache(batchCacheFile);
                JSONObject batchJson = new JSONObject(jsonData);
                
                for (Map.Entry<String, String> entry : coinIdToSymbol.entrySet()) {
                    String coinId = entry.getKey();
                    String symbol = entry.getValue();
                    
                    if (batchJson.has(coinId)) {
                        JSONArray priceArray = batchJson.getJSONArray(coinId);
                        List<Double> prices = new ArrayList<>();
                        
                        for (int i = 0; i < priceArray.length(); i++) {
                            prices.add(priceArray.getDouble(i));
                        }
                        
                        history.put(symbol, prices);
                    }
                }
                
                return history;
            } catch (Exception e) {
                System.err.println("Error reading cache: " + e.getMessage());
                // Continue to fetch data if cache read fails
            }
        }
        
        // Fetch individual coin data and build batch result
        JSONObject batchResult = new JSONObject();
        
        for (Map.Entry<String, String> entry : coinIdToSymbol.entrySet()) {
            String coinId = entry.getKey();
            String symbol = entry.getValue();
            try {
                List<Double> prices = fetchHistoricalPrices(coinId, lookbackDays);
                if (!prices.isEmpty()) {
                    history.put(symbol, prices);
                    
                    // Add to batch result for caching
                    JSONArray priceArray = new JSONArray();
                    for (Double price : prices) {
                        priceArray.put(price);
                    }
                    batchResult.put(coinId, priceArray);
                }
            } catch (IOException e) {
                System.err.println("Failed to fetch data for: " + coinId + " - " + e.getMessage());
            }
        }
        
        // Save batch result to cache
        try {
            writeToCache(batchCacheFile, batchResult.toString());
        } catch (Exception e) {
            System.err.println("Failed to write cache: " + e.getMessage());
        }
        
        return history;
    }

    // Internal helper to fetch historical prices
    private List<Double> fetchHistoricalPrices(String coinId, int days) throws IOException {
        List<Double> prices = new ArrayList<>();
        
        // Check individual coin cache
        File cacheFile = new File(CACHE_DIR + "/" + coinId + "_hist_" + days + ".json");
        if (cacheFile.exists() && !isCacheExpired(cacheFile, CACHE_EXPIRATION_MINUTES * 4)) {
            try {
                String jsonData = readFromCache(cacheFile);
                JSONObject json = new JSONObject(jsonData);
                JSONArray priceArray = json.getJSONArray("prices");
                
                for (int i = 0; i < priceArray.length(); i++) {
                    prices.add(priceArray.getJSONArray(i).getDouble(1));
                }
                
                return prices;
            } catch (Exception e) {
                System.err.println("Error reading cache for " + coinId + ": " + e.getMessage());
                // Continue to fetch data if cache read fails
            }
        }
        
        String urlStr = String.format(
                "https://api.coingecko.com/api/v3/coins/%s/market_chart?vs_currency=usd&days=%d&interval=daily",
                coinId, days
        );

        JSONObject json = fetchJsonFromUrl(urlStr);
        
        // Save to cache
        try {
            writeToCache(cacheFile, json.toString());
        } catch (Exception e) {
            System.err.println("Failed to write cache for " + coinId + ": " + e.getMessage());
        }
        
        JSONArray priceArray = json.getJSONArray("prices");

        for (int i = 0; i < priceArray.length(); i++) {
            prices.add(priceArray.getJSONArray(i).getDouble(1));
        }

        return prices;
    }

    // Helper to fetch and parse JSON from a URL
    private JSONObject fetchJsonFromUrl(String urlString) throws IOException {
        // Implement rate limiting
        applyRateLimit();
        
        URL url = URI.create(urlString).toURL();
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("GET");
        con.setRequestProperty("User-Agent", "Mozilla/5.0");

        int status = con.getResponseCode();
        
        // Handle rate limiting
        if (status == 429) {
            System.out.println("Rate limit hit. Waiting 5 seconds...");
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            return fetchJsonFromUrl(urlString); // Retry
        }
        
        if (status != 200) {
            throw new IOException("Failed to fetch data: HTTP " + status);
        }

        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
        StringBuilder response = new StringBuilder();
        String line;

        while ((line = in.readLine()) != null) {
            response.append(line);
        }

        in.close();
        con.disconnect();

        return new JSONObject(response.toString());
    }
    
    /**
     * Apply rate limiting to API calls
     */
    private void applyRateLimit() {
        long currentTime = System.currentTimeMillis();
        long lastCall = lastApiCallTime.get();
        long timeSinceLastCall = currentTime - lastCall;
        
        if (timeSinceLastCall < API_CALL_INTERVAL_MS) {
            try {
                Thread.sleep(API_CALL_INTERVAL_MS - timeSinceLastCall);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        
        lastApiCallTime.set(System.currentTimeMillis());
    }
    
    /**
     * Check if cache file is expired
     */
    private boolean isCacheExpired(File cacheFile, int expirationMinutes) {
        long lastModified = cacheFile.lastModified();
        long currentTime = System.currentTimeMillis();
        long expirationTime = TimeUnit.MINUTES.toMillis(expirationMinutes);
        
        return (currentTime - lastModified) > expirationTime;
    }
    
    /**
     * Read data from cache file
     */
    private String readFromCache(File cacheFile) throws Exception {
        StringBuilder content = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(cacheFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line);
            }
        }
        return content.toString();
    }
    
    /**
     * Write data to cache file
     */
    private void writeToCache(File cacheFile, String data) throws Exception {
        try (FileWriter writer = new FileWriter(cacheFile)) {
            writer.write(data);
        }
    }
}

