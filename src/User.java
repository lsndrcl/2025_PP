/**
 * Represents a user in the crypto trading system with associated account and portfolio.
 */
public class User {
    private String username;
    private String passwordHash;
    private Account account;
    private Portfolio portfolio;

    /**
     * Default constructor needed for JSON deserialization.
     */
    public User() {}

    /**
     * Creates a user with the specified credentials and initializes their account and portfolio.
     * @param username The user's username
     * @param passwordHash The hashed password
     */
    public User(String username, String passwordHash) {
        this.username = username;
        this.passwordHash = passwordHash;
        this.account = new Account();
        this.portfolio = new Portfolio(this.account);
    }

    public String getUsername() {
        return username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public Account getAccount() {
        return account;
    }

    public Portfolio getPortfolio() {
        return portfolio;
    }
}
