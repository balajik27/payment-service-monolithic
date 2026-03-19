

# Payment Service (Monolithic)

A Spring Boot-based payment platform that supports user registration, JWT authentication, multi-currency wallets, deposits, money transfers, refunds, and transaction history in a single monolithic application.

## Features

- JWT-based authentication and authorization
- User registration and login
- Automatic primary wallet creation during registration
- Multi-currency wallet support (`INR`, `USD`, `EUR`)
- Deposit funds into a wallet
- Create additional wallets per user
- User-to-user money transfers
- Refund processing with idempotency support
- Transaction history retrieval
- Global API response wrapper and exception handling

## Tech Stack

- Java 17
- Spring Boot 3.5.x
- Spring Security
- Spring Data JPA / Hibernate
- MySQL
- Maven
- Lombok
- JJWT

## Project Structure

```text
src/main/java/com/balaji/payment
├── common        # Shared API response, exception handling, JWT utilities
├── config        # Security configuration and application properties binding
├── transaction   # Transaction APIs, DTOs, entities, repository, service
├── user          # User APIs, DTOs, entities, repository, auth logic
└── wallet        # Wallet APIs, DTOs, entities, repository, service
````

## API Overview

### Public Endpoints

#### Register User

`POST /api/v1/users/register`

Registers a new user and creates a primary wallet automatically.

Example request:

```json
{
  "name": "Balaji",
  "email": "balaji@example.com",
  "password": "StrongPassword123",
  "currency": "INR"
}
```

#### Login User

`POST /api/v1/users/login`

Authenticates the user and returns a JWT token.

Example request:

```json
{
  "email": "balaji@example.com",
  "password": "StrongPassword123"
}
```

---

### Protected Endpoints

All endpoints below require:

```http
Authorization: Bearer <jwt-token>
```

#### Get User Wallets

`GET /api/v1/wallets/user/{userId}`

Returns all wallets owned by a user.

#### Deposit into Wallet

`POST /api/v1/wallets/deposit`

Example request:

```json
{
  "userId": "00000000-0000-0000-0000-000000000000",
  "amount": 500.00,
  "currency": "INR"
}
```

If `currency` is omitted, the primary wallet is used.

#### Add New Wallet

`POST /api/v1/wallets/add-wallet`

Example request:

```json
{
  "userId": "00000000-0000-0000-0000-000000000000",
  "currency": "USD"
}
```

#### Create Transaction

`POST /api/v1/transactions`

Transfers funds from one user to another.

Example request:

```json
{
  "senderId": "00000000-0000-0000-0000-000000000001",
  "receiverId": "00000000-0000-0000-0000-000000000002",
  "amount": 250.00,
  "idempotencyKey": "txn-001"
}
```

#### Refund Transaction

`POST /api/v1/transactions/refund`

Refunds a previously successful transaction.

Example request:

```json
{
  "transactionId": "00000000-0000-0000-0000-000000000010",
  "idempotencyKey": "refund-001"
}
```

#### Get Transaction History

`GET /api/v1/transactions/user/{userId}`

Returns transaction history for a user.

## Authentication

The application uses Spring Security with JWT authentication.

* Public routes:

  * `/api/v1/users/register`
  * `/api/v1/users/login`
* All other routes require a valid JWT token.
* Passwords are encrypted using `BCryptPasswordEncoder`.

## Wallet and Transaction Behavior

* A primary wallet is created when a user registers.
* The primary wallet is initialized with a starting balance of `100.00`.
* A user can create additional wallets in supported currencies.
* Transfers use the sender's primary wallet.
* The receiver wallet is selected by matching the sender currency when possible; otherwise the receiver's primary wallet is used.
* Transaction and refund operations are designed with idempotency support using a unique `idempotency_key`.

## Currency and Exchange Rules

Supported currencies:

* `INR`
* `USD`
* `EUR`

Cross-currency transactions apply:

* an exchange fee
* a conversion rate

Current implementation uses static exchange rates in code, which is fine for a demo but not for a real payment system.

## Configuration

Update `src/main/resources/application.properties` before running the project.

Example configuration:

```properties
spring.application.name=payment-service

payment.exchange.fee-rate=0.01
payment.exchange.default-rate=0.012
payment.supported-currencies=USD,EUR,INR

spring.datasource.url=jdbc:mysql://localhost:3306/payment_db?createDatabaseIfNotExist=true
spring.datasource.username=your_mysql_username
spring.datasource.password=your_mysql_password
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true

jwt.secret=your_base64_encoded_secret
jwt.expiration=86400000
```

## Getting Started

### Prerequisites

* Java 17
* Maven or Maven Wrapper
* MySQL

### Run Locally

Clone the repository:

```bash
git clone <your-repo-url>
cd payment-service
```

Run with Maven Wrapper:

```bash
./mvnw spring-boot:run
```

Or with Maven:

```bash
mvn spring-boot:run
```

## Testing

Run tests with:

```bash
mvn test
```

The project currently includes a Spring Boot context test and a transaction service unit test.

## Example Response Format

The APIs return a wrapped response format similar to this:

```json
{
  "success": true,
  "message": "Deposit successful",
  "data": {
    "id": "wallet-id",
    "userId": "user-id",
    "balance": 600.00,
    "currency": "INR",
    "status": "ACTIVE",
    "primary": true
  },
  "timestamp": "2026-03-19T10:00:00"
}
```

