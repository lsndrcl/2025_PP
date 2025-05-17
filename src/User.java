public class User {
    private String username;
    private String passwordHash;
    private Account account;
    private Portfolio portfolio;

    public User() {
        // Needed for Jackson deserialization
    }

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
