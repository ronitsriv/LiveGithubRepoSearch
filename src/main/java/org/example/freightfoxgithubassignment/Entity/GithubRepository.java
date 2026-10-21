package org.example.freightfoxgithubassignment.Entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.Instant;

@Entity
@Table(name = "github_repository")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class GithubRepository {

    @Id
    private Long id;                 // GitHub's repo ID, so no @GeneratedValue

    @Column(nullable = false)
    private String name;

    @Column(length = 2000)
    private String description;      // can be null on GitHub

    private String owner;            // from owner.login

    private String language;         // can be null on GitHub

    private Integer stars;           // stargazers_count
    private Integer forks;           // forks_count
    private Integer openIssues;      // open_issues_count
    private String htmlUrl;          // html_url

    private Instant lastUpdated;     // updated_at
}