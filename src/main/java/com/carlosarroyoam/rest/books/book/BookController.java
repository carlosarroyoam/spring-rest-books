package com.carlosarroyoam.rest.books.book;

import com.carlosarroyoam.rest.books.author.dto.AuthorResponse;
import com.carlosarroyoam.rest.books.book.dto.BookResponse;
import com.carlosarroyoam.rest.books.book.dto.BookSpecs;
import com.carlosarroyoam.rest.books.book.dto.CreateBookRequest;
import com.carlosarroyoam.rest.books.book.dto.UpdateBookRequest;
import com.carlosarroyoam.rest.books.core.pagination.PagedResponse;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * Expone las operaciones REST sobre {@link com.carlosarroyoam.rest.books.book.entity.Book}: listado
 * paginado con filtros, consulta por id, alta, actualización, baja lógica y consulta de autores
 * asociados.
 */
@RestController
@RequestMapping("/books")
public class BookController {
  private final BookService bookService;

  public BookController(BookService bookService) {
    this.bookService = bookService;
  }

  /**
   * Lista libros de forma paginada, aplicando los filtros de {@link BookSpecs}.
   *
   * @param bookSpecs filtros opcionales de búsqueda
   * @param pageable configuración de página y orden
   * @return la página de libros encontrados
   */
  @GetMapping(produces = "application/json")
  public ResponseEntity<PagedResponse<BookResponse>> findAll(
      @Valid @ModelAttribute BookSpecs bookSpecs,
      @PageableDefault(page = 0, size = 25, sort = "id") Pageable pageable) {
    PagedResponse<BookResponse> books = bookService.findAll(bookSpecs, pageable);
    return ResponseEntity.ok(books);
  }

  /**
   * Busca un libro por su id.
   *
   * @param bookId id del libro a buscar
   * @return el libro encontrado
   */
  @GetMapping(value = "/{bookId}", produces = "application/json")
  public ResponseEntity<BookResponse> findById(@PathVariable Long bookId) {
    BookResponse bookById = bookService.findById(bookId);
    return ResponseEntity.ok(bookById);
  }

  /**
   * Crea un nuevo libro. Requiere rol {@code App/Admin}.
   *
   * @param request datos del libro a crear
   * @param builder utilizado para construir la URI del recurso creado
   * @return respuesta 201 con la cabecera {@code Location} del libro creado
   */
  @PostMapping(consumes = "application/json")
  @PreAuthorize("hasRole('App/Admin')")
  public ResponseEntity<Void> create(
      @Valid @RequestBody CreateBookRequest request, UriComponentsBuilder builder) {
    BookResponse createdBook = bookService.create(request);
    UriComponents uriComponents =
        builder.path("/books/{bookId}").buildAndExpand(createdBook.getId());
    return ResponseEntity.created(uriComponents.toUri()).build();
  }

  /**
   * Actualiza los datos de un libro. Requiere rol {@code App/Admin}.
   *
   * @param bookId id del libro a actualizar
   * @param request nuevos datos del libro
   */
  @PutMapping(value = "/{bookId}", consumes = "application/json")
  @PreAuthorize("hasRole('App/Admin')")
  public ResponseEntity<Void> update(
      @PathVariable Long bookId, @Valid @RequestBody UpdateBookRequest request) {
    bookService.update(bookId, request);
    return ResponseEntity.noContent().build();
  }

  /**
   * Elimina lógicamente un libro. Requiere rol {@code App/Admin}.
   *
   * @param bookId id del libro a eliminar
   */
  @DeleteMapping("/{bookId}")
  @PreAuthorize("hasRole('App/Admin')")
  public ResponseEntity<Void> deleteById(@PathVariable Long bookId) {
    bookService.deleteById(bookId);
    return ResponseEntity.noContent().build();
  }

  /**
   * Lista los autores asociados a un libro.
   *
   * @param bookId id del libro
   * @return los autores del libro
   */
  @GetMapping(path = "/{bookId}/authors", produces = "application/json")
  public ResponseEntity<List<AuthorResponse>> findBookAuthors(@PathVariable Long bookId) {
    List<AuthorResponse> authors = bookService.findAuthorsByBookId(bookId);
    return ResponseEntity.ok(authors);
  }
}
