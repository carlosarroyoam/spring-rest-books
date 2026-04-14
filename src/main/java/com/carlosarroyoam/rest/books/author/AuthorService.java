package com.carlosarroyoam.rest.books.author;

import com.carlosarroyoam.rest.books.author.dto.AuthorResponse;
import com.carlosarroyoam.rest.books.author.dto.AuthorResponse.AuthorResponseMapper;
import com.carlosarroyoam.rest.books.author.dto.AuthorSpecs;
import com.carlosarroyoam.rest.books.author.dto.CreateAuthorRequest;
import com.carlosarroyoam.rest.books.author.dto.UpdateAuthorRequest;
import com.carlosarroyoam.rest.books.author.entity.Author;
import com.carlosarroyoam.rest.books.author.entity.AuthorStatus;
import com.carlosarroyoam.rest.books.author.entity.Author_;
import com.carlosarroyoam.rest.books.book.dto.BookResponse;
import com.carlosarroyoam.rest.books.book.dto.BookResponse.BookResponseMapper;
import com.carlosarroyoam.rest.books.core.constant.AppMessages;
import com.carlosarroyoam.rest.books.core.dto.PagedResponse;
import com.carlosarroyoam.rest.books.core.dto.PagedResponse.PagedResponseMapper;
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

@Service
public class AuthorService {
  private static final Logger log = LoggerFactory.getLogger(AuthorService.class);
  private final AuthorRepository authorRepository;

  public AuthorService(AuthorRepository authorRepository) {
    this.authorRepository = authorRepository;
  }

  @Transactional(readOnly = true)
  public PagedResponse<AuthorResponse> findAll(AuthorSpecs authorSpecs, Pageable pageable) {
    Specification<Author> spec = SpecificationBuilder.<Author>builder()
        .likeIfPresent(root -> root.get(Author_.name), authorSpecs.getName())
        .equalsIfPresent(root -> root.get(Author_.status), authorSpecs.getStatus())
        .build();

    Page<Author> authors = authorRepository.findAll(spec, pageable);

    return PagedResponseMapper.INSTANCE
        .toPagedResponse(authors.map(AuthorResponseMapper.INSTANCE::toDto));
  }

  @Transactional(readOnly = true)
  public AuthorResponse findById(Long authorId) {
    Author authorById = findAuthorByIdOrFail(authorId);
    return AuthorResponseMapper.INSTANCE.toDto(authorById);
  }

  @Transactional
  public AuthorResponse create(CreateAuthorRequest request) {
    LocalDateTime now = LocalDateTime.now();
    Author author = Author.builder()
        .name(request.getName())
        .status(AuthorStatus.ACTIVE)
        .createdAt(now)
        .updatedAt(now)
        .build();

    return AuthorResponseMapper.INSTANCE.toDto(authorRepository.save(author));
  }

  @Transactional
  public void update(Long authorId, UpdateAuthorRequest request) {
    LocalDateTime now = LocalDateTime.now();
    Author authorById = findAuthorByIdOrFail(authorId);
    authorById.setName(request.getName());
    authorById.setUpdatedAt(now);
    authorRepository.save(authorById);
  }

  @Transactional
  public void deleteById(Long authorId) {
    LocalDateTime now = LocalDateTime.now();
    Author authorById = findAuthorByIdOrFail(authorId);
    authorById.setStatus(AuthorStatus.DELETED);
    authorById.setUpdatedAt(now);
    authorById.setDeletedAt(now);
    authorRepository.save(authorById);
  }

  @Transactional(readOnly = true)
  public List<BookResponse> findBooksByAuthorId(Long authorId) {
    Author authorById = findAuthorByIdOrFail(authorId);
    return BookResponseMapper.INSTANCE.toDtos(authorById.getBooks());
  }

  private Author findAuthorByIdOrFail(Long authorId) {
    return authorRepository.findById(authorId).orElseThrow(() -> {
      log.warn(AppMessages.AUTHOR_NOT_FOUND_EXCEPTION);
      return new ResponseStatusException(HttpStatus.NOT_FOUND,
          AppMessages.AUTHOR_NOT_FOUND_EXCEPTION);
    });
  }
}
