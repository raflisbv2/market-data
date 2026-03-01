# Market Data Service

A Spring Boot application that ingests real-time market data (Bid/Ask events) via Kafka, aggregates them into Candlestick (OHLCV) charts, and exposes a REST API for historical data retrieval.

## Project Overview

This service is designed to handle high-frequency market data updates and provide aggregated views for charting applications.

### Key Features
*   **Stream Ingestion**: Consumes `BidAskEvent` messages from a Kafka topic (`market-data`).
*   **Real-time Aggregation**: Aggregates events into Candles (Open, High, Low, Close, Volume) for multiple timeframes:
    *   1 second (`1s`)
    *   5 seconds (`5s`)
    *   1 minute (`1m`)
    *   15 minutes (`15m`)
    *   1 hour (`1h`)
*   **In-Memory Storage**: Stores aggregated candle data in thread-safe in-memory maps for low-latency access.
*   **History API**: Provides a REST endpoint to query historical candle data.
*   **Data Simulation**: Includes a built-in producer that generates random market data for testing purposes.
*   **API Documentation**: Integrated Swagger UI for easy API exploration and testing.

## Tech Stack
*   **Java**: 21
*   **Framework**: Spring Boot 3.2.3
*   **Messaging**: Apache Kafka (Spring Kafka)
*   **Build Tool**: Maven
*   **Testing**: JUnit 5, Mockito
*   **Utilities**: Lombok
*   **Documentation**: SpringDoc OpenAPI (Swagger)

## Assumptions & Trade-offs

1.  **Storage (In-Memory)**:
    *   *Trade-off*: Data is stored in `ConcurrentHashMap`. This provides extremely fast read/write access but is **volatile**. All data is lost if the application restarts.
    *   *Assumption*: For this MVP/Proof of Concept, the dataset size fits within the available heap memory. In a production environment, this would be replaced with a time-series database (e.g., InfluxDB, TimescaleDB) or a persistent cache (Redis).

2.  **Price Aggregation**:
    *   *Assumption*: The `bid` price from the `BidAskEvent` is used to calculate the Open, High, Low, and Close prices.
    *   *Volume*: Volume is calculated as the "count of ticks" (number of updates), not the actual size of the trade (since `BidAskEvent` usually represents a quote, not a trade).

3.  **Time Alignment**:
    *   Candles are aligned based on Unix timestamps. For example, a 1-minute candle for `10:00:00` includes data from `10:00:00` up to (but not including) `10:01:00`.

4.  **Kafka Configuration**:
    *   The application is configured to use `localhost:9092` by default.
    *   The consumer group is set to `market-data-group` with `auto-offset-reset: earliest`.

## Getting Started

### Prerequisites
*   Java 21 SDK
*   Maven
*   A running Kafka instance (for running the app locally)

### Running the Application

1.  **Start Kafka** (if running locally):
    Ensure you have a Kafka broker running on `localhost:9092`.

2.  **Build and Run**:
    ```bash
    mvn clean spring-boot:run
    ```

    Once started, the internal `MarketDataProducer` will begin publishing simulated data to the `market-data` topic, and the `MarketDataListener` will consume and aggregate it.

### API Usage

**Swagger UI**:
You can explore and test the API using the Swagger UI at:
`http://localhost:8080/swagger-ui/index.html`

**Endpoint**: `GET /history`

**Parameters**:
*   `symbol`: e.g., `BTC-USD`
*   `interval`: `1s`, `5s`, `1m`, `15m`, `1h`
*   `from`: Start timestamp (Unix seconds)
*   `to`: End timestamp (Unix seconds)

**Example Request**:
```bash
curl "http://localhost:8080/history?symbol=BTC-USD&interval=1m&from=1620000000&to=1720000600"
```

**Response Format**:
```json
{
  "s": "ok",
  "t": [1620000000, 1620000060],  // Time
  "o": [50000.0, 50050.0],        // Open
  "h": [50100.0, 50100.0],        // High
  "l": [49900.0, 50000.0],        // Low
  "c": [50050.0, 50080.0],        // Close
  "v": [15, 8]                    // Volume
}
```

## Running Tests

The project includes Unit Tests

### Unit Tests
Tests the core aggregation logic in `CandleService`.
```bash
mvn test -Dtest=CandleServiceTest
```


### Run All Tests
```bash
mvn test
```
