package com.carlosarroyoam.rest.books.author;

import com.carlosarroyoam.rest.books.author.dto.AuthorDto;
import com.carlosarroyoam.rest.books.author.dto.AuthorDto.AuthorDtoMapper;
import com.carlosarroyoam.rest.books.author.dto.AuthorSpecsDto;
import com.carlosarroyoam.rest.books.author.dto.CreateAuthorRequestDto;
import com.carlosarroyoam.rest.books.author.dto.UpdateAuthorRequestDto;
import com.carlosarroyoam.rest.books.author.entity.Author;
import com.carlosarroyoam.rest.books.author.entity.Author_;
import com.carlosarroyoam.rest.books.book.dto.BookDto;
import com.carlosarroyoam.rest.books.book.dto.BookDto.BookDtoMapper;
import com.carlosarroyoam.rest.books.core.constant.AppMessages;
import com.carlosarroyoam.rest.books.core.dto.PagedResponseDto;
import com.carlosarroyoam.rest.books.core.dto.PagedResponseDto.PagedResponseDtoMapper;
import com.carlosarroyoam.rest.books.core.specification.SpecificationBuilder;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthorService {
  private static final Logger log = LoggerFactory.getLogger(AuthorService.class);
  private final AuthorRepository authorRepository;

  public AuthorService(AuthorRepository authorRepository) {
    this.authorRepository = authorRepository;
  }

  public PagedResponseDto<AuthorDto> findAll(AuthorSpecsDto authorSpecs, Pageable pageable) {
    Specification<Author> spec = SpecificationBuilder.<Author>builder()
        .likeIfPresent(root -> root.get(Author_.name), authorSpecs.getName())
        .equalsIfPresent(root -> root.get(Author_.status), authorSpecs.getStatus())
        .build();

    Page<Author> authors = authorRepository.findAll(spec, pageable);

    return PagedResponseDtoMapper.INSTANCE
        .toPagedResponseDto(authors.map(AuthorDtoMapper.INSTANCE::toDto));
  }

  public AuthorDto findById(Long authorId) {
    Author authorById = findAuthorEntityById(authorId);
    return AuthorDtoMapper.INSTANCE.toDto(authorById);
  }

  @Transactional
  public AuthorDto create(CreateAuthorRequestDto requestDto) {
    LocalDateTime now = LocalDateTime.now();
    Author author = Author.builder()
        .name(requestDto.getName())
        .createdAt(now)
        .updatedAt(now)
        .build();

    return AuthorDtoMapper.INSTANCE.toDto(authorRepository.save(author));
  }

  @Transactional
  public void update(Long authorId, UpdateAuthorRequestDto requestDto) {
    LocalDateTime now = LocalDateTime.now();
    Author authorById = findAuthorEntityById(authorId);
    authorById.setName(requestDto.getName());
    authorById.setUpdatedAt(now);
    authorRepository.save(authorById);
  }

  @Transactional
  public void deleteById(Long authorId) {
    LocalDateTime now = LocalDateTime.now();
    Author authorById = findAuthorEntityById(authorId);
    authorById.setUpdatedAt(now);
    authorById.setDeletedAt(now);
    authorRepository.save(authorById);
  }

  public List<BookDto> findBooksByAuthorId(Long authorId) {
    Author authorById = findAuthorEntityById(authorId);
    return BookDtoMapper.INSTANCE.toDtos(authorById.getBooks());
  }

  private Author findAuthorEntityById(Long authorId) {
    return authorRepository.findById(authorId).orElseThrow(() -> {
      log.warn(AppMessages.AUTHOR_NOT_FOUND_EXCEPTION);
      return new ResponseStatusException(HttpStatus.NOT_FOUND,
          AppMessages.AUTHOR_NOT_FOUND_EXCEPTION);
    });
  }
}
