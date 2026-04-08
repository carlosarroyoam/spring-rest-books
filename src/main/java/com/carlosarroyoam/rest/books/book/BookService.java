package com.carlosarroyoam.rest.books.book;

import com.carlosarroyoam.rest.books.author.dto.AuthorResponse;
import com.carlosarroyoam.rest.books.author.dto.AuthorResponse.AuthorResponseMapper;
import com.carlosarroyoam.rest.books.author.entity.Author_;
import com.carlosarroyoam.rest.books.book.dto.BookResponse;
import com.carlosarroyoam.rest.books.book.dto.BookResponse.BookResponseMapper;
import com.carlosarroyoam.rest.books.book.dto.BookSpecs;
import com.carlosarroyoam.rest.books.book.dto.CreateBookRequest;
import com.carlosarroyoam.rest.books.book.dto.UpdateBookRequest;
import com.carlosarroyoam.rest.books.book.entity.Book;
import com.carlosarroyoam.rest.books.book.entity.BookStatus;
import com.carlosarroyoam.rest.books.book.entity.Book_;
import com.carlosarroyoam.rest.books.core.constant.AppMessages;
import com.carlosarroyoam.rest.books.core.dto.PagedResponse;
import com.carlosarroyoam.rest.books.core.dto.PagedResponse.PagedResponseMapper;
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

  public PagedResponse<BookResponse> findAll(BookSpecs bookSpecs, Pageable pageable) {
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

    return PagedResponseMapper.INSTANCE
        .toPagedResponse(books.map(BookResponseMapper.INSTANCE::toDto));
  }

  public BookResponse findById(Long bookId) {
    Book bookById = findBookEntityById(bookId);
    return BookResponseMapper.INSTANCE.toDto(bookById);
  }

  @Transactional
  public BookResponse create(CreateBookRequest request) {
    if (Boolean.TRUE.equals(bookRepository.existsByIsbn(request.getIsbn()))) {
      log.warn(AppMessages.ISBN_ALREADY_EXISTS_EXCEPTION);
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
          AppMessages.ISBN_ALREADY_EXISTS_EXCEPTION);
    }

    LocalDateTime now = LocalDateTime.now();
    Book book = Book.builder()
        .isbn(request.getIsbn())
        .title(request.getTitle())
        .coverUrl(request.getCoverUrl())
        .price(request.getPrice())
        .isAvailableOnline(request.getIsAvailableOnline())
        .status(BookStatus.ACTIVE)
        .publishedAt(request.getPublishedAt())
        .createdAt(now)
        .updatedAt(now)
        .build();

    return BookResponseMapper.INSTANCE.toDto(bookRepository.save(book));
  }

  @Transactional
  public void update(Long bookId, UpdateBookRequest request) {
    LocalDateTime now = LocalDateTime.now();
    Book bookById = findBookEntityById(bookId);
    bookById.setIsbn(request.getIsbn());
    bookById.setTitle(request.getTitle());
    bookById.setCoverUrl(request.getCoverUrl());
    bookById.setPrice(request.getPrice());
    bookById.setPublishedAt(request.getPublishedAt());
    bookById.setIsAvailableOnline(request.getIsAvailableOnline());
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

  public List<AuthorResponse> findAuthorsByBookId(Long bookId) {
    Book bookById = findBookEntityById(bookId);
    return AuthorResponseMapper.INSTANCE.toDtos(bookById.getAuthors());
  }

  private Book findBookEntityById(Long bookId) {
    return bookRepository.findById(bookId).orElseThrow(() -> {
      log.warn(AppMessages.BOOK_NOT_FOUND_EXCEPTION);
      return new ResponseStatusException(HttpStatus.NOT_FOUND,
          AppMessages.BOOK_NOT_FOUND_EXCEPTION);
    });
  }
}
