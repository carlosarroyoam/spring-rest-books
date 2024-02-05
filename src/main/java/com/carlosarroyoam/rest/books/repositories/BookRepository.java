package com.carlosarroyoam.rest.books.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.carlosarroyoam.rest.books.entities.Book;

public interface BookRepository extends JpaRepository<Book, Long> {
}
