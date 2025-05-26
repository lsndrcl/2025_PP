import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * A service class that interacts with the CoinGecko API to fetch current and historical cryptocurrency prices.
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
        Map<String, Double> prices = new HashMap<>();
        String ids = String.join(",", coinIdToSymbol.keySet());
        String urlStr = "https://api.coingecko.com/api/v3/simple/price?ids=" + ids + "&vs_currencies=usd";
        JSONObject json = fetchJsonFromUrl(urlStr);

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
     * @param lookbackDays Number of days of historical data
     * @return Map of coin symbols to a list of their daily prices
     */
    public Map<String, List<Double>> getHistoricalPrices(int lookbackDays) {
        Map<String, List<Double>> history = new HashMap<>();
        for (Map.Entry<String, String> entry : coinIdToSymbol.entrySet()) {
            String coinId = entry.getKey();
            String symbol = entry.getValue();
            try {
                List<Double> prices = fetchHistoricalPrices(coinId, lookbackDays);
                if (!prices.isEmpty()) {
                    history.put(symbol, prices);
                }
            } catch (IOException e) {
                System.err.println("Failed to fetch data for: " + coinId);
            }
        }
        return history;
    }

    // Internal helper to fetch historical prices
    private List<Double> fetchHistoricalPrices(String coinId, int days) throws IOException {
        List<Double> prices = new ArrayList<>();
        String urlStr = String.format(
                "https://api.coingecko.com/api/v3/coins/%s/market_chart?vs_currency=usd&days=%d&interval=daily",
                coinId, days
        );

        JSONObject json = fetchJsonFromUrl(urlStr);
        JSONArray priceArray = json.getJSONArray("prices");

        for (int i = 0; i < priceArray.length(); i++) {
            prices.add(priceArray.getJSONArray(i).getDouble(1));
        }

        return prices;
    }

    // Helper to fetch and parse JSON from a URL
    private JSONObject fetchJsonFromUrl(String urlString) throws IOException {
        URL url = new URL(urlString);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("GET");

        int status = con.getResponseCode();
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
}

