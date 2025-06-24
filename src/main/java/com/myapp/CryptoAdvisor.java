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
import java.util.concurrent.atomic.AtomicBoolean;

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

    /*
    public String recommendCoin() throws Exception {
        return recommendCoin(new AtomicBoolean(false));
    }
     */
    
    /**
     * Recommends the best coin to invest in based on predicted price growth,
     * with support for cancellation.
     *
     * @param cancelRequested AtomicBoolean flag that can be set to true to cancel the process
     * @return The symbol of the recommended coin (e.g., "BTC", "ETH"), or null if cancelled or no data is usable.
     * @throws Exception if model training or data retrieval fails.
     */
    public String recommendCoin(AtomicBoolean cancelRequested) throws Exception {
        LiveDataLoader loader = new LiveDataLoader();
        Map<String, String> coinIdToSymbol = cryptoService.getCoinIdToSymbolMap();
        
        if (useParallelProcessing) {
            return recommendCoinParallel(loader, coinIdToSymbol, cancelRequested);
        } else {
            return recommendCoinSequential(loader, coinIdToSymbol, cancelRequested);
        }
    }

    /**
     * Performs a sequential scan of a given set of cryptocurrencies and recommends the one
     * with the highest predicted growth using a machine learning model.
     *
     * <p>For each coin, this method:</p>
     * <ol>
     *   <li>Loads historical data (with caching)</li>
     *   <li>Trains a model on the data</li>
     *   <li>Predicts future growth</li>
     * </ol>
     *
     * <p>If the operation is cancelled via the {@code cancelRequested} flag, the method
     * terminates early and returns {@code null}.</p>
     *
     * @param loader            The {@link LiveDataLoader} instance to fetch coin data.
     * @param coinIdToSymbol    A map of CoinGecko coin IDs to their corresponding symbols.
     * @param cancelRequested   Atomic flag to support external cancellation of the task.
     * @return The symbol of the coin with the highest predicted growth, or {@code null} if cancelled.
     * @throws Exception If an error occurs during data fetching or model training.
     */
    private String recommendCoinSequential(LiveDataLoader loader, Map<String, String> coinIdToSymbol, AtomicBoolean cancelRequested) throws Exception {
        String bestCoin = null;
        double bestGrowth = Double.NEGATIVE_INFINITY;

        for (Map.Entry<String, String> entry : coinIdToSymbol.entrySet()) {
            // Check if cancellation was requested
            if (cancelRequested.get()) {
                return null;
            }
            
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
     * Performs a parallel scan of a set of cryptocurrencies and recommends the one
     * with the highest predicted growth using a trained machine learning model.
     *
     * <p>This implementation leverages multithreading to improve performance when
     * analyzing many coins. Each coin is processed in a separate thread using a
     * {@link CompletableFuture}.</p>
     *
     * <p>The method supports external cancellation via the {@code cancelRequested} flag and
     * shuts down all threads gracefully if cancellation is detected.</p>
     *
     * <p>Steps for each coin:</p>
     * <ol>
     *   <li>Loads historical data (with caching)</li>
     *   <li>Trains a model and predicts growth</li>
     *   <li>Stores growth in a concurrent map</li>
     * </ol>
     *
     * @param loader            The {@link LiveDataLoader} instance for fetching coin data.
     * @param coinIdToSymbol    A map of CoinGecko coin IDs to their corresponding symbols.
     * @param cancelRequested   Atomic flag to support external cancellation.
     * @return The symbol of the coin with the highest predicted growth, or {@code null} if cancelled.
     * @throws Exception If data processing or threading encounters an unexpected error.
     */
    private String recommendCoinParallel(LiveDataLoader loader, Map<String, String> coinIdToSymbol, AtomicBoolean cancelRequested) throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(maxThreads);
        Map<String, Double> growthResults = new ConcurrentHashMap<>();
        
        // Create futures for each coin
        CompletableFuture<?>[] futures = coinIdToSymbol.entrySet().stream()
            .map(entry -> CompletableFuture.runAsync(() -> {
                // Check if cancellation was requested
                if (cancelRequested.get()) {
                    return;
                }
                
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
        
        // Wait for all futures to complete or until cancellation
        try {
            for (CompletableFuture<?> future : futures) {
                if (cancelRequested.get()) {
                    break;
                }
                // Add a small timeout to check cancellation periodically
                future.get(500, TimeUnit.MILLISECONDS);
            }
        } catch (Exception e) {
            // Timeout is expected for incomplete futures
            System.err.println("Some futures didn't complete in time: " + e.getMessage());
        }
        
        // Shutdown the executor
        executor.shutdown();
        try {
            if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
        }
        
        // If cancelled, return null
        if (cancelRequested.get()) {
            return null;
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
     * Trains a RandomForest model using the given dataset and predicts the growth rate
     * for the latest data point (e.g., a cryptocurrency or stock price).
     *
     * <p>This method performs the following steps:
     * <ul>
     *   <li>Applies standardization to the input features.</li>
     *   <li>Trains a RandomForest regression model using the Weka library.</li>
     *   <li>Predicts the target value (e.g., future price) for the most recent instance.</li>
     *   <li>Calculates and returns the relative growth between the predicted and actual price.</li>
     *   <li>Logs the symbol, current price, predicted price, and growth percentage to console.</li>
     * </ul>
     *
     * @param data   A Weka {@link weka.core.Instances} dataset, where the class attribute
     *               (target variable) is set (typically the price to predict).
     * @param symbol A string representing the asset's symbol (e.g., "BTC", "ETH").
     * @return       The predicted growth ratio, calculated as
     *               (predictedPrice - currentPrice) / currentPrice.
     * @throws Exception If an error occurs during model training or prediction.
     */
    private double trainModelAndPredictGrowth(Instances data, String symbol) throws Exception {
        // Train RandomForest model with standardized input features
        FilteredClassifier model = new FilteredClassifier();
        model.setClassifier(new RandomForest());
        model.setFilter(new Standardize());
        model.buildClassifier(data);

        // Use the most recent instance for prediction
        Instance latest = data.lastInstance();
        double predictedPrice = model.classifyInstance(latest);
        double currentPrice = latest.value(data.classIndex());

        // Calculate relative growth
        double growth = (predictedPrice - currentPrice) / currentPrice;

        // Output results
        System.out.printf("Coin %s: Current=%.2f Predicted=%.2f Growth=%.4f%n", symbol, currentPrice, predictedPrice, growth);

        return growth;
    }

}
