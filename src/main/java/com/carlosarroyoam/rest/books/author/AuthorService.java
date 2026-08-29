package com.carlosarroyoam.rest.books.author;

import com.carlosarroyoam.rest.books.author.dto.AuthorResponse;
import com.carlosarroyoam.rest.books.author.dto.AuthorResponse.AuthorResponseMapper;
import com.carlosarroyoam.rest.books.author.dto.AuthorSpecs;
import com.carlosarroyoam.rest.books.author.dto.CreateAuthorRequest;
import com.carlosarroyoam.rest.books.author.dto.UpdateAuthorRequest;
import com.carlosarroyoam.rest.books.author.entity.Author;
import com.carlosarroyoam.rest.books.author.entity.AuthorStatus;
import com.carlosarroyoam.rest.books.author.entity.Author_;
import com.carlosarroyoam.rest.books.book.BookRepository;
import com.carlosarroyoam.rest.books.book.dto.BookResponse;
import com.carlosarroyoam.rest.books.book.dto.BookResponse.BookResponseMapper;
import com.carlosarroyoam.rest.books.book.entity.Book;
import com.carlosarroyoam.rest.books.core.constant.AppMessages;
import com.carlosarroyoam.rest.books.core.pagination.PagedResponse;
import com.carlosarroyoam.rest.books.core.pagination.PagedResponse.PagedResponseMapper;
import com.carlosarroyoam.rest.books.core.specification.SpecificationBuilder;
import java.time.LocalDateTime;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

/**
 * Contiene la lógica de negocio de {@link Author}: búsqueda paginada con filtros, alta,
 * actualización, baja lógica y consulta de libros asociados a través de {@link BookRepository}.
 */
@Service
public class AuthorService {
  private static final Logger log = LoggerFactory.getLogger(AuthorService.class);
  private final AuthorRepository authorRepository;
  private final BookRepository bookRepository;

  public AuthorService(AuthorRepository authorRepository, BookRepository bookRepository) {
    this.authorRepository = authorRepository;
    this.bookRepository = bookRepository;
  }

  /**
   * Busca autores de forma paginada aplicando los filtros de {@link AuthorSpecs}.
   *
   * @param authorSpecs filtros opcionales de búsqueda
   * @param pageable configuración de página y orden
   * @return la página de autores encontrados
   */
  @Transactional(readOnly = true)
  public PagedResponse<AuthorResponse> findAll(AuthorSpecs authorSpecs, Pageable pageable) {
    Specification<Author> spec =
        SpecificationBuilder.<Author>builder()
            .likeIfPresent(root -> root.get(Author_.name), authorSpecs.getName())
            .equalsIfPresent(root -> root.get(Author_.status), authorSpecs.getStatus())
            .build();

    Page<Author> authors = authorRepository.findAll(spec, pageable);

    return PagedResponseMapper.INSTANCE.toPagedResponse(
        authors.map(AuthorResponseMapper.INSTANCE::toDto));
  }

  /**
   * Busca un autor por su id.
   *
   * @param authorId id del autor a buscar
   * @return el autor encontrado
   */
  @Transactional(readOnly = true)
  public AuthorResponse findById(Long authorId) {
    Author authorById = findAuthorByIdOrFail(authorId);
    return AuthorResponseMapper.INSTANCE.toDto(authorById);
  }

  /**
   * Crea un autor con estado {@code ACTIVE}.
   *
   * @param request datos del autor a crear
   * @return el autor creado
   */
  @Transactional
  public AuthorResponse create(CreateAuthorRequest request) {
    LocalDateTime now = LocalDateTime.now();
    Author author =
        Author.builder()
            .name(request.getName())
            .bio(request.getBio())
            .status(AuthorStatus.ACTIVE)
            .createdAt(now)
            .updatedAt(now)
            .build();

    return AuthorResponseMapper.INSTANCE.toDto(authorRepository.save(author));
  }

  /**
   * Actualiza el nombre y la biografía de un autor existente.
   *
   * @param authorId id del autor a actualizar
   * @param request nuevos datos del autor
   */
  @Transactional
  public void update(Long authorId, UpdateAuthorRequest request) {
    LocalDateTime now = LocalDateTime.now();
    Author authorById = findAuthorByIdOrFail(authorId);
    authorById.setName(request.getName());
    authorById.setBio(request.getBio());
    authorById.setUpdatedAt(now);
    authorRepository.save(authorById);
  }

  /**
   * Marca un autor como {@code DELETED} y registra la fecha de baja, sin eliminar el registro.
   *
   * @param authorId id del autor a eliminar
   */
  @Transactional
  public void deleteById(Long authorId) {
    LocalDateTime now = LocalDateTime.now();
    Author authorById = findAuthorByIdOrFail(authorId);
    authorById.setStatus(AuthorStatus.DELETED);
    authorById.setUpdatedAt(now);
    authorById.setDeletedAt(now);
    authorRepository.save(authorById);
  }

  /**
   * Lista los libros asociados a un autor.
   *
   * @param authorId id del autor
   * @return los libros del autor
   */
  @Transactional(readOnly = true)
  public List<BookResponse> findBooksByAuthorId(Long authorId) {
    List<Book> booksByAuthorId = bookRepository.findByAuthorId(authorId);
    return BookResponseMapper.INSTANCE.toDtos(booksByAuthorId);
  }

  /**
   * Busca un autor por id o lanza {@code 404 Not Found} si no existe.
   *
   * @param authorId id del autor a buscar
   * @return el autor encontrado
   */
  private Author findAuthorByIdOrFail(Long authorId) {
    return authorRepository
        .findById(authorId)
        .orElseThrow(
            () -> {
              log.warn(AppMessages.AUTHOR_NOT_FOUND_EXCEPTION);
              return new ResponseStatusException(
                  HttpStatus.NOT_FOUND, AppMessages.AUTHOR_NOT_FOUND_EXCEPTION);
            });
  }
}
