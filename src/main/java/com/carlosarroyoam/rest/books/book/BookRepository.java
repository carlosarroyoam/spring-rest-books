package com.carlosarroyoam.rest.books.book;

import com.carlosarroyoam.rest.books.book.entity.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface BookRepository extends JpaRepository<Book, Long>, JpaSpecificationExecutor<Book> {
  boolean existsByIsbn(String isbn);
}
