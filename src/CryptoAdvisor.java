import weka.classifiers.meta.FilteredClassifier;
import weka.classifiers.trees.RandomForest;
import weka.core.Instance;
import weka.core.Instances;
import weka.filters.unsupervised.attribute.Standardize;

import java.util.Map;

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

    /**
     * Constructs a new CryptoAdvisor.
     *
     * @param cryptoService  A service providing access to coin IDs and live price data.
     * @param lookbackDays   Number of days of historical data to fetch for training.
     */
    public CryptoAdvisor(CryptoService cryptoService, int lookbackDays) {
        this.cryptoService = cryptoService;
        this.lookbackDays = lookbackDays;
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

        String bestCoin = null;
        double bestGrowth = Double.NEGATIVE_INFINITY;

        for (Map.Entry<String, String> entry : coinIdToSymbol.entrySet()) {
            String coinId = entry.getKey();
            String symbol = entry.getValue();

            try {
                // Pause to avoid hitting rate limits (HTTP 429)
                Thread.sleep(1500);
                Instances data = loader.getHistoricalData(coinId, lookbackDays);

                if (data.numInstances() < 5) {
                    System.err.println("Not enough data for " + coinId);
                    continue;
                }

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

                if (growth > bestGrowth) {
                    bestGrowth = growth;
                    bestCoin = symbol;
                }

            } catch (Exception e) {
                System.err.println("Failed to fetch data for " + coinId + ": " + e.getMessage());
            }
        }

        return bestCoin;
    }
}
