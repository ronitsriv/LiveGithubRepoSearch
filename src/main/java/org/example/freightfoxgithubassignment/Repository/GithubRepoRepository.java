package org.example.freightfoxgithubassignment.Repository;

import org.example.freightfoxgithubassignment.Entity.GithubRepository;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GithubRepoRepository extends JpaRepository<GithubRepository,Long> {
}
