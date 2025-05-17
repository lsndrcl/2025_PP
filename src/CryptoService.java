import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import org.json.JSONObject;

public class CryptoService {

    //just using ten standard coin for the purpose of the project
    // Mapping CoinGecko IDs to their common symbols
    private static final Map<String, String> coinIdToSymbol = Map.of(
            "bitcoin", "BTC",
            "ethereum", "ETH",
            "tether", "USDT",
            "binancecoin", "BNB",
            "solana", "SOL",
            "usd-coin", "USDC",
            "xrp", "XRP",
            "cardano", "ADA",
            "dogecoin", "DOGE",
            "avalanche", "AVAX"
    );

    public Map<String, Double> getCurrentPrices() throws Exception {
        Map<String, Double> prices = new HashMap<>();

        //should not be a problem to push on github since it's not an API key
        //can only get probably one coin price at a time
        //TODO in the ui the must be a way to choose which coin the user wants to know the price of
        String coinIds = String.join(",", coinIdToSymbol.keySet());
        JSONObject json = getJsonObject();

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

    private static JSONObject getJsonObject() throws IOException {
        String apiUrl = "https://api.coingecko.com/api/v3/simple/price?ids=bitcoin&vs_currencies=usd";

        URL url = new URL(apiUrl);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("GET");

        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
        StringBuilder response = new StringBuilder();
        String line;

        while ((line = in.readLine()) != null) {
            response.append(line);
        }
        in.close();

        JSONObject json = new JSONObject(response.toString());
        return json;
    }
}
