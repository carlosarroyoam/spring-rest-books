package com.carlosarroyoam.rest.books.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.carlosarroyoam.rest.books.entities.Author;

public interface AuthorRepository extends JpaRepository<Author, Long> {
}
