package com.carlosarroyoam.rest.books.book;

import com.carlosarroyoam.rest.books.book.entity.Book;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookRepository extends JpaRepository<Book, Long> {
  boolean existsByIsbn(String isbn);
}
