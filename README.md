# Weather Info for Pincode

Spring Boot backend solution for the Weather Info for Pincode assignment.

## What it does

The API accepts a pincode and a date, resolves the pincode to latitude/longitude, fetches weather for that date, stores the data in MySQL, and reuses stored data on later requests.

The implementation follows the assignment requirements:
- one REST API
- relational database persistence
- pincode latitude/longitude stored separately
- weather data stored separately
- external API calls optimized by reusing saved data
- Postman/Swagger testability
- structured backend code
- JUnit tests

## API

### POST /api/v1/weather

Request:

```json
{
  "pincode": "411014",
  "for_date": "2020-10-15"
}
```

Example response:

```json
{
  "pincode": "411014",
  "for_date": "2020-10-15",
  "latitude": 18.5679,
  "longitude": 73.9143,
  "minTemperature": 20.0,
  "maxTemperature": 31.0,
  "afternoonTemperature": 28.0,
  "afternoonHumidity": 60,
  "afternoonPressure": 1012,
  "windSpeed": 5.5,
  "precipitation": 0.0,
  "cloudCover": 20,
  "cached": false
}
```

`cached=true` means the weather record was returned from the database without calling the weather provider.

## API-call optimization

Two tables are used:

`locations`
- pincode
- latitude
- longitude

`weather_data`
- location
- forecast date
- weather values

The request flow is:

```text
First request for a pincode/date
pincode -> geocoding API -> save coordinates
latitude/longitude -> weather API -> save weather
```

```text
Same pincode/date again
weather_data -> return saved result
```

```text
Same pincode/different date
locations -> reuse saved coordinates
latitude/longitude -> weather API -> save new date
```

Unique database constraints prevent duplicate pincode records and duplicate weather records for the same location/date.

## OpenWeather reference and assumption

The assignment references OpenWeather's Current Weather API. That reference shows the latitude/longitude weather request and the weather response structure.

The assignment also requires a `for_date` value, including the sample date `2020-10-15`. The supplied OpenWeather reference documents the Current Weather API, which returns current conditions rather than arbitrary historical dates. As a reasonable implementation assumption, this project uses that referenced endpoint and stores the requested `for_date` alongside the returned weather snapshot. For a live demonstration, use the current date. The database cache still prevents repeated provider calls for the same pincode/date.

Geocoding uses OpenWeather's postal-code endpoint:

```text
/geo/1.0/zip?zip={pincode},IN
```

The OpenWeather API key is read from the environment and is never stored in source code.

## Database

MySQL is used as the relational database. The assignment does not require a specific database vendor.

Create the database:

```sql
CREATE DATABASE weather_assignment;
```

Set environment variables:

```text
DB_USERNAME=root
DB_PASSWORD=your_mysql_password
OPENWEATHER_API_KEY=your_openweather_api_key
SERVER_PORT=8080
```

Default JDBC URL:

```text
jdbc:mysql://localhost:3306/weather_assignment
```

## Run

Requirements:
- Java 21
- Maven
- MySQL 8+
- OpenWeather API key with access to the Current Weather endpoint

Run tests:

```bash
mvn clean test
```

Run the application:

```bash
mvn spring-boot:run
```

Swagger UI:

```text
http://localhost:8080/swagger-ui.html
```

## Postman

Import:

```text
postman/Weather-Pincode-API.postman_collection.json
```

The collection demonstrates:
- first request
- repeated request/cache hit
- same pincode with a different date
- invalid pincode

## Tests

The test suite covers the main business paths:
- cache hit with no external API calls
- reuse of saved coordinates for a new date
- geocoding for a new pincode
- valid HTTP request
- invalid pincode validation

The service tests mock the external clients, so the business logic can be tested without making real OpenWeather calls.

## Project structure

```text
src/main/java/com/example/weather
├── client
├── controller
├── dto
├── entity
├── exception
├── repository
└── service
```
