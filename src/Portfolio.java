import java.util.HashMap;
import java.util.Map;

public class Portfolio {
    private final Map<String, Double> holdings;
    private double cashBalance;
    private final Account account;
    private final CryptoService cryptoService;

    public Portfolio(Account account) {
        this.account = account;
        this.cryptoService = new CryptoService();
        this.holdings = new HashMap<>();
        this.cashBalance = 0.0;
    }

    public void depositCash(double amount) {
        if (amount <= 0) throw new IllegalArgumentException("Deposit must be positive.");
        cashBalance += amount;
    }

    public void buy(String coin, double fiatAmount) throws Exception {
        Map<String, Double> prices = cryptoService.getCurrentPrices();
        if (!prices.containsKey(coin)) throw new IllegalArgumentException("Unsupported coin.");

        double price = prices.get(coin);
        double quantity = fiatAmount / price;

        if (fiatAmount > cashBalance) throw new IllegalStateException("Not enough fiat balance.");

        cashBalance -= fiatAmount;
        holdings.put(coin, holdings.getOrDefault(coin, 0.0) + quantity);
        account.getTransactions().add(new Transaction(TransactionType.CRYPTO_PURCHASE, fiatAmount, "Bought " + quantity + " " + coin));
    }

    public void sell(String coin, double quantity) throws Exception {
        if (!holdings.containsKey(coin) || holdings.get(coin) < quantity)
            throw new IllegalStateException("Not enough " + coin + " to sell.");

        Map<String, Double> prices = cryptoService.getCurrentPrices();
        double price = prices.get(coin);
        double fiatGained = price * quantity;

        holdings.put(coin, holdings.get(coin) - quantity);
        cashBalance += fiatGained;
        account.getTransactions().add(new Transaction(TransactionType.CRYPTO_SALE, fiatGained, "Sold " + quantity + " " + coin));
    }

    public double getTotalValue() throws Exception {
        Map<String, Double> prices = cryptoService.getCurrentPrices();
        double total = cashBalance;
        for (Map.Entry<String, Double> entry : holdings.entrySet()) {
            String coin = entry.getKey();
            double qty = entry.getValue();
            total += prices.getOrDefault(coin, 0.0) * qty;
        }
        return total;
    }

    public Map<String, Double> getHoldings() {
        return new HashMap<>(holdings);
    }

    public double getCashBalance() {
        return cashBalance;
    }
}
