package com.carlosarroyoam.rest.books.author;

import com.carlosarroyoam.rest.books.author.dto.AuthorDto;
import com.carlosarroyoam.rest.books.author.dto.AuthorDto.AuthorDtoMapper;
import com.carlosarroyoam.rest.books.author.dto.AuthorFilterDto;
import com.carlosarroyoam.rest.books.author.dto.CreateAuthorRequestDto;
import com.carlosarroyoam.rest.books.author.dto.UpdateAuthorRequestDto;
import com.carlosarroyoam.rest.books.author.entity.Author;
import com.carlosarroyoam.rest.books.book.dto.BookDto;
import com.carlosarroyoam.rest.books.book.dto.BookDto.BookDtoMapper;
import com.carlosarroyoam.rest.books.core.constant.AppMessages;
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

  public AuthorService(final AuthorRepository authorRepository) {
    this.authorRepository = authorRepository;
  }

  public List<AuthorDto> findAll(Pageable pageable, AuthorFilterDto filters) {
    Specification<Author> spec = Specification.unrestricted();
    spec = spec.and(AuthorSpecification.nameContains(filters.getName()));

    Page<Author> authors = authorRepository.findAll(spec, pageable);
    return AuthorDtoMapper.INSTANCE.toDtos(authors.getContent());
  }

  public AuthorDto findById(Long authorId) {
    Author authorById = authorRepository.findById(authorId).orElseThrow(() -> {
      log.warn(AppMessages.AUTHOR_NOT_FOUND_EXCEPTION);
      return new ResponseStatusException(HttpStatus.NOT_FOUND,
          AppMessages.AUTHOR_NOT_FOUND_EXCEPTION);
    });

    return AuthorDtoMapper.INSTANCE.toDto(authorById);
  }

  @Transactional
  public AuthorDto create(CreateAuthorRequestDto requestDto) {
    LocalDateTime now = LocalDateTime.now();
    Author author = AuthorDtoMapper.INSTANCE.toEntity(requestDto);
    author.setCreatedAt(now);
    author.setUpdatedAt(now);
    return AuthorDtoMapper.INSTANCE.toDto(authorRepository.save(author));
  }

  @Transactional
  public void update(Long authorId, UpdateAuthorRequestDto requestDto) {
    Author authorById = authorRepository.findById(authorId).orElseThrow(() -> {
      log.warn(AppMessages.AUTHOR_NOT_FOUND_EXCEPTION);
      return new ResponseStatusException(HttpStatus.NOT_FOUND,
          AppMessages.AUTHOR_NOT_FOUND_EXCEPTION);
    });

    authorById.setName(requestDto.getName());
    authorById.setUpdatedAt(LocalDateTime.now());
    authorRepository.save(authorById);
  }

  @Transactional
  public void deleteById(Long authorId) {
    if (Boolean.FALSE.equals(authorRepository.existsById(authorId))) {
      log.warn(AppMessages.AUTHOR_NOT_FOUND_EXCEPTION);
      throw new ResponseStatusException(HttpStatus.NOT_FOUND,
          AppMessages.AUTHOR_NOT_FOUND_EXCEPTION);
    }

    authorRepository.deleteById(authorId);
  }

  public List<BookDto> findBooksByAuthorId(Long authorId) {
    Author authorById = authorRepository.findById(authorId).orElseThrow(() -> {
      log.warn(AppMessages.AUTHOR_NOT_FOUND_EXCEPTION);
      return new ResponseStatusException(HttpStatus.NOT_FOUND,
          AppMessages.AUTHOR_NOT_FOUND_EXCEPTION);
    });

    return BookDtoMapper.INSTANCE.toDtos(authorById.getBooks());
  }
}
