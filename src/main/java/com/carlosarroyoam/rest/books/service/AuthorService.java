package com.carlosarroyoam.rest.books.service;

import com.carlosarroyoam.rest.books.constant.AppMessages;
import com.carlosarroyoam.rest.books.dto.AuthorResponseDto;
import com.carlosarroyoam.rest.books.dto.BookDto;
import com.carlosarroyoam.rest.books.dto.CreateAuthorRequestDto;
import com.carlosarroyoam.rest.books.dto.UpdateAuthorRequestDto;
import com.carlosarroyoam.rest.books.entity.Author;
import com.carlosarroyoam.rest.books.mapper.AuthorMapper;
import com.carlosarroyoam.rest.books.mapper.BookMapper;
import com.carlosarroyoam.rest.books.repository.AuthorRepository;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthorService {
  private static final Logger log = LoggerFactory.getLogger(AuthorService.class);
  private final AuthorRepository authorRepository;
  private final AuthorMapper authorMapper;
  private final BookMapper bookMapper;

  public AuthorService(final AuthorRepository authorRepository, final AuthorMapper authorMapper,
      final BookMapper bookMapper) {
    this.authorRepository = authorRepository;
    this.authorMapper = authorMapper;
    this.bookMapper = bookMapper;
  }

  public List<AuthorResponseDto> findAll(Integer page, Integer size) {
    Pageable pageable = PageRequest.of(page, size);
    Page<Author> authors = authorRepository.findAll(pageable);
    return authorMapper.toDtos(authors.getContent());
  }

  public AuthorResponseDto findById(Long authorId) {
    Author authorById = authorRepository.findById(authorId).orElseThrow(() -> {
      log.warn(AppMessages.AUTHOR_NOT_FOUND_EXCEPTION);
      return new ResponseStatusException(HttpStatus.NOT_FOUND,
          AppMessages.AUTHOR_NOT_FOUND_EXCEPTION);
    });

    return authorMapper.toDto(authorById);
  }

  @Transactional
  public AuthorResponseDto create(CreateAuthorRequestDto requestDto) {
    LocalDateTime now = LocalDateTime.now();
    Author author = authorMapper.toEntity(requestDto);
    author.setCreatedAt(now);
    author.setUpdatedAt(now);

    Author savedAuthor = authorRepository.save(author);
    return authorMapper.toDto(savedAuthor);
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

    return bookMapper.toDtos(authorById.getBooks());
  }
}
