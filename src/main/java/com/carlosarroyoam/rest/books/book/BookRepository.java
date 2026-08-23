package com.carlosarroyoam.rest.books.book;

import com.carlosarroyoam.rest.books.book.entity.Book;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

/** Acceso a datos de {@link Book} mediante Spring Data JPA. */
public interface BookRepository extends JpaRepository<Book, Long>, JpaSpecificationExecutor<Book> {

  /**
   * Busca los libros asociados a un autor.
   *
   * @param authorId id del autor
   * @return los libros del autor
   */
  @Query("SELECT b FROM Book b JOIN b.authors a WHERE a.id = :authorId")
  List<Book> findByAuthorId(Long authorId);

  /**
   * Indica si existe un libro con el ISBN dado.
   *
   * @param isbn ISBN a verificar
   * @return {@code true} si ya existe un libro con ese ISBN
   */
  boolean existsByIsbn(String isbn);
}
