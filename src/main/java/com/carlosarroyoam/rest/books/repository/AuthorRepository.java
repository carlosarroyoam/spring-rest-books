package com.carlosarroyoam.rest.books.repository;

import com.carlosarroyoam.rest.books.entity.Author;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuthorRepository extends JpaRepository<Author, Long> {
}
