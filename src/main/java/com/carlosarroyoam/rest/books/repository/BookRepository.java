package com.carlosarroyoam.rest.books.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.carlosarroyoam.rest.books.entity.Book;

public interface BookRepository extends JpaRepository<Book, Long> {
	boolean existsByIsbn(String isbn);
}
