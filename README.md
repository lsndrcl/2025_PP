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
- Track holdings across multiple coins
- View portfolio valuation in real-time

### Market Data
- Fetch current cryptocurrency prices
- Retrieve historical price data
- Support for 10+ major cryptocurrencies (BTC, ETH, etc.)
- Data caching system to reduce API calls and improve performance

### Analysis & Prediction
- Machine learning models for price prediction
- Technical indicators calculation
- Investment recommendation engine
- Parallel processing for faster analysis of multiple cryptocurrencies

### User Interface
- Swing-based graphical user interface
- Login and registration dialog
- Account management panel
- Trading panel with real-time price updates
- Transaction filtering and searching
- Portfolio visualization

## Project Structure

- Source code is in `src/main/java/com/myapp`
- Test code is in `src/test/java/com/myapp`

### Key Components

- `Main` - Main application entry point with GUI
- `User` - Represents a user with account and portfolio
- `Account` - Manages fiat currency balances and transactions
- `Portfolio` - Handles cryptocurrency holdings and trading
- `Transaction` - Represents financial transactions (deposits, withdrawals, trades)
- `CryptoService` - Provides cryptocurrency market data and analysis
- `CryptoAdvisor` - Offers investment recommendations based on ML analysis
- `LiveDataLoader` - Retrieves and processes real-time crypto data
- `UserManager` - Handles user authentication, registration, and data persistence

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
```bash
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

1. When you start the application, you'll see the login dialog with options to:
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
   - Get AI-powered investment recommendations
   - View current market prices for major cryptocurrencies

## Recent Improvements

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

## License

This project is licensed under the MIT License - see the LICENSE file for details.