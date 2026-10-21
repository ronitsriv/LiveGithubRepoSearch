package org.example.freightfoxgithubassignment.Controller;

import org.example.freightfoxgithubassignment.Service.GithubRepositoryService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/*
 * HOW TO RUN (start the app, then use Postman)
 *
 * 1) Fetch from GitHub and save to H2
 *    POST http://localhost:8080/api/github/search
 *    Body (raw JSON):
 *    { "query": "spring boot", "language": "Java", "sort": "stars" }
 *    query (required), language (optional), sort (optional: stars | forks | updated)
 *
 * 2) Read saved results (data is in-memory, so run step 1 first)
 *    GET http://localhost:8080/api/github/repositories
 *    GET http://localhost:8080/api/github/repositories?language=Java&minStars=100&sort=stars
 *    language, minStars and sort are all optional and can be combined.
 *
 * H2 console: http://localhost:8080/h2-console
 *    JDBC URL: jdbc:h2:mem:githubdb, user: sa, password: (empty)
 */
@RestController
@RequestMapping("/api/github")
public class GithubRepositoryController {

    private final GithubRepositoryService service;

    public GithubRepositoryController(GithubRepositoryService service) {
        this.service = service;
    }

    @GetMapping("/search")
    public ResponseEntity<String> searchFallback() {
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED)
                .contentType(MediaType.TEXT_PLAIN)
                .body(service.searchUsage());
    }

    @PostMapping("/search")
    public Map<String, Object> search(@RequestBody Map<String, String> body) {
        return service.searchAndSave(body.get("query"), body.get("language"), body.get("sort"));
    }

    @GetMapping("/repositories")
    public Map<String, Object> getRepositories(
            @RequestParam(required = false) String language,
            @RequestParam(required = false) Integer minStars,
            @RequestParam(required = false) String sort) {
        return service.getRepositories(language, minStars, sort);
    }
}