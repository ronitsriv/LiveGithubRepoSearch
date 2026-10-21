# GitHub Repository Searcher

A backend-only Spring Boot application that searches GitHub repositories using the GitHub REST API, stores repository data using Spring Data JPA, and exposes REST endpoints to retrieve and filter saved results.

The application supports repository search, upsert operations, sorting, filtering, and structured error responses. It can be tested locally using Postman or through the deployed API.

## Live Deployment

**Base URL:** https://livegithubreposearch.onrender.com

> This project exposes REST APIs rather than a frontend. Opening the base URL (`/`) may return a Spring Boot Whitelabel 404 page because no homepage endpoint is defined.

### Quick API Links

| Purpose | Method | Endpoint |
|---|---|---|
| Search usage instructions | GET | [`/api/github/search`](https://livegithubreposearch.onrender.com/api/github/search) |
| Search GitHub repositories | POST | `/api/github/search` |
| Retrieve saved repositories | GET | [`/api/github/repositories`](https://livegithubreposearch.onrender.com/api/github/repositories) |
| Filter saved repositories | GET | `/api/github/repositories?language=Java&minStars=100&sort=stars` |
- **Swagger UI:** [Open API Documentation](https://livegithubreposearch.onrender.com/swagger-ui/index.html)
- **H2 Console:** [Open Database Console](https://livegithubreposearch.onrender.com/h2-console)

The search endpoint requires a POST request with a JSON body. The GET request to `/api/github/search` returns a short usage message with HTTP `405 Method Not Allowed`.

## Tech Stack

- **Language:** Java 17+
- **Framework:** Spring Boot
- **API:** Spring Web, REST
- **Persistence:** Spring Data JPA, Hibernate
- **Database:** H2 (in-memory by default)
- **Validation:** Jakarta Bean Validation
- **Utilities:** Lombok
- **API Documentation:** SpringDoc OpenAPI / Swagger UI
- **Build Tool:** Maven

## Features

- Search public GitHub repositories using the GitHub REST API.
- Filter searches by programming language.
- Sort search results by stars, forks, or last updated date.
- Fetch up to 20 repositories per search.
- Persist repository details using Spring Data JPA.
- Update existing records instead of creating duplicates, using the GitHub repository ID.
- Retrieve saved repositories independently of the GitHub search request.
- Filter stored repositories by language and minimum star count.
- Return structured JSON responses for successful requests and errors.

## API Documentation

### 1. Search GitHub Repositories

**Endpoint:** `POST /api/github/search`

Fetches repositories from GitHub and saves or updates the results in the database.

**Request body:**

```json
{
  "query": "spring boot",
  "language": "Java",
  "sort": "stars"
}
```

| Field | Required | Description |
|---|---|---|
| `query` | Yes | Search term for GitHub repositories |
| `language` | No | Programming language filter |
| `sort` | No | Sorting option: `stars`, `forks`, or `updated` |

**Example using curl:**

```bash
curl -X POST \
  "https://livegithubreposearch.onrender.com/api/github/search" \
  -H "Content-Type: application/json" \
  -d '{
    "query": "spring boot",
    "language": "Java",
    "sort": "stars"
  }'
```

**Expected response structure:**

```json
{
  "message": "Repositories fetched and saved successfully",
  "repositories": [
    {
      "id": 123456,
      "name": "spring-boot-example",
      "description": "An example repository for Spring Boot",
      "owner": "user123",
      "language": "Java",
      "stars": 450,
      "forks": 120,
      "openIssues": 12,
      "htmlUrl": "https://github.com/user123/spring-boot-example",
      "lastUpdated": "2024-01-01T12:00:00Z"
    }
  ]
}
```

The repository shown above is an illustrative example, not live response data.

### 2. Retrieve Saved Repositories

**Endpoint:** `GET /api/github/repositories`

Returns repositories currently stored in the database.

**Example:**

```bash
curl "https://livegithubreposearch.onrender.com/api/github/repositories"
```

### 3. Filter and Sort Repositories

All query parameters are optional and can be combined.

| Parameter | Description |
|---|---|
| `language` | Filter by programming language, case-insensitively |
| `minStars` | Return repositories with at least the specified number of stars |
| `sort` | Sort by `stars`, `forks`, or `updated` |

**Example:**

```http
GET /api/github/repositories?language=Java&minStars=100&sort=stars
```

This retrieves stored Java repositories with at least 100 stars, sorted by star count.

## Error Handling

The application is designed to return structured JSON errors for invalid input and upstream API failures.

Example error response:

```json
{
  "timestamp": "2026-10-21T10:15:30Z",
  "status": 400,
  "error": "Bad Request",
  "message": "query must not be blank"
}
```

| Scenario | HTTP Status |
|---|---:|
| Blank or missing search query | 400 |
| Invalid sorting option | 400 |
| Missing or malformed JSON body | 400 |
| Invalid query parameter type | 400 |
| GitHub rate limit exceeded | 429 |
| GitHub unavailable or returns an upstream server error | 502 |
| Unexpected application error | 500 |

Actual status codes depend on the exception handling and upstream response mapping implemented in the application.

## Database

The application uses an H2 in-memory database by default.

- No separate database server is required.
- Repository records are cleared when the application restarts.
- Run a search request before retrieving results from a fresh instance.

**H2 connection details for local development:**

```properties
spring.datasource.url=jdbc:h2:mem:githubdb
spring.datasource.username=sa
spring.datasource.password=
```

The H2 console is available locally at:

`http://localhost:8080/h2-console`

If the H2 console is enabled in the deployed environment, its availability will depend on the deployment configuration and security settings.

## Architecture

The application follows a layered Spring Boot architecture.

```text
Client (Postman / curl)
        |
        v
Controller
        |
        v
Service
        |
        +------> GitHub REST API
        |
        v
Spring Data JPA Repository
        |
        v
H2 Database
```

**Controller:** Handles HTTP requests, reads request bodies and query parameters, and delegates to the service layer.

**Service:** Builds GitHub search requests, calls the external API, maps responses to entities, saves results, and applies filtering and sorting.

**Repository:** Provides database operations through Spring Data JPA.

**Entity:** Represents the GitHub repository data stored in the database.

**Exception handling:** Provides consistent error responses for validation failures and runtime errors.

## Design Decisions

- **Upsert behaviour:** GitHub repository IDs are used as identifiers so existing repositories can be updated rather than duplicated.
- **Layered architecture:** HTTP handling, business logic, and data access are separated for maintainability.
- **JPA-based persistence:** Database operations are abstracted behind Spring Data JPA.
- **Stateless REST interface:** Clients interact with the application through HTTP requests and JSON responses.

## Running Locally

### Prerequisites

- Java 17 or newer
- Git
- Maven (optional if using the Maven Wrapper)

### Steps

1. Clone the repository.

   ```bash
   git clone <your-github-repository-url>
   cd <your-project-directory>
   ```

2. Start the application.

   **Windows:**

   ```bash
   mvnw.cmd spring-boot:run
   ```

   **Linux/macOS:**

   ```bash
   ./mvnw spring-boot:run
   ```

   Alternatively, run `FreightFoxGithubAssignmentApplication` from IntelliJ IDEA.

3. Test the endpoints using Postman or curl.

   ```http
   POST http://localhost:8080/api/github/search
   ```

   Send the JSON search body shown above, then retrieve saved data using:

   ```http
   GET http://localhost:8080/api/github/repositories
   ```

## Running Tests

Execute the test suite using the Maven Wrapper.

**Windows:**

```bash
mvnw.cmd test
```

**Linux/macOS:**

```bash
./mvnw test
```

## GitHub API Rate Limits

Unauthenticated GitHub API requests have rate limits. Search requests can be limited more strictly than ordinary API requests, and limits can change.

If the application returns HTTP `429`, wait for the rate limit to reset before retrying. Avoid repeatedly submitting identical requests while rate-limited.

## Switching to PostgreSQL

The application can be configured to use PostgreSQL instead of H2 by adding the PostgreSQL JDBC driver and updating the datasource configuration.

Example configuration for local development:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/githubdb
spring.datasource.username=${DB_USERNAME}
spring.datasource.password=${DB_PASSWORD}
spring.datasource.driver-class-name=org.postgresql.Driver
spring.jpa.hibernate.ddl-auto=update
```

Set the database credentials using environment variables. The existing JPA-based persistence layer can generally be retained, subject to database-specific configuration and compatibility.

## Future Improvements

- Add authenticated GitHub API requests to improve rate-limit capacity.
- Introduce persistent database storage for production deployments.
- Add pagination for larger search results.
- Add automated integration tests for GitHub API interactions.
- Add caching and retry handling for transient upstream failures.
- Secure and document production API endpoints with OpenAPI.

---

**Project:** GitHub Repository Searcher  
**Framework:** Spring Boot  
**Deployment:** Render  
**Interface:** REST API (backend-only)
