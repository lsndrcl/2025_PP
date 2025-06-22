package com.myapp;

import java.util.*;

/**
 * Represents a user's crypto portfolio, tracking owned cryptocurrencies and transactions.
 */
public class Portfolio {
    private final Account account;
    private final Map<String, Double> holdings;  // Symbol -> Amount
    private final Map<String, Double> purchasePrices;  // Symbol -> Average Purchase Price
    private final Map<String, List<Position>> positions; // Symbol -> List of positions

    /**
     * Creates a new portfolio linked to a user account.
     * @param account The user's account for fiat transactions
     */
    public Portfolio(Account account) {
        this.account = account;
        this.holdings = new HashMap<>();
        this.purchasePrices = new HashMap<>();
        this.positions = new HashMap<>();
    }

    /**
     * Gets the associated account.
     * @return The account associated with this portfolio
     */
    public Account getAccount() {
        return account;
    }

    /**
     * Gets a copy of the current crypto holdings.
     * @return Map of crypto symbol to amount held
     */
    public Map<String, Double> getHoldings() {
        return new HashMap<>(holdings);
    }
    
    /**
     * Gets a copy of the average purchase prices for each holding.
     * @return Map of crypto symbol to average purchase price
     */
    public Map<String, Double> getPurchasePrices() {
        return new HashMap<>(purchasePrices);
    }
    
    /**
     * Gets the average purchase price for a specific coin.
     * @param symbol The crypto symbol
     * @return The average purchase price, or 0.0 if not found
     */
    public double getAveragePurchasePrice(String symbol) {
        return purchasePrices.getOrDefault(symbol, 0.0);
    }
    
    /**
     * Buys a cryptocurrency using fiat from the linked account.
     * Fetches the current price automatically.
     * 
     * @param symbol The crypto symbol (e.g., "BTC")
     * @param fiatAmount The fiat amount to spend
     * @return The amount of crypto purchased
     * @throws Exception if the price cannot be fetched or the transaction fails
     */
    public double buyCrypto(String symbol, double fiatAmount) throws Exception {
        if (fiatAmount <= 0) throw new IllegalArgumentException("Amount must be positive");
        
        // Get current price
        CryptoService service = new CryptoService();
        double price = service.getCurrentPrices().get(symbol);
        if (price <= 0) throw new IllegalStateException("Invalid price for " + symbol);
        
        // Calculate crypto amount
        double cryptoAmount = fiatAmount / price;
        
        // Execute the purchase
        buyCrypto(symbol, cryptoAmount, price);
        
        return cryptoAmount;
    }

    /**
     * Buys a cryptocurrency using fiat from the linked account.
     * @param symbol The crypto symbol (e.g., "BTC")
     * @param amount The amount to buy
     * @param price The price per unit in fiat
     */
    public void buyCrypto(String symbol, double amount, double price) {
        if (amount <= 0) throw new IllegalArgumentException("Amount must be positive");
        if (price <= 0) throw new IllegalArgumentException("Price must be positive");
        
        double totalCost = amount * price;
        account.withdraw(totalCost, "Purchase of " + amount + " " + symbol, TransactionType.CRYPTO_PURCHASE);
        
        // Update holdings
        double currentAmount = holdings.getOrDefault(symbol, 0.0);
        double currentAvgPrice = purchasePrices.getOrDefault(symbol, 0.0);
        
        // Calculate new average purchase price (weighted average)
        double newTotalAmount = currentAmount + amount;
        double newAvgPrice;
        
        if (currentAmount > 0) {
            // Calculate weighted average: (currentAmount * currentPrice + newAmount * newPrice) / totalAmount
            newAvgPrice = ((currentAmount * currentAvgPrice) + (amount * price)) / newTotalAmount;
        } else {
            newAvgPrice = price;
        }
        
        holdings.put(symbol, newTotalAmount);
        purchasePrices.put(symbol, newAvgPrice);
    }
    
    /**
     * Sells a cryptocurrency, converting to fiat in the linked account.
     * Fetches the current price automatically.
     * 
     * @param symbol The crypto symbol (e.g., "BTC")
     * @param cryptoAmount The amount of crypto to sell
     * @return The fiat amount received
     * @throws Exception if the price cannot be fetched or the transaction fails
     */
    public double sellCrypto(String symbol, double cryptoAmount) throws Exception {
        if (cryptoAmount <= 0) throw new IllegalArgumentException("Amount must be positive");
        
        // Get current price
        CryptoService service = new CryptoService();
        double price = service.getCurrentPrices().get(symbol);
        if (price <= 0) throw new IllegalStateException("Invalid price for " + symbol);
        
        // Calculate fiat value
        double fiatValue = cryptoAmount * price;
        
        // Execute the sale
        sellCrypto(symbol, cryptoAmount, price);
        
        return fiatValue;
    }

    /**
     * Sells a cryptocurrency, converting to fiat in the linked account.
     * @param symbol The crypto symbol (e.g., "BTC")
     * @param amount The amount to sell
     * @param price The price per unit in fiat
     */
    public void sellCrypto(String symbol, double amount, double price) {
        if (amount <= 0) throw new IllegalArgumentException("Amount must be positive");
        if (price <= 0) throw new IllegalArgumentException("Price must be positive");
        
        // Check if we have enough of the crypto
        double currentAmount = holdings.getOrDefault(symbol, 0.0);
        if (amount > currentAmount) {
            throw new IllegalStateException("Insufficient " + symbol + " balance");
        }
        
        double totalValue = amount * price;
        account.deposit(totalValue, "Sale of " + amount + " " + symbol, TransactionType.CRYPTO_SALE);
        
        // Update holdings
        double newAmount = currentAmount - amount;
        if (newAmount > 0) {
            holdings.put(symbol, newAmount);
            // Purchase price remains the same when selling
        } else {
            holdings.remove(symbol);
            purchasePrices.remove(symbol);
        }
    }
    
    /**
     * Calculates the total value of the portfolio using current market prices.
     * @return The total portfolio value in fiat
     * @throws Exception if prices cannot be fetched
     */
    public double calculateTotalValue() throws Exception {
        if (holdings.isEmpty()) {
            return 0.0;
        }
        
        CryptoService service = new CryptoService();
        Map<String, Double> prices = service.getCurrentPrices();
        
        double totalValue = 0.0;
        for (Map.Entry<String, Double> entry : holdings.entrySet()) {
            String symbol = entry.getKey();
            double amount = entry.getValue();
            double price = prices.getOrDefault(symbol, 0.0);
            totalValue += (amount * price);
        }
        
        return totalValue;
    }
    
    /**
     * Calculates the profit/loss for a specific coin.
     * @param symbol The crypto symbol
     * @return The profit/loss percentage (positive for profit, negative for loss)
     * @throws Exception if prices cannot be fetched
     */
    public double calculateProfitLossPercent(String symbol) throws Exception {
        if (!holdings.containsKey(symbol) || !purchasePrices.containsKey(symbol)) {
            return 0.0;
        }
        
        CryptoService service = new CryptoService();
        Map<String, Double> prices = service.getCurrentPrices();
        
        double currentPrice = prices.getOrDefault(symbol, 0.0);
        double purchasePrice = purchasePrices.get(symbol);
        
        if (purchasePrice <= 0 || currentPrice <= 0) {
            return 0.0;
        }
        
        return ((currentPrice - purchasePrice) / purchasePrice) * 100.0;
    }

    /**
     * Apre una posizione short su una criptovaluta specificata.
     *
     * Il metodo calcola il collaterale richiesto (importo * prezzo), lo preleva
     * dall'account e registra la posizione tra le posizioni aperte del portafoglio.
     *
     * @param symbol Il simbolo della criptovaluta (es. "BTC")
     * @param amount La quantità di criptovaluta da shortare
     * @param price Il prezzo corrente della criptovaluta al momento dell'apertura
     * @throws IllegalArgumentException Se amount o price sono <= 0
     */
    public void openShortPosition(String symbol, double amount, double price) {
        if (amount <= 0) throw new IllegalArgumentException("Amount must be positive");
        if (price <= 0) throw new IllegalArgumentException("Price must be positive");

        double collateral = amount * price; // Collaterale richiesto per la posizione short
        account.withdraw(collateral, "Short position collateral for " + amount + " " + symbol,
                TransactionType.CRYPTO_SHORT_OPEN);

        long timestamp = System.currentTimeMillis();
        Position shortPosition = new Position(symbol, amount, price, PositionType.SHORT, timestamp);

        positions.computeIfAbsent(symbol, k -> new ArrayList<>()).add(shortPosition);
    }


    /**
     * Chiude una posizione short per una determinata criptovaluta.
     *
     * Il metodo chiude una o più posizioni short per la quantità richiesta,
     * calcola il profitto o la perdita (PnL) per ciascuna e restituisce
     * all'account il collaterale originale più l'eventuale profitto.
     *
     * Se la quantità da chiudere è maggiore delle posizioni esistenti,
     * viene sollevata un'eccezione.
     *
     * @param symbol Il simbolo della criptovaluta (es. "BTC")
     * @param amount La quantità da chiudere
     * @param currentPrice Il prezzo di mercato attuale della criptovaluta
     * @return Il profitto o perdita totale (PnL) ottenuto dalla chiusura
     * @throws IllegalStateException Se non ci sono sufficienti posizioni short aperte
     */
    public double closeShortPosition(String symbol, double amount, double currentPrice) {
        List<Position> symbolPositions = positions.get(symbol);
        if (symbolPositions == null) throw new IllegalStateException("No short positions for " + symbol);

        double remainingToClose = amount;
        double totalPnL = 0.0;
        Iterator<Position> iterator = symbolPositions.iterator();

        while (iterator.hasNext() && remainingToClose > 0) {
            Position pos = iterator.next();
            if (pos.getType() != PositionType.SHORT) continue;

            double positionAmount = pos.getAmount();
            double amountToClose = Math.min(remainingToClose, positionAmount);

            // PnL per short: guadagno quando prezzo scende
            double returnAmount = amountToClose * (2 * pos.getEntryPrice() - currentPrice);
            totalPnL += amountToClose * (pos.getEntryPrice() - currentPrice);
            account.deposit(returnAmount, "Close short position " + amountToClose + " " + symbol,
                    TransactionType.CRYPTO_SHORT_CLOSE);

            if (amountToClose == positionAmount) {
                iterator.remove();
            } else {
                pos.setAmount(positionAmount - amountToClose);
            }

            remainingToClose -= amountToClose;

            if (remainingToClose > 0) {
                throw new IllegalStateException("Not enough short positions to close the requested amount.");
            }

        }

        return totalPnL;
    }


    /**
     * Restituisce una mappa delle posizioni short attualmente aperte.
     *
     * La mappa contiene per ogni criptovaluta la quantità totale shortata.
     *
     * @return Una mappa da simbolo di criptovaluta a quantità shortata
     */
    public Map<String, Double> getShortPositions() {
        Map<String, Double> shorts = new HashMap<>();
        for (Map.Entry<String, List<Position>> entry : positions.entrySet()) {
            double totalShort = entry.getValue().stream()
                    .filter(p -> p.getType() == PositionType.SHORT)
                    .mapToDouble(Position::getAmount)
                    .sum();
            if (totalShort > 0) {
                shorts.put(entry.getKey(), totalShort);
            }
        }
        return shorts;
    }


    /**
     * Restituisce tutte le posizioni aperte (long e short) nel portafoglio.
     *
     * La mappa risultante è immutabile e mostra ogni simbolo associato alla lista
     * delle relative posizioni (long o short).
     *
     * @return Una mappa non modificabile da simbolo a lista di posizioni
     */
    public Map<String, List<Position>> getAllPositions() {
        return Collections.unmodifiableMap(positions);
    }

}

