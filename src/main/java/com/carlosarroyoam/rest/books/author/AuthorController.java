package com.carlosarroyoam.rest.books.author;

import com.carlosarroyoam.rest.books.author.dto.AuthorResponse;
import com.carlosarroyoam.rest.books.author.dto.AuthorSpecs;
import com.carlosarroyoam.rest.books.author.dto.CreateAuthorRequest;
import com.carlosarroyoam.rest.books.author.dto.UpdateAuthorRequest;
import com.carlosarroyoam.rest.books.book.dto.BookResponse;
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
 * Expone las operaciones REST sobre {@link com.carlosarroyoam.rest.books.author.entity.Author}:
 * listado paginado con filtros, consulta por id, alta, actualización, baja lógica y consulta de
 * libros asociados.
 */
@RestController
@RequestMapping("/authors")
public class AuthorController {
  private final AuthorService authorService;

  public AuthorController(AuthorService authorService) {
    this.authorService = authorService;
  }

  /**
   * Lista autores de forma paginada, aplicando los filtros de {@link AuthorSpecs}.
   *
   * @param authorSpecs filtros opcionales de búsqueda
   * @param pageable configuración de página y orden
   * @return la página de autores encontrados
   */
  @GetMapping(produces = "application/json")
  public ResponseEntity<PagedResponse<AuthorResponse>> findAll(
      @Valid @ModelAttribute AuthorSpecs authorSpecs,
      @PageableDefault(page = 0, size = 25, sort = "id") Pageable pageable) {
    PagedResponse<AuthorResponse> authors = authorService.findAll(authorSpecs, pageable);
    return ResponseEntity.ok(authors);
  }

  /**
   * Busca un autor por su id.
   *
   * @param authorId id del autor a buscar
   * @return el autor encontrado
   */
  @GetMapping(path = "/{authorId}", produces = "application/json")
  public ResponseEntity<AuthorResponse> findById(@PathVariable Long authorId) {
    AuthorResponse authorById = authorService.findById(authorId);
    return ResponseEntity.ok(authorById);
  }

  /**
   * Crea un nuevo autor. Requiere rol {@code App/Admin}.
   *
   * @param request datos del autor a crear
   * @param builder utilizado para construir la URI del recurso creado
   * @return respuesta 201 con la cabecera {@code Location} del autor creado
   */
  @PostMapping(consumes = "application/json")
  @PreAuthorize("hasRole('App/Admin')")
  public ResponseEntity<Void> create(
      @Valid @RequestBody CreateAuthorRequest request, UriComponentsBuilder builder) {
    AuthorResponse createdAuthor = authorService.create(request);
    UriComponents uriComponents =
        builder.path("/authors/{authorId}").buildAndExpand(createdAuthor.getId());
    return ResponseEntity.created(uriComponents.toUri()).build();
  }

  /**
   * Actualiza el nombre y la biografía de un autor. Requiere rol {@code App/Admin}.
   *
   * @param authorId id del autor a actualizar
   * @param request nuevos datos del autor
   */
  @PutMapping(value = "/{authorId}", consumes = "application/json")
  @PreAuthorize("hasRole('App/Admin')")
  public ResponseEntity<Void> update(
      @PathVariable Long authorId, @Valid @RequestBody UpdateAuthorRequest request) {
    authorService.update(authorId, request);
    return ResponseEntity.noContent().build();
  }

  /**
   * Elimina lógicamente un autor. Requiere rol {@code App/Admin}.
   *
   * @param authorId id del autor a eliminar
   */
  @DeleteMapping("/{authorId}")
  @PreAuthorize("hasRole('App/Admin')")
  public ResponseEntity<Void> deleteById(@PathVariable Long authorId) {
    authorService.deleteById(authorId);
    return ResponseEntity.noContent().build();
  }

  /**
   * Lista los libros asociados a un autor.
   *
   * @param authorId id del autor
   * @return los libros del autor
   */
  @GetMapping(path = "/{authorId}/books", produces = "application/json")
  public ResponseEntity<List<BookResponse>> findBookAuthors(@PathVariable Long authorId) {
    List<BookResponse> books = authorService.findBooksByAuthorId(authorId);
    return ResponseEntity.ok(books);
  }
}
