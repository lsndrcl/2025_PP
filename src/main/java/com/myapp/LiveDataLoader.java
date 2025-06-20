package com.myapp;

import org.json.JSONArray;
import org.json.JSONObject;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instances;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * LiveDataLoader is responsible for fetching and converting historical cryptocurrency
 * price data into Weka Instances that can be used for training ML models.
 *
 * It retrieves price data from the CoinGecko API, computes features such as:
 * - Previous day's price
 * - Moving averages (3-day, 7-day)
 * - Volatility (3-day standard deviation)
 *
 * and constructs labeled instances for supervised learning.
 */
public class LiveDataLoader {
    // Cache directory
    private static final String CACHE_DIR = "data/cache";
    // Cache expiration time in minutes
    private static final int CACHE_EXPIRATION_MINUTES = 60;
    // In-memory cache for current session
    private final Map<String, CacheEntry> memoryCache = new HashMap<>();
    
    /**
     * Constructor that ensures cache directory exists
     */
    public LiveDataLoader() {
        File cacheDir = new File(CACHE_DIR);
        if (!cacheDir.exists()) {
            cacheDir.mkdirs();
        }
    }
    
    /**
     * Downloads historical daily price data for a specific coin and converts it into
     * a Weka dataset with engineered features.
     *
     * @param coinId The CoinGecko ID of the coin (e.g., "bitcoin", "ethereum").
     * @param days   Number of days of historical data to retrieve.
     * @return An Instances object ready for training, with features and target price.
     * @throws Exception if API fails or data is malformed.
     */
    public Instances getHistoricalData(String coinId, int days) throws Exception {
        // Check memory cache first
        String cacheKey = coinId + "_" + days;
        if (memoryCache.containsKey(cacheKey)) {
            CacheEntry entry = memoryCache.get(cacheKey);
            if (!entry.isExpired()) {
                System.out.println("Using memory cache for " + coinId);
                return entry.getData();
            }
        }
        
        // Check file cache
        File cacheFile = new File(CACHE_DIR + "/" + cacheKey + ".json");
        if (cacheFile.exists() && !isCacheExpired(cacheFile)) {
            System.out.println("Using file cache for " + coinId);
            String jsonData = readFromCache(cacheFile);
            Instances data = processJsonData(jsonData);
            
            // Update memory cache
            memoryCache.put(cacheKey, new CacheEntry(data));
            
            return data;
        }
        
        // If not in cache, fetch from API
        String urlStr = String.format(
                "https://api.coingecko.com/api/v3/coins/%s/market_chart?vs_currency=usd&days=%d&interval=daily",
                coinId, days);

        HttpURLConnection con = (HttpURLConnection) URI.create(urlStr).toURL().openConnection();
        con.setRequestMethod("GET");
        con.setRequestProperty("User-Agent", "Mozilla/5.0");

        int status = con.getResponseCode();

        if (status == 429) {
            System.err.println("Rate limit hit for " + coinId + ". Retrying...");
            Thread.sleep(3000);
            return getHistoricalData(coinId, days);  // Retry once
        }

        if (status != 200) {
            throw new RuntimeException("Failed to get data for " + coinId + " HTTP code: " + status);
        }

        BufferedReader reader = new BufferedReader(new InputStreamReader(con.getInputStream()));
        StringBuilder jsonText = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) jsonText.append(line);
        reader.close();
        
        // Save to cache
        writeToCache(cacheFile, jsonText.toString());
        
        // Process the data
        Instances data = processJsonData(jsonText.toString());
        
        // Update memory cache
        memoryCache.put(cacheKey, new CacheEntry(data));
        
        return data;
    }
    
    /**
     * Process JSON data into Weka Instances
     */
    private Instances processJsonData(String jsonData) {
        JSONObject json = new JSONObject(jsonData);
        JSONArray prices = json.getJSONArray("prices");

        // Define attributes (features + target)
        ArrayList<Attribute> attributes = new ArrayList<>();
        attributes.add(new Attribute("PrevPrice"));
        attributes.add(new Attribute("MA3"));
        attributes.add(new Attribute("MA7"));
        attributes.add(new Attribute("Volatility3"));
        attributes.add(new Attribute("Price"));  // Target variable

        Instances data = new Instances("CryptoPrices", attributes, prices.length() - 7);
        data.setClassIndex(4); // target: Price

        // Construct instances
        for (int i = 7; i < prices.length(); i++) {
            double prevPrice = prices.getJSONArray(i - 1).getDouble(1);
            double price = prices.getJSONArray(i).getDouble(1);

            // MA3
            double sum3 = 0;
            for (int j = i - 3; j < i; j++) {
                sum3 += prices.getJSONArray(j).getDouble(1);
            }
            double ma3 = sum3 / 3.0;

            // MA7
            double sum7 = 0;
            for (int j = i - 7; j < i; j++) {
                sum7 += prices.getJSONArray(j).getDouble(1);
            }
            double ma7 = sum7 / 7.0;

            // Volatility3
            double mean3 = ma3;
            double sumSq = 0;
            for (int j = i - 3; j < i; j++) {
                double val = prices.getJSONArray(j).getDouble(1);
                sumSq += Math.pow(val - mean3, 2);
            }
            double volatility3 = Math.sqrt(sumSq / 3);

            DenseInstance instance = new DenseInstance(5);
            instance.setValue(attributes.get(0), prevPrice);
            instance.setValue(attributes.get(1), ma3);
            instance.setValue(attributes.get(2), ma7);
            instance.setValue(attributes.get(3), volatility3);
            instance.setValue(attributes.get(4), price);

            data.add(instance);
        }

        return data;
    }
    
    /**
     * Check if cache file is expired
     */
    private boolean isCacheExpired(File cacheFile) {
        long lastModified = cacheFile.lastModified();
        long currentTime = System.currentTimeMillis();
        long expirationTime = TimeUnit.MINUTES.toMillis(CACHE_EXPIRATION_MINUTES);
        
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
    
    /**
     * Cache entry with timestamp for in-memory cache
     */
    private static class CacheEntry {
        private final Instances data;
        private final LocalDateTime timestamp;
        
        public CacheEntry(Instances data) {
            this.data = data;
            this.timestamp = LocalDateTime.now();
        }
        
        public Instances getData() {
            return data;
        }
        
        public boolean isExpired() {
            return LocalDateTime.now().minusMinutes(CACHE_EXPIRATION_MINUTES).isAfter(timestamp);
        }
    }
}
