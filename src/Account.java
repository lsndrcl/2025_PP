import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

//TODO implement a way to add transactions, should be imported with json file or by direct function
/**
 * Represents a user’s fiat account, tracking balance and transaction history.
 */
public class Account {
    private double balance;
    private final List<Transaction> transactions;

    /**
     * Creates a new empty account.
     */
    public Account() {
        this.balance = 0.0;
        this.transactions = new ArrayList<>();
    }

    public double getBalance() {
        return balance;
    }

    public List<Transaction> getTransactions() {
        return new ArrayList<>(transactions);
    }

    /**
     * Deposits an amount into the account.
     * @param amount Amount to deposit
     * @param description Description of the transaction
     */
    public void deposit(double amount, String description) {
        if (amount <= 0) throw new IllegalArgumentException("Deposit amount must be positive.");
        balance += amount;
        transactions.add(new Transaction(TransactionType.DEPOSIT, amount, description));
    }

    /**
     * Withdraws an amount from the account.
     * @param amount Amount to withdraw
     * @param description Description of the transaction
     */
    public void withdraw(double amount, String description) {
        if (amount <= 0) throw new IllegalArgumentException("Withdrawal amount must be positive.");
        if (amount > balance) throw new IllegalStateException("Insufficient balance.");
        balance -= amount;
        transactions.add(new Transaction(TransactionType.WITHDRAWAL, amount, description));
    }

    /**
     * Returns the N most recent transactions.
     * @param count Number of transactions to return
     * @return List of recent transactions
     */
    public List<Transaction> getRecentTransactions(int count) {
        return transactions.stream()
                .skip(Math.max(0, transactions.size() - count))
                .collect(Collectors.toList());
    }

    /**
     * Searches transactions using a custom filter.
     * @param filter A TransactionFilter predicate
     * @return List of matching transactions
     */
    public List<Transaction> searchTransactions(TransactionFilter filter) {
        return transactions.stream()
                .filter(filter::matches)
                .collect(Collectors.toList());
    }
}

