package com.carlosarroyoam.rest.books.author;

import com.carlosarroyoam.rest.books.author.entity.Author;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

/** Acceso a datos de {@link Author} mediante Spring Data JPA. */
public interface AuthorRepository
    extends JpaRepository<Author, Long>, JpaSpecificationExecutor<Author> {

  /**
   * Busca los autores asociados a un libro.
   *
   * @param bookId id del libro
   * @return los autores del libro
   */
  @Query("SELECT a FROM Author a JOIN a.books b WHERE b.id = :bookId")
  List<Author> findByBookId(Long bookId);
}
