package com.carlosarroyoam.rest.books.repository;

import com.carlosarroyoam.rest.books.entity.Book;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookRepository extends JpaRepository<Book, Long> {
  boolean existsByIsbn(String isbn);
}
