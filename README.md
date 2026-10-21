# GitHub Repository Searcher

A Spring Boot application that searches GitHub repositories through the GitHub REST API, stores the results in a database, and serves the stored results with optional filters. It is a backend-only project (REST APIs, no UI) and can be tested with Postman.

## Tech stack

- Java 17+
- Spring Boot (Web, Data JPA, Validation)
- H2 database (in-memory by default)
- Lombok
- SpringDoc OpenAPI (Swagger UI)
- Maven

## How to run

1. Make sure Java 17 or newer is installed.
2. From the project root, start the app:

   ```bash
   ./mvnw spring-boot:run
   ```

   On Windows use `mvnw.cmd spring-boot:run`. You can also run `FreightFoxGithubAssignmentApplication` from IntelliJ.
3. The app starts on `http://localhost:8080`.

No database setup, API key or token is needed.

> **The database is in-memory**, so data is cleared on every restart.
> Call `POST /api/github/search` first to load data, then use `GET /api/github/repositories`.

### Useful URLs

| What | URL |
|---|---|
| Swagger UI | `http://localhost:8080/swagger-ui.html` |
| H2 console | `http://localhost:8080/h2-console` |

H2 console login: JDBC URL `jdbc:h2:mem:githubdb`, user `sa`, empty password.

## API

### 1. Search GitHub and save the results

`POST /api/github/search`

Request body:

```json
{
    "query": "spring boot",
    "language": "Java",
    "sort": "stars"
}
```

| Field | Required | Description |
|---|---|---|
| `query` | yes | Full or partial repository name |
| `language` | no | Programming language, e.g. `Java` |
| `sort` | no | `stars`, `forks` or `updated` (default `stars`) |

Response (`200`):

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

- Up to 20 results are fetched per search.
- A repository that already exists (same GitHub ID) is **updated, not duplicated**.
- If nothing matches, the response is `200` with `"message": "No repositories found"` and an empty list.

### 2. Retrieve stored repositories

`GET /api/github/repositories`

All parameters are optional and can be combined.

| Parameter | Description |
|---|---|
| `language` | Filter by language (case-insensitive) |
| `minStars` | Only repositories with at least this many stars |
| `sort` | `stars`, `forks` or `updated` (default `stars`), highest or newest first |

Example:

```
GET /api/github/repositories?language=Java&minStars=100&sort=stars
```

Response (`200`):

```json
{
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

## Error handling

All errors use the same JSON shape:

```json
{
    "timestamp": "2026-10-21T10:15:30Z",
    "status": 400,
    "error": "Bad Request",
    "message": "query must not be blank"
}
```

| Situation | Status |
|---|---|
| Blank or missing `query` | 400 |
| Invalid `sort` value | 400 |
| Missing or malformed JSON body | 400 |
| Invalid parameter type, e.g. `minStars=abc` | 400 |
| GitHub rejects the search query | 400 |
| GitHub rate limit reached | 429 |
| GitHub unreachable or returns a server error | 502 |
| GitHub returns an invalid response | 502 |
| Unexpected server error | 500 |

Calling `GET /api/github/search` returns `405` with a short usage message.

### GitHub rate limit

The app calls the GitHub search API without a token, which allows about 10 requests per minute. If the limit is reached, the API returns `429`; wait a minute and retry.

## Project structure

```
Controller   HTTP only: reads the request, calls the service
Service      All logic: query building, GitHub call, mapping, saving, filtering
Repository   Spring Data JPA interface
Entity       GithubRepository (the single table)
Exception    Global exception handler
```

## Design notes

- **Upsert:** the GitHub repository ID is the primary key (not auto-generated), so `saveAll` updates existing rows and inserts new ones.
- **One entity:** `GithubRepository` is the only table. GitHub's JSON is mapped straight onto it in the service.
- **Sorting:** the API values `stars`, `forks`, `updated` map to the entity fields `stars`, `forks`, `lastUpdated`.

## Switching to PostgreSQL

H2 is used so the project runs with no setup. To use PostgreSQL:

1. Add the PostgreSQL driver dependency to `pom.xml`.
2. Update `application.properties`:

   ```properties
   spring.datasource.url=jdbc:postgresql://localhost:5432/githubdb
   spring.datasource.username=<user>
   spring.datasource.password=<password>
   spring.datasource.driver-class-name=org.postgresql.Driver
   spring.jpa.hibernate.ddl-auto=update
   ```

No code changes are needed, because the code only uses JPA.

## Running the tests

```bash
./mvnw test
```
