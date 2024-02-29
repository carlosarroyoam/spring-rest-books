package com.carlosarroyoam.rest.books.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.carlosarroyoam.rest.books.entity.Author;

public interface AuthorRepository extends JpaRepository<Author, Long> {
}
