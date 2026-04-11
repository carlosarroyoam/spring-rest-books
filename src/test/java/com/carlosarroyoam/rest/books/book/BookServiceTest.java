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
  @DisplayName("Given books exist, when find all, then returns paged books")
  void givenBooksExist_whenFindAll_thenReturnsPagedBooks() {
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
  @DisplayName("Given book exists, when find by id, then returns book")
  void givenBookExists_whenFindById_thenReturnsBook() {
    when(bookRepository.findById(anyLong())).thenReturn(Optional.of(book));

    BookResponse bookResponse = bookService.findById(1L);

    assertThat(bookResponse).isNotNull();
    assertThat(bookResponse.getId()).isEqualTo(1L);
  }

  @Test
  @DisplayName("Given book does not exist, when find by id, then throws not found exception")
  void givenBookDoesNotExist_whenFindById_thenThrowsNotFoundException() {
    when(bookRepository.findById(anyLong())).thenReturn(Optional.empty());

    assertThatThrownBy(() -> bookService.findById(1L)).isInstanceOf(ResponseStatusException.class)
        .hasMessageContaining(HttpStatus.NOT_FOUND.toString())
        .hasMessageContaining(AppMessages.BOOK_NOT_FOUND_EXCEPTION);
  }

  @Test
  @DisplayName("Given valid book data, when create, then returns created book")
  void givenValidBookData_whenCreate_thenReturnsCreatedBook() {
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
  @DisplayName("Given book with existing isbn, when create, then throws bad request exception")
  void givenBookWithExistingIsbn_whenCreate_thenThrowsBadRequestException() {
    CreateBookRequest request = CreateBookRequest.builder().isbn("978-1-3035-0529-4").build();

    when(bookRepository.existsByIsbn(anyString())).thenReturn(true);

    assertThatThrownBy(() -> bookService.create(request))
        .isInstanceOf(ResponseStatusException.class)
        .hasMessageContaining(HttpStatus.BAD_REQUEST.toString())
        .hasMessageContaining(AppMessages.ISBN_ALREADY_EXISTS_EXCEPTION);
  }

  @Test
  @DisplayName("Given book exists, when update with valid data, then updates book")
  void givenBookExists_whenUpdateWithValidData_thenUpdatesBook() {
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
  @DisplayName("Given book does not exist, when update, then throws not found exception")
  void givenBookDoesNotExist_whenUpdate_thenThrowsNotFoundException() {
    UpdateBookRequest request = UpdateBookRequest.builder().build();

    when(bookRepository.findById(anyLong())).thenReturn(Optional.empty());

    assertThatThrownBy(() -> bookService.update(1L, request))
        .isInstanceOf(ResponseStatusException.class)
        .hasMessageContaining(HttpStatus.NOT_FOUND.toString())
        .hasMessageContaining(AppMessages.BOOK_NOT_FOUND_EXCEPTION);
  }

  @Test
  @DisplayName("Given book exists, when delete, then deletes book")
  void givenBookExists_whenDelete_thenDeletesBook() {
    when(bookRepository.findById(anyLong())).thenReturn(Optional.of(book));

    bookService.deleteById(1L);

    verify(bookRepository).save(any(Book.class));
  }

  @Test
  @DisplayName("Given book does not exist, when delete, then throws not found exception")
  void givenBookDoesNotExist_whenDelete_thenThrowsNotFoundException() {
    when(bookRepository.findById(anyLong())).thenReturn(Optional.empty());

    assertThatThrownBy(() -> bookService.deleteById(1L)).isInstanceOf(ResponseStatusException.class)
        .hasMessageContaining(HttpStatus.NOT_FOUND.toString())
        .hasMessageContaining(AppMessages.BOOK_NOT_FOUND_EXCEPTION);
  }

  @Test
  @DisplayName("Given book exists, when find authors by book id, then returns authors")
  void givenBookExists_whenFindAuthorsByBookId_thenReturnsAuthors() {
    when(bookRepository.findById(anyLong())).thenReturn(Optional.of(book));

    List<AuthorResponse> authors = bookService.findAuthorsByBookId(1L);

    assertThat(authors).hasSize(1).first().satisfies(actualAuthor -> {
      assertThat(actualAuthor.getId()).isEqualTo(1L);
      assertThat(actualAuthor.getName()).isEqualTo("Yuval Noah Harari");
    });
  }

  @Test
  @DisplayName("Given book does not exist, when find authors by book id, then throws not found exception")
  void givenBookDoesNotExist_whenFindAuthorsByBookId_thenThrowsNotFoundException() {
    when(bookRepository.findById(anyLong())).thenReturn(Optional.empty());

    assertThatThrownBy(() -> bookService.findAuthorsByBookId(1L))
        .isInstanceOf(ResponseStatusException.class)
        .hasMessageContaining(HttpStatus.NOT_FOUND.toString())
        .hasMessageContaining(AppMessages.BOOK_NOT_FOUND_EXCEPTION);
  }
}
