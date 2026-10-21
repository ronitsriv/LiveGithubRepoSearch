package org.example.freightfoxgithubassignment.Service;

import jakarta.persistence.criteria.Predicate;
import org.example.freightfoxgithubassignment.Entity.GithubRepository;
import org.example.freightfoxgithubassignment.Repository.GithubRepoRepository;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.JsonNode;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class GithubRepositoryService {

    // API sort value -> entity field name
    private static final Map<String, String> SORT_FIELDS = Map.of(
            "stars", "stars",
            "forks", "forks",
            "updated", "lastUpdated");

    private final RestClient restClient = RestClient.create();
    private final GithubRepoRepository githubRepoRepository;

    public GithubRepositoryService(GithubRepoRepository githubRepoRepository) {
        this.githubRepoRepository = githubRepoRepository;
    }

    public String searchUsage() {
        return """
                This URL is designed for POST requests only.
                Send a POST request to /api/github/search with a JSON body like this:

                {
                    "query": "spring boot",
                    "language": "Java",
                    "sort": "stars"
                }

                query    (required) : full or partial repository name
                language (optional) : programming language, e.g. Java
                sort     (optional) : stars, forks or updated (default: stars)
                """;
    }

    public Map<String, Object> searchAndSave(String query, String language, String sort) {
        if (query == null || query.isBlank()) {
            throw new IllegalArgumentException("query must not be blank");
        }
        String sortBy = (sort == null || sort.isBlank()) ? "stars" : sort;
        if (!SORT_FIELDS.containsKey(sortBy)) {
            throw new IllegalArgumentException("sort must be stars, forks or updated");
        }

        String q = query + " in:name";
        if (language != null && !language.isBlank()) {
            q += " language:" + language;
        }

        JsonNode root = restClient.get()
                .uri("https://api.github.com/search/repositories?q={q}&sort={sort}&per_page=20", q, sortBy)
                .header("Accept", "application/vnd.github+json")
                .retrieve()
                .body(JsonNode.class);

        List<GithubRepository> repos = new ArrayList<>();

        if (root == null || !root.has("items")) {
            throw new IllegalStateException("Received an invalid response from GitHub");
        }

        for (JsonNode item : root.path("items")) {
            repos.add(toEntity(item));
        }

        if (repos.isEmpty()) {
            return Map.of("message", "No repositories found", "repositories", repos);
        }
        List<GithubRepository> saved = githubRepoRepository.saveAll(repos);
        return Map.of("message", "Repositories fetched and saved successfully", "repositories", saved);
    }

    public Map<String, Object> getRepositories(String language, Integer minStars, String sort) {
        String sortBy = (sort == null || sort.isBlank()) ? "stars" : sort;
        String field = SORT_FIELDS.get(sortBy);
        if (field == null) {
            throw new IllegalArgumentException("sort must be stars, forks or updated");
        }

        List<GithubRepository> repos = githubRepoRepository
                .findAll(Sort.by(Sort.Direction.DESC, field))
                .stream()
                .filter(r -> language == null || language.isBlank()
                        || language.equalsIgnoreCase(r.getLanguage()))
                .filter(r -> minStars == null
                        || (r.getStars() != null && r.getStars() >= minStars))
                .toList();

        return Map.of("repositories", repos);
    }

    private GithubRepository toEntity(JsonNode item) {
        return GithubRepository.builder()
                .id(item.path("id").asLong())
                .name(item.path("name").asText())
                .description(item.path("description").asText(null))
                .owner(item.path("owner").path("login").asText(null))
                .language(item.path("language").asText(null))
                .stars(item.path("stargazers_count").asInt())
                .forks(item.path("forks_count").asInt())
                .openIssues(item.path("open_issues_count").asInt())
                .htmlUrl(item.path("html_url").asText(null))
                .lastUpdated(Instant.parse(item.path("updated_at").asText()))
                .build();
    }
}
