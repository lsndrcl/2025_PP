# Java Bank Crypto Project

A Java application for cryptocurrency trading, portfolio management, and financial analysis.

## Overview

This application helps users manage their finances and make informed cryptocurrency investment decisions by:

- Managing user accounts with deposit/withdrawal functionality
- Tracking cryptocurrency portfolios with buy/sell capabilities
- Fetching real-time cryptocurrency price data from CoinGecko API
- Analyzing historical price data to provide investment recommendations
- Generating reports on portfolio performance
- Providing transaction history management and filtering

## Features

### Account Management
- Create and manage user accounts
- Track fiat currency balances
- Record transaction history
- Deposit and withdraw funds
- Import/export transactions from/to JSON files

### Cryptocurrency Portfolio
- Buy and sell cryptocurrencies
- Open and close short positions
- Track holdings across multiple coins
- View portfolio valuation in real-time

## Short Selling (Shorting)

Short selling allows opening positions by selling cryptocurrencies you do not own, with the expectation to buy them back later at a lower price, profiting from the difference.

- **Opening a short position**: you “borrow” a certain amount of crypto and sell it at the current price, posting collateral equal to the position’s value.
- **Closing a short position**: you buy back the cryptocurrency at the current market price to return the borrowed amount.
- **PnL (Profit and Loss) calculation**: the profit or loss is calculated as `(Entry Price - Current Price) × Amount`. A price drop results in a profit; a price increase causes a loss.


### Market Data
- Fetch current cryptocurrency prices
- Retrieve historical price data
- Support for 10 major cryptocurrencies (BTC, ETH, etc.)
- Data caching system to reduce API calls and improve performance

### Analysis & Prediction
- Machine learning model for price prediction
- Technical indicators calculation
- Investment recommendation engine
- Parallel processing for faster analysis of multiple cryptocurrencies

### User Interface
- Swing-based graphical user interface
- Login and registration dialog
- Account management panel
- Trading panel with real-time price updates
- Short position management table (open/close and view PnL)
- Transaction filtering and searching
- Portfolio visualization

## Project Structure

- Source code is in `src/main/java/com/myapp`
- Test code is in `src/test/java/com/myapp`

### Key Components

- `Main` - Main application entry point with GUI
- `User` - Represents a user with account and portfolio
- `Account` - Manages fiat currency balances and transactions
- `Portfolio` - Handles cryptocurrency holdings and trading (buy/sell and short/cover)
- `Transaction` - Represents financial transactions (deposits, withdrawals, trades)
- `CryptoService` - Provides cryptocurrency market data and analysis
- `CryptoAdvisor` - Offers investment recommendations based on ML analysis
- `LiveDataLoader` - Retrieves and processes real-time crypto data
- `UserManager` - Handles user authentication, registration, and data persistence
- `Position` - Represents a long or short position with symbol, amount, entry price, and timestamp


### UI Components
- `MainFrame` - Main application window
- `LoginDialog` - Authentication dialog for login/registration
- `AccountPanel` - Panel for managing account and transactions
- `TradingPanel` - Panel for cryptocurrency trading and price analysis
- `TransactionFilterDialog` - Dialog for filtering transaction history

## Getting Started

### Prerequisites
- Java 21 or higher
- Maven

### Installation

1. Clone the repository
```bashgit
git clone https://github.com/yourusername/java-bank-crypto-project.git
cd java-bank-crypto-project
```

2. Build the project
```bash
mvn clean install
```

### Running the Application

The application provides a graphical user interface:

```bash
mvn exec:java -Dexec.mainClass="com.myapp.Main"
```

To build an executable JAR with all dependencies:

```bash
mvn clean package assembly:single
```

Then run the JAR:

```bash
java -jar target/JavaBankCryptoProject-1.0-SNAPSHOT-jar-with-dependencies.jar
```

## Application Usage

1. When you start the application, you'll see the login (NOTE: Only local accounts) dialog with options to:
   - Login with existing credentials
   - Register a new account

2. After logging in, the main application window opens with tabs for:
   - Account Management - View balance, deposit/withdraw funds, and manage transactions
   - Trading - Buy/sell cryptocurrencies and view real-time market data
   - Portfolio Analysis - View portfolio performance and investment recommendations

3. Features include:
   - Deposit and withdraw funds from your account
   - Import and export transaction history as JSON
   - Filter transactions by type, amount, and date
   - Buy and sell cryptocurrencies with real-time pricing
   - Get ML-powered investment recommendations
   - View current market prices for major cryptocurrencies
   - Open short positions on supported cryptocurrencies (borrow and sell high)
   - Close short positions when the price drops (buy low to repay)
   - View and track open short positions and real-time profit & loss (PnL)

## Improvements

### Performance Enhancements
- Implemented data caching system to reduce API calls
- Added parallel processing for cryptocurrency analysis
- Optimized machine learning model training
- Added cancellation support for long-running operations

### UI Improvements
- Redesigned trading panel with better price formatting
- Added transaction filtering capabilities
- Improved error handling and user feedback
- Added loading indicators for background operations

### Code Quality
- Fixed import dependencies and removed unused imports
- Resolved naming conflicts between packages
- Added comprehensive test coverage for UI components
- Improved exception handling and error reporting



## ML Coin Recommender System

The application features a ML-powered cryptocurrency recommendation system that analyzes market data to suggest the most promising investment opportunities.

### How It Works

1. **Data Collection**:
   - The `CryptoAdvisor` class fetches historical price data (typically 14 days) for multiple cryptocurrencies
   - Data is retrieved from the CoinGecko API through the `LiveDataLoader` component
   - Supports analysis of 10 major cryptocurrencies including BTC, ETH, ADA, SOL, XRP, etc.

2. **Feature Engineering**:
   - For each cryptocurrency, the system calculates several technical indicators:
     - Previous day's price
     - 3-day moving average
     - 7-day moving average
     - 3-day volatility (standard deviation)
   - These features are used to train the machine learning model

3. **Machine Learning Model**:
   - Uses Weka's RandomForest algorithm for regression analysis
   - Each cryptocurrency gets its own trained model
   - Models are trained in real-time when recommendations are requested
   - Standardization is applied to normalize input features

4. **Prediction Process**:
   - The model predicts the next-day price for each cryptocurrency
   - Growth rate is calculated as: (predicted_price - current_price) / current_price
   - The cryptocurrency with the highest predicted growth rate is recommended

### Optimizations

1. **Multi-level Caching System**:
   - **File-based Cache**: Raw market data is stored in JSON files in the `data/cache` directory
     - Current prices cache expires after 15 minutes
     - Historical data cache expires after 60 minutes
     - Individual cryptocurrency data is cached separately
   - **Memory Cache**: Processed data (Weka Instances) is cached in memory
     - Avoids redundant processing of the same data within a session
     - Automatically expires based on configurable timeouts

2. **Parallel Processing**:
   - Multiple cryptocurrencies are analyzed simultaneously using Java's ExecutorService
   - Thread pool size is optimized based on available processors (capped at 4 to avoid API rate limits)
   - Results are collected in a ConcurrentHashMap for thread-safe operations

3. **API Rate Limiting**:
   - Implements automatic rate limiting to comply with CoinGecko's API restrictions
   - Spaces API calls to avoid hitting rate limits (approximately 50 calls per minute)
   - Automatic retry mechanism with exponential backoff for 429 (Too Many Requests) responses

4. **Cancellation Support**:
   - Long-running analysis can be cancelled by the user
   - Uses AtomicBoolean flags for thread-safe cancellation requests
   - Gracefully shuts down thread pools when cancellation is requested

The recommendation system balances accuracy, performance, and API usage to provide timely investment advice while respecting external API limitations.

## Testing

The project includes JUnit 5 tests for all components:

- Core functionality tests:
  - `UserTest` - Tests for user creation and initialization
  - `AccountTest` - Tests for account operations and transactions
  - `TransactionFilterTest` - Tests for transaction filtering functionality
  - `AccountJsonTest` - Tests for transaction import/export functionality

- Crypto functionality tests:
  - `CryptoServiceTest` - Tests for cryptocurrency data retrieval
  - `CryptoAdvisorTest` - Tests for investment recommendation engine
  - `LiveDataLoaderTest` - Tests for data loading and processing
  - `PortfolioTest` - Tests for portfolio management

- UI component tests:
  - `LoginDialogTest` - Tests for authentication dialog
  - `MainFrameTest` - Tests for main application window
  - `TradingPanelTest` - Tests for trading functionality

### Running Tests

To run all tests:

```bash
mvn test
```

To run a specific test class:

```bash
mvn test -Dtest=UserTest
```

## Development Challenges

During the development of this project, we encountered several significant challenges that required creative solutions:

### API Limitations and Integration

- **Finding a Free API**: One of our biggest initial challenges was finding a reliable, free cryptocurrency API that offered both current and historical data. After evaluating several options, we settled on CoinGecko's API due to its generous free tier, but even this came with limitations.
  
- **Rate Limiting**: CoinGecko's free API enforces strict rate limits (50 calls per minute), which required us to implement caching and request management to avoid hitting these limits during normal application usage.
  
- **Inconsistent Data Formats**: The API occasionally returned inconsistent data formats or incomplete data for certain cryptocurrencies, requiring robust error handling and data validation.

### Performance Optimization

- **Caching System**: We implemented a multi-level caching system (both file-based and in-memory) to minimize API calls. This required careful consideration of cache invalidation strategies and appropriate timeout values to balance freshness of data with performance.
  
- **Parallel Processing**: Running analysis on multiple cryptocurrencies simultaneously improved performance but introduced threading issues. We had to carefully manage thread pools and implement thread-safe data structures.
  
- **UI Responsiveness**: Ensuring the UI remained responsive during data fetching and analysis operations required implementing background workers and proper progress indication.

### Data Management and Persistence

- **JSON Serialization Edge Cases**: Handling special cases in JSON serialization/deserialization, especially with nested objects and collections in the Portfolio and Transaction classes, proved challenging.
  
- **Backup System**: Implementing an automatic backup system that wouldn't interfere with normal operation required careful consideration of timing and file management.
  
- **Data Integrity**: Ensuring data integrity across application restarts and during concurrent operations required implementing proper synchronization mechanisms.

### Debugging and Testing Challenges

- **Activity Errors**: One of the most persistent issues was handling "activity errors" where operations would fail due to UI thread blocking or background operations timing out. This required careful refactoring to separate UI and business logic.
  
- **Asynchronous Testing**: Testing components that relied on asynchronous operations (like API calls) required special testing approaches and sometimes mock objects.
  
- **Cross-Platform Compatibility**: Ensuring the application worked consistently across different operating systems (Windows, macOS, Linux) required addressing platform-specific UI rendering and file system access patterns.

### UI Design and Implementation

- **Responsive Layouts**: Creating layouts that would adapt properly to window resizing was challenging with Swing. We had to implement custom layout managers and carefully design component hierarchies.
  
- **Component Coordination**: Coordinating updates between different panels (e.g., ensuring the account balance updates when a transaction occurs in the trading panel) required implementing a proper event system.
  
- **Currency Formatting**: Implementing proper currency formatting with different symbols and decimal places for various currencies required custom formatting logic.

- **Backup Interface**: The UI is the main channel to use this program, however we added a commented out part of code in case the program needs to run only on terminal.

These challenges pushed us to deepen our understanding of Java, concurrent programming, API integration, and UI design principles. The solutions we developed not only addressed the immediate issues but also improved the overall architecture and robustness of the application.

### How we split the work (summary)

- **Alessandro R.**: Implemented various features and first functioning structure of data fetching and ML crypto advising, as well as setting up all the basic classes and UI/CLI aspects. Contributed to documentation.

- **Tashi P.**: Added various major features in both account and crypto sections, added details and helper methods in most classes, editing the UI respectively. Did testing. Contributed to documentation.

- **Abdellah K.**:

## License

This project is licensed under the MIT License - see the LICENSE file for details.