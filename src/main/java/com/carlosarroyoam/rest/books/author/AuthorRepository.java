package com.carlosarroyoam.rest.books.author;

import com.carlosarroyoam.rest.books.author.entity.Author;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

public interface AuthorRepository
    extends JpaRepository<Author, Long>, JpaSpecificationExecutor<Author> {
  @Query("SELECT a FROM Author a JOIN a.books b WHERE b.id = :bookId")
  List<Author> findByBookId(Long bookId);
}
