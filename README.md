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

### Analysis & Prediction
- Machine learning models for price prediction
- Technical indicators calculation
- Investment recommendation engine

## Project Structure

- Source code is in `src/main/java/com/myapp`
- Test code is in `src/test/java/com/myapp`

### Key Components

- `Main` - Main application entry point with console-based UI
- `User` - Represents a user with account and portfolio
- `Account` - Manages fiat currency balances and transactions
- `Portfolio` - Handles cryptocurrency holdings and trading
- `Transaction` - Represents financial transactions (deposits, withdrawals, trades)
- `CryptoService` - Provides cryptocurrency market data and analysis
- `CryptoAdvisor` - Offers investment recommendations based on ML analysis
- `LiveDataLoader` - Retrieves and processes real-time crypto data
- `UserManager` - Handles user authentication, registration, and data persistence

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

The application provides a console-based user interface:

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

1. When you start the application, you'll see the authentication menu with options to:
   - Login with existing credentials
   - Register a new account
   - Exit the application

2. After logging in, you can:
   - View account details
   - Deposit or withdraw funds
   - View transaction history
   - View your cryptocurrency portfolio
   - Trade cryptocurrencies (buy/sell)
   - Logout or exit the application

## Testing

The project includes JUnit 5 tests for core components:

- `UserTest` - Tests for user creation and initialization
- `AccountTest` - Tests for account operations and transactions
- `TransactionFilterTest` - Tests for transaction filtering functionality
- `AccountJsonTest` - Tests for transaction import/export functionality

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