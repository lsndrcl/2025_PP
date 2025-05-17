import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

//TODO implement a way to add transactions
public class Account {
    private double balance;
    private final List<Transaction> transactions;

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

    public void deposit(double amount, String description) {
        if (amount <= 0) throw new IllegalArgumentException("Deposit amount must be positive.");
        balance += amount;
        transactions.add(new Transaction(TransactionType.DEPOSIT, amount, description));
    }

    public void withdraw(double amount, String description) {
        if (amount <= 0) throw new IllegalArgumentException("Withdrawal amount must be positive.");
        if (amount > balance) throw new IllegalStateException("Insufficient balance.");
        balance -= amount;
        transactions.add(new Transaction(TransactionType.WITHDRAWAL, amount, description));
    }

    public List<Transaction> getRecentTransactions(int count) {
        return transactions.stream()
                .skip(Math.max(0, transactions.size() - count))
                .collect(Collectors.toList());
    }

    public List<Transaction> searchTransactions(TransactionFilter filter) {
        return transactions.stream()
                .filter(filter::matches)
                .collect(Collectors.toList());
    }
}
