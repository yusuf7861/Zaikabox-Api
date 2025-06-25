# Zaikabox API

Zaikabox API is a Spring Boot-based backend service for managing food orders, billing, carts, and user authentication for a food delivery or restaurant management platform.

## Features
- User registration, authentication, and role management
- Food item management (CRUD)
- Cart operations (add, remove, update items)
- Order placement and tracking
- Billing and payment details
- Contact us support
- JWT-based authentication and authorization
- Azure and OpenAPI configuration

## Project Structure
```
src/main/java/tech/realworks/yusuf/zaikabox/
├── ZaikaboxApiApplication.java
├── config/                # Configuration classes (Security, Azure, OpenAPI)
├── controller/            # REST controllers (Billing, Cart, Food, User, etc.)
├── entity/                # JPA entities (User, Order, Food, etc.)
├── filter/                # JWT authentication filters
├── io/                    # Request/response DTOs
├── repository/            # Spring Data JPA repositories
├── service/               # Service interfaces and implementations
├── util/                  # Utility classes (JWT, etc.)
```

## Getting Started

### Prerequisites
- Java 17 or later
- Maven 3.6+
- (Optional) Docker

### Build & Run

1. **Clone the repository:**
   ```bash
   git clone <repo-url>
   cd Zaikabox-Api
   ```
2. **Configure application properties:**
   Edit `src/main/resources/application.properties` as needed for your environment (DB, Azure, JWT secrets, etc.).
3. **Build the project:**
   ```bash
   ./mvnw clean install
   ```
4. **Run the application:**
   ```bash
   ./mvnw spring-boot:run
   ```
   The API will be available at `http://localhost:8080` by default.

### API Documentation
- OpenAPI/Swagger docs available at `/swagger-ui.html` or `/v3/api-docs` when the app is running.
- See `api-docs.yaml` for the OpenAPI specification.

## Testing
Run all tests with:
```bash
./mvnw test
```

## Docker
To build and run with Docker:
```bash
docker build -t zaikabox-api .
docker run -p 8080:8080 zaikabox-api
```

## Contributing
Pull requests are welcome. For major changes, please open an issue first to discuss what you would like to change.

## License
[MIT](LICENSE)

