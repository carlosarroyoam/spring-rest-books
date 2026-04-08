package com.carlosarroyoam.rest.books.book;

import com.carlosarroyoam.rest.books.author.dto.AuthorResponse;
import com.carlosarroyoam.rest.books.author.entity.Author;
import com.carlosarroyoam.rest.books.book.dto.BookResponse;
import com.carlosarroyoam.rest.books.book.dto.BookSpecs;
import com.carlosarroyoam.rest.books.book.dto.CreateBookRequest;
import com.carlosarroyoam.rest.books.book.dto.UpdateBookRequest;
import com.carlosarroyoam.rest.books.book.entity.Book;
import com.carlosarroyoam.rest.books.core.constant.AppMessages;
import com.carlosarroyoam.rest.books.core.dto.PagedResponse;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookServiceTest {
  @Mock
  private BookRepository bookRepository;

  @InjectMocks
  private BookService bookService;

  private Book book;
  private Author author;

  @BeforeEach
  void setUp() {
    LocalDateTime now = LocalDateTime.now();

    author = Author.builder()
        .id(1L)
        .name("Yuval Noah Harari")
        .createdAt(now)
        .updatedAt(now)
        .build();

    book = Book.builder()
        .id(1L)
        .isbn("978-1-3035-0529-4")
        .title("Homo Deus: A Brief History of Tomorrow")
        .coverUrl("https://images.isbndb.com/covers/39/36/9781784703936.jpg")
        .price(new BigDecimal("22.99"))
        .isAvailableOnline(Boolean.FALSE)
        .publishedAt(LocalDate.parse("2017-01-01"))
        .authors(List.of(author))
        .createdAt(now)
        .updatedAt(now)
        .build();
  }

  @Test
  @DisplayName("Should return PagedResponse<BookResponse> when find all books")
  void shouldReturnListOfBooks() {
    Pageable pageable = PageRequest.of(0, 25);
    List<Book> books = List.of(book);

    when(bookRepository.findAll(ArgumentMatchers.<Specification<Book>>any(), any(Pageable.class)))
        .thenReturn(new PageImpl<>(books, pageable, books.size()));

    PagedResponse<BookResponse> response = bookService.findAll(BookSpecs.builder().build(),
        PageRequest.of(0, 25));

    assertThat(response).isNotNull();
    assertThat(response.getItems()).isNotNull().hasSize(1);
    assertThat(response.getPagination()).isNotNull();
    assertThat(response.getPagination().getPage()).isZero();
    assertThat(response.getPagination().getSize()).isEqualTo(25);
    assertThat(response.getPagination().getTotalItems()).isEqualTo(1);
    assertThat(response.getPagination().getTotalPages()).isEqualTo(1);
  }

  @Test
  @DisplayName("Should return BookResponse when find book by id with existing id")
  void shouldReturnWhenFindBookByIdWithExisitingId() {
    when(bookRepository.findById(anyLong())).thenReturn(Optional.of(book));

    BookResponse bookResponse = bookService.findById(1L);

    assertThat(bookResponse).isNotNull();
    assertThat(bookResponse.getId()).isEqualTo(1L);
  }

  @Test
  @DisplayName("Should throw ResponseStatusException when find a book by id with non existing id")
  void shouldThrowWhenFindBookByIdWithNonExisitingId() {
    when(bookRepository.findById(anyLong())).thenReturn(Optional.empty());

    assertThatThrownBy(() -> bookService.findById(1L)).isInstanceOf(ResponseStatusException.class)
        .hasMessageContaining(HttpStatus.NOT_FOUND.toString())
        .hasMessageContaining(AppMessages.BOOK_NOT_FOUND_EXCEPTION);
  }

  @Test
  @DisplayName("Should return BookResponse when create a book with valid data")
  void shouldReturnWhenCreateBookWithValidData() {
    CreateBookRequest request = CreateBookRequest.builder()
        .isbn("978-9-7389-4434-3")
        .title("Sapiens: A Brief History of Humankind")
        .build();

    Book savedBook = Book.builder()
        .id(2L)
        .isbn("978-9-7389-4434-3")
        .title("Sapiens: A Brief History of Humankind")
        .build();

    when(bookRepository.existsByIsbn(anyString())).thenReturn(false);
    when(bookRepository.save(any(Book.class))).thenReturn(savedBook);

    BookResponse bookResponse = bookService.create(request);

    assertThat(bookResponse).isNotNull();
    assertThat(bookResponse.getIsbn()).isEqualTo("978-9-7389-4434-3");
    assertThat(bookResponse.getTitle()).isEqualTo("Sapiens: A Brief History of Humankind");
  }

  @Test
  @DisplayName("Should thow ResponseStatusException when create a book with existing ISBN")
  void shouldThrowWhenCreateBookWithExistingIsbn() {
    CreateBookRequest request = CreateBookRequest.builder().isbn("978-1-3035-0529-4").build();

    when(bookRepository.existsByIsbn(anyString())).thenReturn(true);

    assertThatThrownBy(() -> bookService.create(request))
        .isInstanceOf(ResponseStatusException.class)
        .hasMessageContaining(HttpStatus.BAD_REQUEST.toString())
        .hasMessageContaining(AppMessages.ISBN_ALREADY_EXISTS_EXCEPTION);
  }

  @Test
  @DisplayName("Should update book with valid data")
  void shouldUpdateBookWithValidData() {
    UpdateBookRequest request = UpdateBookRequest.builder()
        .isbn("978-1-3035-0293-1")
        .title("Homo Deus")
        .build();

    Book updatedBook = Book.builder().isbn("978-1-3035-0293-1").title("Homo Deus").build();

    when(bookRepository.findById(anyLong())).thenReturn(Optional.of(book));
    when(bookRepository.save(any(Book.class))).thenReturn(updatedBook);

    bookService.update(1L, request);

    verify(bookRepository).findById(1L);
    verify(bookRepository).save(any(Book.class));
    assertThat(book.getId()).isEqualTo(1L);
    assertThat(book.getIsbn()).isEqualTo("978-1-3035-0293-1");
    assertThat(book.getTitle()).isEqualTo("Homo Deus");
  }

  @Test
  @DisplayName("Should throw ResponseStatusException when update book with non existing id")
  void shouldThrowWhenUpdateBookWithInvalidData() {
    UpdateBookRequest request = UpdateBookRequest.builder().build();

    when(bookRepository.findById(anyLong())).thenReturn(Optional.empty());

    assertThatThrownBy(() -> bookService.update(1L, request))
        .isInstanceOf(ResponseStatusException.class)
        .hasMessageContaining(HttpStatus.NOT_FOUND.toString())
        .hasMessageContaining(AppMessages.BOOK_NOT_FOUND_EXCEPTION);
  }

  @Test
  @DisplayName("Should delete book with existing id")
  void shouldDeleteBookWithExistingId() {
    when(bookRepository.findById(anyLong())).thenReturn(Optional.of(book));

    bookService.deleteById(1L);

    verify(bookRepository).save(any(Book.class));
  }

  @Test
  @DisplayName("Should throw ResponseStatusException when delete book with non existing id")
  void shouldThrowWhenDeleteBookWithNonExistingId() {
    when(bookRepository.findById(anyLong())).thenReturn(Optional.empty());

    assertThatThrownBy(() -> bookService.deleteById(1L)).isInstanceOf(ResponseStatusException.class)
        .hasMessageContaining(HttpStatus.NOT_FOUND.toString())
        .hasMessageContaining(AppMessages.BOOK_NOT_FOUND_EXCEPTION);
  }

  @Test
  @DisplayName("Should return List<AuthorResponse> when find authors by book id with existing id")
  void shouldReturnWhenFindAuthorsByBookIdWithExistingId() {
    when(bookRepository.findById(anyLong())).thenReturn(Optional.of(book));

    List<AuthorResponse> authors = bookService.findAuthorsByBookId(1L);

    assertThat(authors).hasSize(1).first().satisfies(actualAuthor -> {
      assertThat(actualAuthor.getId()).isEqualTo(1L);
      assertThat(actualAuthor.getName()).isEqualTo("Yuval Noah Harari");
    });
  }

  @Test
  @DisplayName("Should throw ResponseStatusException when find authors by book id with non existing id")
  void shouldThrowWhenFindAuthorsByBookIdWithNonExistingId() {
    when(bookRepository.findById(anyLong())).thenReturn(Optional.empty());

    assertThatThrownBy(() -> bookService.findAuthorsByBookId(1L))
        .isInstanceOf(ResponseStatusException.class)
        .hasMessageContaining(HttpStatus.NOT_FOUND.toString())
        .hasMessageContaining(AppMessages.BOOK_NOT_FOUND_EXCEPTION);
  }
}
