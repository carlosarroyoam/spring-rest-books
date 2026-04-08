package com.carlosarroyoam.rest.books.book;

import com.carlosarroyoam.rest.books.author.dto.AuthorDto;
import com.carlosarroyoam.rest.books.author.dto.AuthorDto.AuthorDtoMapper;
import com.carlosarroyoam.rest.books.author.entity.Author_;
import com.carlosarroyoam.rest.books.book.dto.BookDto;
import com.carlosarroyoam.rest.books.book.dto.BookDto.BookDtoMapper;
import com.carlosarroyoam.rest.books.book.dto.BookSpecsDto;
import com.carlosarroyoam.rest.books.book.dto.CreateBookRequestDto;
import com.carlosarroyoam.rest.books.book.dto.UpdateBookRequestDto;
import com.carlosarroyoam.rest.books.book.entity.Book;
import com.carlosarroyoam.rest.books.book.entity.BookStatus;
import com.carlosarroyoam.rest.books.book.entity.Book_;
import com.carlosarroyoam.rest.books.core.constant.AppMessages;
import com.carlosarroyoam.rest.books.core.dto.PagedResponseDto;
import com.carlosarroyoam.rest.books.core.dto.PagedResponseDto.PagedResponseDtoMapper;
import com.carlosarroyoam.rest.books.core.specification.SpecificationBuilder;
import jakarta.persistence.criteria.JoinType;
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
public class BookService {
  private static final Logger log = LoggerFactory.getLogger(BookService.class);
  private final BookRepository bookRepository;

  public BookService(BookRepository bookRepository) {
    this.bookRepository = bookRepository;
  }

  public PagedResponseDto<BookDto> findAll(BookSpecsDto bookSpecs, Pageable pageable) {
    Specification<Book> spec = SpecificationBuilder.<Book>builder()
        .likeIfPresent(root -> root.get(Book_.isbn), bookSpecs.getIsbn())
        .likeIfPresent(root -> root.get(Book_.title), bookSpecs.getTitle())
        .betweenIfPresent(root -> root.get(Book_.price), bookSpecs.getMinPrice(),
            bookSpecs.getMaxPrice())
        .equalsIfPresent(root -> root.get(Book_.isAvailableOnline),
            bookSpecs.getIsAvailableOnline())
        .equalsIfPresent(root -> root.get(Book_.status), bookSpecs.getStatus())
        .inIfPresent(root -> root.join(Book_.authors, JoinType.LEFT).get(Author_.id),
            bookSpecs.getAuthorIds())
        .build();

    Page<Book> books = bookRepository.findAll(spec, pageable);

    return PagedResponseDtoMapper.INSTANCE
        .toPagedResponseDto(books.map(BookDtoMapper.INSTANCE::toDto));
  }

  public BookDto findById(Long bookId) {
    Book bookById = findBookEntityById(bookId);
    return BookDtoMapper.INSTANCE.toDto(bookById);
  }

  @Transactional
  public BookDto create(CreateBookRequestDto requestDto) {
    if (Boolean.TRUE.equals(bookRepository.existsByIsbn(requestDto.getIsbn()))) {
      log.warn(AppMessages.ISBN_ALREADY_EXISTS_EXCEPTION);
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
          AppMessages.ISBN_ALREADY_EXISTS_EXCEPTION);
    }

    LocalDateTime now = LocalDateTime.now();
    Book book = Book.builder()
        .isbn(requestDto.getIsbn())
        .title(requestDto.getTitle())
        .coverUrl(requestDto.getCoverUrl())
        .price(requestDto.getPrice())
        .isAvailableOnline(requestDto.getIsAvailableOnline())
        .status(BookStatus.ACTIVE)
        .publishedAt(requestDto.getPublishedAt())
        .createdAt(now)
        .updatedAt(now)
        .build();

    return BookDtoMapper.INSTANCE.toDto(bookRepository.save(book));
  }

  @Transactional
  public void update(Long bookId, UpdateBookRequestDto requestDto) {
    LocalDateTime now = LocalDateTime.now();
    Book bookById = findBookEntityById(bookId);
    bookById.setIsbn(requestDto.getIsbn());
    bookById.setTitle(requestDto.getTitle());
    bookById.setCoverUrl(requestDto.getCoverUrl());
    bookById.setPrice(requestDto.getPrice());
    bookById.setPublishedAt(requestDto.getPublishedAt());
    bookById.setIsAvailableOnline(requestDto.getIsAvailableOnline());
    bookById.setUpdatedAt(now);
    bookRepository.save(bookById);
  }

  @Transactional
  public void deleteById(Long bookId) {
    LocalDateTime now = LocalDateTime.now();
    Book bookById = findBookEntityById(bookId);
    bookById.setStatus(BookStatus.DELETED);
    bookById.setUpdatedAt(now);
    bookById.setDeletedAt(now);
    bookRepository.save(bookById);
  }

  public List<AuthorDto> findAuthorsByBookId(Long bookId) {
    Book bookById = findBookEntityById(bookId);
    return AuthorDtoMapper.INSTANCE.toDtos(bookById.getAuthors());
  }

  private Book findBookEntityById(Long bookId) {
    return bookRepository.findById(bookId).orElseThrow(() -> {
      log.warn(AppMessages.BOOK_NOT_FOUND_EXCEPTION);
      return new ResponseStatusException(HttpStatus.NOT_FOUND,
          AppMessages.BOOK_NOT_FOUND_EXCEPTION);
    });
  }
}
