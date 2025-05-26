import java.util.HashMap;
import java.util.Map;

/**
 * Represents a user's portfolio that tracks crypto holdings and cash balance.
 */
public class Portfolio {
    private final Map<String, Double> holdings;
    private double cashBalance;
    private final Account account;
    private final CryptoService cryptoService;

    /**
     * Constructs a new portfolio associated with an account.
     * @param account The user's account
     */
    public Portfolio(Account account) {
        this.account = account;
        this.cryptoService = new CryptoService();
        this.holdings = new HashMap<>();
        this.cashBalance = 0.0;
    }

    /**
     * Adds fiat currency to the cash balance.
     * @param amount Amount to deposit
     */
    public void depositCash(double amount) {
        if (amount <= 0) throw new IllegalArgumentException("Deposit must be positive.");
        cashBalance += amount;
    }

    /**
     * Buys cryptocurrency using fiat balance.
     * @param coin The coin symbol (e.g., "BTC")
     * @param fiatAmount Amount of fiat currency to use
     * @throws Exception If coin is unsupported or balance is insufficient
     */
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

    /**
     * Sells cryptocurrency and converts it to fiat.
     * @param coin The coin symbol
     * @param quantity Quantity of the coin to sell
     * @throws Exception If insufficient quantity
     */
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

    /**
     * Returns the total fiat value of the portfolio (crypto + cash).
     * @return Total value in USD
     * @throws Exception if price lookup fails
     */
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

