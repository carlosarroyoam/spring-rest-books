package com.carlosarroyoam.rest.books.book;

import com.carlosarroyoam.rest.books.book.entity.Book;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

public interface BookRepository extends JpaRepository<Book, Long>, JpaSpecificationExecutor<Book> {
  @Query("SELECT b FROM Book b JOIN b.authors a WHERE a.id = :authorId")
  List<Book> findByAuthorId(Long authorId);

  boolean existsByIsbn(String isbn);
}
