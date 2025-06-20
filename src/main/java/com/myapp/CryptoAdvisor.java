package com.myapp;

import weka.classifiers.meta.FilteredClassifier;
import weka.classifiers.trees.RandomForest;
import weka.core.Instance;
import weka.core.Instances;
import weka.filters.unsupervised.attribute.Standardize;

import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * CryptoAdvisor provides a recommendation for the most promising cryptocurrency to invest in,
 * based on a machine learning model trained on historical market data.
 *
 * It uses Weka's RandomForest algorithm to perform regression on recent price data
 * (e.g., previous price, moving averages, volatility) and predicts the next-day price.
 *
 * The coin with the highest predicted growth rate is returned as the recommendation.
 */
public class CryptoAdvisor {

    private final CryptoService cryptoService;
    private final int lookbackDays;
    private final int maxThreads;
    private final boolean useParallelProcessing;

    /**
     * Constructs a new CryptoAdvisor.
     *
     * @param cryptoService  A service providing access to coin IDs and live price data.
     * @param lookbackDays   Number of days of historical data to fetch for training.
     */
    public CryptoAdvisor(CryptoService cryptoService, int lookbackDays) {
        this(cryptoService, lookbackDays, true);
    }
    
    /**
     * Constructs a new CryptoAdvisor with option to use parallel processing.
     *
     * @param cryptoService  A service providing access to coin IDs and live price data.
     * @param lookbackDays   Number of days of historical data to fetch for training.
     * @param useParallelProcessing Whether to use parallel processing for API calls and model training.
     */
    public CryptoAdvisor(CryptoService cryptoService, int lookbackDays, boolean useParallelProcessing) {
        this.cryptoService = cryptoService;
        this.lookbackDays = lookbackDays;
        this.useParallelProcessing = useParallelProcessing;
        // Use available processors but cap at 4 to avoid overwhelming the API
        this.maxThreads = Math.min(Runtime.getRuntime().availableProcessors(), 4);
    }

    /**
     * Recommends the best coin to invest in based on predicted price growth.
     * The recommendation is made by training a RandomForest regressor on each coin's
     * historical price data and comparing predicted vs. current prices.
     *
     * @return The symbol of the recommended coin (e.g., "BTC", "ETH"), or null if no data is usable.
     * @throws Exception if model training or data retrieval fails.
     */
    public String recommendCoin() throws Exception {
        LiveDataLoader loader = new LiveDataLoader();
        Map<String, String> coinIdToSymbol = cryptoService.getCoinIdToSymbolMap();
        
        if (useParallelProcessing) {
            return recommendCoinParallel(loader, coinIdToSymbol);
        } else {
            return recommendCoinSequential(loader, coinIdToSymbol);
        }
    }
    
    /**
     * Sequential implementation of coin recommendation.
     */
    private String recommendCoinSequential(LiveDataLoader loader, Map<String, String> coinIdToSymbol) throws Exception {
        String bestCoin = null;
        double bestGrowth = Double.NEGATIVE_INFINITY;

        for (Map.Entry<String, String> entry : coinIdToSymbol.entrySet()) {
            String coinId = entry.getKey();
            String symbol = entry.getValue();

            try {
                // Reduced pause time since we're using caching now
                Thread.sleep(1000);
                Instances data = loader.getHistoricalData(coinId, lookbackDays);

                if (data.numInstances() < 5) {
                    System.err.println("Not enough data for " + coinId);
                    continue;
                }

                // Train model and get growth prediction
                double growth = trainModelAndPredictGrowth(data, symbol);

                if (growth > bestGrowth) {
                    bestGrowth = growth;
                    bestCoin = symbol;
                }

            } catch (Exception e) {
                System.err.println("Failed to process data for " + coinId + ": " + e.getMessage());
            }
        }

        return bestCoin;
    }
    
    /**
     * Parallel implementation of coin recommendation.
     */
    private String recommendCoinParallel(LiveDataLoader loader, Map<String, String> coinIdToSymbol) throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(maxThreads);
        Map<String, Double> growthResults = new ConcurrentHashMap<>();
        
        // Create futures for each coin
        CompletableFuture<?>[] futures = coinIdToSymbol.entrySet().stream()
            .map(entry -> CompletableFuture.runAsync(() -> {
                String coinId = entry.getKey();
                String symbol = entry.getValue();
                
                try {
                    Instances data = loader.getHistoricalData(coinId, lookbackDays);
                    
                    if (data.numInstances() < 5) {
                        System.err.println("Not enough data for " + coinId);
                        return;
                    }
                    
                    // Train model and get growth prediction
                    double growth = trainModelAndPredictGrowth(data, symbol);
                    growthResults.put(symbol, growth);
                    
                } catch (Exception e) {
                    System.err.println("Failed to process data for " + coinId + ": " + e.getMessage());
                }
            }, executor))
            .toArray(CompletableFuture[]::new);
        
        // Wait for all futures to complete
        CompletableFuture.allOf(futures).join();
        
        // Shutdown the executor
        executor.shutdown();
        try {
            if (!executor.awaitTermination(30, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
        }
        
        // Find the best coin
        String bestCoin = null;
        double bestGrowth = Double.NEGATIVE_INFINITY;
        
        for (Map.Entry<String, Double> entry : growthResults.entrySet()) {
            if (entry.getValue() > bestGrowth) {
                bestGrowth = entry.getValue();
                bestCoin = entry.getKey();
            }
        }
        
        return bestCoin;
    }
    
    /**
     * Train a model on the given data and predict growth rate.
     */
    private double trainModelAndPredictGrowth(Instances data, String symbol) throws Exception {
        // Train RandomForest model with standardized input features
        FilteredClassifier model = new FilteredClassifier();
        model.setClassifier(new RandomForest());
        model.setFilter(new Standardize());
        model.buildClassifier(data);

        Instance latest = data.lastInstance();
        double predictedPrice = model.classifyInstance(latest);
        double currentPrice = latest.value(data.classIndex());

        double growth = (predictedPrice - currentPrice) / currentPrice;

        System.out.printf("Coin %s: Current=%.2f Predicted=%.2f Growth=%.4f%n", symbol, currentPrice, predictedPrice, growth);
        
        return growth;
    }
}
