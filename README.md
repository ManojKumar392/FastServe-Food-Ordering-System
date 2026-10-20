# FastServe

FastServe is a REST API for a food-ordering service, built with Spring Boot and Java 21. It supports user authentication, restaurant and menu discovery, order management, mock payments with payment history, and restaurant reviews.

> **Development status:** Payments use a mock gateway. The application currently uses Hibernate schema updates (`ddl-auto=update`); configure a migration strategy and a real payment provider before production use.

## Features

- Stateless authentication with JWT access tokens and refresh tokens
- Restaurant and menu management, search, and pagination
- Order creation, status updates, cancellation, and payment retries
- Payment history and review/rating endpoints
- PostgreSQL persistence, Redis-backed caching, and login rate limiting
- OpenAPI documentation through Swagger UI

## Requirements

- Java 21
- Docker Desktop with Docker Compose (recommended for running the full stack)

The Maven wrapper is included, so a separate Maven installation is not required.

## Run with Docker Compose

From the repository root, create a `.env` file (it is ignored by Git) with your own values:

```dotenv
DB_PASSWORD=replace-with-a-local-database-password
JWT_SECRET=replace-with-a-random-secret-at-least-32-bytes-long
```

Generate a suitable JWT secret with `openssl rand -base64 32`. Keep these values private and do not commit `.env`.

Start the API, PostgreSQL, and Redis:

```shell
docker compose up --build
```

The API listens on `http://localhost:8080`. Stop the containers with `Ctrl+C`; use `docker compose down` to remove the containers. PostgreSQL data is kept in the `postgres-data` volume. To delete that persisted data as well, run `docker compose down --volumes`.

## Run locally

Start PostgreSQL and Redis separately, with PostgreSQL available at `localhost:5432` and Redis at `localhost:6379`. Set `DB_PASSWORD` to the PostgreSQL password and `JWT_SECRET` to a random value of at least 32 bytes. The default configuration uses the Redis hostname `redis`; when Redis is running on the local machine, override that hostname:

```powershell
$env:DB_PASSWORD = "your-local-database-password"
$env:JWT_SECRET = "your-random-secret-at-least-32-bytes-long"
$env:SPRING_DATA_REDIS_HOST = "localhost"
.\mvnw.cmd spring-boot:run
```

On macOS or Linux, use `./mvnw spring-boot:run` after exporting the same environment variables.

## Configuration

| Variable | Purpose |
| --- | --- |
| `DB_PASSWORD` | PostgreSQL password |
| `JWT_SECRET` | HMAC key used to sign access and refresh tokens; use a random secret of at least 32 bytes |
| `SPRING_PROFILES_ACTIVE` | Selects a Spring profile; Docker Compose uses `docker` |
| `SPRING_DATA_REDIS_HOST` | Redis hostname; defaults to `redis` |

The Docker profile connects to the Compose services by their service names (`postgres` and `redis`). The default profile expects PostgreSQL at `localhost:5432` and uses the Redis hostname `redis`. The `dev` and `prod` profiles are present, but are not fully configured for standalone or production deployment.

## API overview

All endpoints require an access token in the `Authorization: Bearer <accessToken>` header except signup, login, token refresh/logout, Swagger/OpenAPI documentation, and the health endpoint. The all-orders listing and administrative order payment/status updates additionally require the `ADMIN` role.

| Method | Path | Description |
| --- | --- | --- |
| `POST` | `/users/signup` | Register a user |
| `POST` | `/users/login?email=...&password=...` | Log in; returns access and refresh tokens |
| `POST` | `/users/refresh` | Exchange a refresh token |
| `POST` | `/users/logout` | Revoke a refresh token |
| `GET` | `/restaurants` | List restaurants (paged) |
| `GET` | `/restaurants/search?name=...&location=...` | Search restaurants |
| `POST` | `/restaurants` | Create a restaurant |
| `GET` | `/restaurants/{id}/menu` | List a restaurant's menu |
| `POST` | `/restaurants/{id}/menu` | Add a menu item |
| `PUT` | `/restaurants/{id}` | Update a restaurant |
| `DELETE` | `/restaurants/{id}` | Soft-delete a restaurant |
| `PUT` | `/restaurants/{id}/restore` | Restore a restaurant |
| `GET` | `/menu/search` | Search menu items (paged; `name`, `minPrice`, `maxPrice`, `restaurantId`) |
| `POST` | `/orders` | Place an order |
| `GET` | `/orders/my` | List the current user's orders (paged) |
| `GET` | `/orders/{id}` | Get an order |
| `PATCH` | `/orders/{id}/cancel` | Cancel an order |
| `POST` | `/orders/{id}/retry-payment` | Retry a failed payment |
| `GET` | `/orders` | List all orders (admin; paged) |
| `PATCH` | `/orders/{id}/pay` | Mark an order as paid (admin) |
| `PATCH` | `/orders/{id}/status?status=...` | Change order status (admin) |
| `GET` | `/orders/{orderId}/payment-history` | Get an order's payment history |
| `POST` | `/restaurants/{restaurantId}/reviews` | Add a review |
| `GET` | `/restaurants/{restaurantId}/reviews` | List reviews |
| `GET` | `/restaurants/{restaurantId}/average-rating` | Get the average rating |

Paged endpoints accept Spring Data pagination parameters such as `page` and `size`.

### Example requests

Sign up:

```http
POST /users/signup
Content-Type: application/json

{
  "name": "Alex Customer",
  "email": "alex@example.com",
  "password": "choose-a-password",
  "address": "123 Example Street"
}
```

Log in using query parameters:

```http
POST /users/login?email=alex%40example.com&password=choose-a-password
```

Place an order with the returned access token:

```http
POST /orders
Authorization: Bearer <accessToken>
Content-Type: application/json

{
  "paymentMode": "CARD",
  "items": [
    {
      "menuItemId": 1,
      "quantity": 2
    }
  ]
}
```

Valid payment modes are `CASH`, `CARD`, and `UPI`.

## API documentation and health

- Swagger UI: `http://localhost:8080/swagger-ui/index.html`
- OpenAPI spec: `http://localhost:8080/v3/api-docs`
- Health endpoint: `http://localhost:8080/actuator/health`

## Tests

Run the test suite with the Maven wrapper:

```powershell
.\mvnw.cmd test
```

On macOS or Linux, use `./mvnw test`.
