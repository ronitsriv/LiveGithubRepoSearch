package org.example.freightfoxgithubassignment;

import org.example.freightfoxgithubassignment.Entity.GithubRepository;
import org.example.freightfoxgithubassignment.Repository.GithubRepoRepository;
import org.example.freightfoxgithubassignment.Service.GithubRepositoryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GithubRepositoryServiceTest {

    private GithubRepoRepository repo;
    private GithubRepositoryService service;

    @BeforeEach
    void setUp() {
        repo = mock(GithubRepoRepository.class);   // fake database
        service = new GithubRepositoryService(repo);
    }

    @Test
    void blankQueryIsRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> service.searchAndSave("  ", null, null));
    }

    @Test
    void invalidSortIsRejectedOnSearch() {
        assertThrows(IllegalArgumentException.class,
                () -> service.searchAndSave("spring", null, "banana"));
    }

    @Test
    void invalidSortIsRejectedOnGet() {
        assertThrows(IllegalArgumentException.class,
                () -> service.getRepositories(null, null, "banana"));
    }

    @Test
    void filtersByLanguage() {
        when(repo.findAll(any(Sort.class))).thenReturn(List.of(
                repo("a", "Java", 500),
                repo("b", "Python", 900)));

        Map<String, Object> result = service.getRepositories("java", null, "stars");

        assertEquals(1, ((List<?>) result.get("repositories")).size());
    }

    @Test
    void filtersByMinStars() {
        when(repo.findAll(any(Sort.class))).thenReturn(List.of(
                repo("a", "Java", 50),
                repo("b", "Java", 500)));

        Map<String, Object> result = service.getRepositories(null, 100, "stars");

        assertEquals(1, ((List<?>) result.get("repositories")).size());
    }

    private GithubRepository repo(String name, String language, int stars) {
        return GithubRepository.builder().name(name).language(language).stars(stars).build();
    }
}
