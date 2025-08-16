package com.carlosarroyoam.rest.books.book;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.carlosarroyoam.rest.books.author.dto.AuthorDto;
import com.carlosarroyoam.rest.books.author.entity.Author;
import com.carlosarroyoam.rest.books.book.dto.BookDto;
import com.carlosarroyoam.rest.books.book.dto.BookDto.BookDtoMapper;
import com.carlosarroyoam.rest.books.book.dto.BookFilterDto;
import com.carlosarroyoam.rest.books.book.dto.CreateBookRequestDto;
import com.carlosarroyoam.rest.books.book.dto.UpdateBookRequestDto;
import com.carlosarroyoam.rest.books.book.entity.Book;
import com.carlosarroyoam.rest.books.core.constant.AppMessages;
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
  @DisplayName("Should return List<BookDto> when find all books")
  void shouldReturnListOfBooks() {
    when(bookRepository.findAll(ArgumentMatchers.<Specification<Book>>any(), any(Pageable.class)))
        .thenReturn(new PageImpl<>(List.of(book)));

    List<BookDto> booksDto = bookService.findAll(PageRequest.of(0, 25),
        BookFilterDto.builder().build());

    assertThat(booksDto).isNotNull().isNotEmpty().hasSize(1);
    assertThat(booksDto.get(0)).isNotNull();
    assertThat(booksDto.get(0).getId()).isEqualTo(1L);
    assertThat(booksDto.get(0).getIsbn()).isEqualTo("978-1-3035-0529-4");
    assertThat(booksDto.get(0).getTitle()).isEqualTo("Homo Deus: A Brief History of Tomorrow");
    assertThat(booksDto.get(0).getCoverUrl())
        .isEqualTo("https://images.isbndb.com/covers/39/36/9781784703936.jpg");
    assertThat(booksDto.get(0).getPrice()).isEqualTo(new BigDecimal("22.99"));
    assertThat(booksDto.get(0).getIsAvailableOnline()).isEqualTo(Boolean.FALSE);
    assertThat(booksDto.get(0).getPublishedAt()).isEqualTo(LocalDate.parse("2017-01-01"));
    assertThat(booksDto.get(0).getCreatedAt()).isNotNull();
    assertThat(booksDto.get(0).getUpdatedAt()).isNotNull();
  }

  @Test
  @DisplayName("Should return BookDto when find book by id with existing id")
  void shouldReturnWhenFindBookByIdWithExisitingId() {
    when(bookRepository.findById(anyLong())).thenReturn(Optional.of(book));

    BookDto bookDto = bookService.findById(1L);

    assertThat(bookDto).isNotNull();
    assertThat(bookDto.getId()).isEqualTo(1L);
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
  @DisplayName("Should return BookDto when create a book with valid data")
  void shouldReturnWhenCreateBookWithValidData() {
    CreateBookRequestDto requestDto = CreateBookRequestDto.builder()
        .isbn("978-9-7389-4434-3")
        .title("Sapiens: A Brief History of Humankind")
        .coverUrl("https://images.isbndb.com/covers/60/97/9780062316097.jpg")
        .price(new BigDecimal("20.79"))
        .isAvailableOnline(Boolean.FALSE)
        .publishedAt(LocalDate.parse("2022-12-01"))
        .build();

    when(bookRepository.existsByIsbn(anyString())).thenReturn(false);
    when(bookRepository.save(any(Book.class)))
        .thenReturn(BookDtoMapper.INSTANCE.createRequestToEntity(requestDto));

    BookDto bookDto = bookService.create(requestDto);

    assertThat(bookDto).isNotNull();
    assertThat(bookDto.getIsbn()).isEqualTo("978-9-7389-4434-3");
    assertThat(bookDto.getTitle()).isEqualTo("Sapiens: A Brief History of Humankind");
    assertThat(bookDto.getCoverUrl())
        .isEqualTo("https://images.isbndb.com/covers/60/97/9780062316097.jpg");
    assertThat(bookDto.getPrice()).isEqualTo(new BigDecimal("20.79"));
    assertThat(bookDto.getIsAvailableOnline()).isEqualTo(Boolean.FALSE);
    assertThat(bookDto.getPublishedAt()).isEqualTo(LocalDate.parse("2022-12-01"));
  }

  @Test
  @DisplayName("Should thow ResponseStatusException when create a book with existing ISBN")
  void shouldThrowWhenCreateBookWithExistingIsbn() {
    CreateBookRequestDto requestDto = CreateBookRequestDto.builder()
        .isbn("978-1-3035-0529-4")
        .build();

    when(bookRepository.existsByIsbn(anyString())).thenReturn(true);

    assertThatThrownBy(() -> bookService.create(requestDto))
        .isInstanceOf(ResponseStatusException.class)
        .hasMessageContaining(HttpStatus.BAD_REQUEST.toString())
        .hasMessageContaining(AppMessages.ISBN_ALREADY_EXISTS_EXCEPTION);
  }

  @Test
  @DisplayName("Should update book with valid data")
  void shouldUpdateBookWithValidData() {
    UpdateBookRequestDto requestDto = UpdateBookRequestDto.builder()
        .isbn("978-1-3035-0293-1")
        .title("Homo Deus")
        .coverUrl("https://images.isbndb.com/covers/10293421502.jpg")
        .price(new BigDecimal("17.99"))
        .isAvailableOnline(Boolean.FALSE)
        .publishedAt(LocalDate.parse("2015-01-01"))
        .build();

    when(bookRepository.findById(anyLong())).thenReturn(Optional.of(book));
    when(bookRepository.save(any(Book.class))).thenReturn(book);

    bookService.update(1L, requestDto);

    verify(bookRepository).save(any(Book.class));
    assertThat(book.getId()).isEqualTo(1L);
    assertThat(book.getIsbn()).isEqualTo("978-1-3035-0293-1");
    assertThat(book.getTitle()).isEqualTo("Homo Deus");
    assertThat(book.getCoverUrl()).isEqualTo("https://images.isbndb.com/covers/10293421502.jpg");
    assertThat(book.getPrice()).isEqualTo(new BigDecimal("17.99"));
    assertThat(book.getIsAvailableOnline()).isFalse();
    assertThat(book.getPublishedAt()).isEqualTo(LocalDate.parse("2015-01-01"));
    assertThat(book.getCreatedAt()).isNotNull();
    assertThat(book.getUpdatedAt()).isNotNull();
  }

  @Test
  @DisplayName("Should throw ResponseStatusException when update book with non existing id")
  void shouldThrowWhenUpdateBookWithInvalidData() {
    UpdateBookRequestDto requestDto = UpdateBookRequestDto.builder().build();

    when(bookRepository.findById(anyLong())).thenReturn(Optional.empty());

    assertThatThrownBy(() -> bookService.update(1L, requestDto))
        .isInstanceOf(ResponseStatusException.class)
        .hasMessageContaining(HttpStatus.NOT_FOUND.toString())
        .hasMessageContaining(AppMessages.BOOK_NOT_FOUND_EXCEPTION);
  }

  @Test
  @DisplayName("Should delete book with existing id")
  void shouldDeleteBookWithExistingId() {
    when(bookRepository.existsById(anyLong())).thenReturn(true);

    bookService.deleteById(1L);

    verify(bookRepository).deleteById(anyLong());
  }

  @Test
  @DisplayName("Should throw ResponseStatusException when delete book with non existing id")
  void shouldThrowWhenDeleteBookWithNonExistingId() {
    when(bookRepository.existsById(anyLong())).thenReturn(false);

    assertThatThrownBy(() -> bookService.deleteById(1L)).isInstanceOf(ResponseStatusException.class)
        .hasMessageContaining(HttpStatus.NOT_FOUND.toString())
        .hasMessageContaining(AppMessages.BOOK_NOT_FOUND_EXCEPTION);
  }

  @Test
  @DisplayName("Should return List<AuthorDto> when find authors by book id with existing id")
  void shouldReturnWhenFindAuthorsByBookIdWithExistingId() {
    when(bookRepository.findById(anyLong())).thenReturn(Optional.of(book));

    List<AuthorDto> authorsDto = bookService.findAuthorsByBookId(1L);

    assertThat(authorsDto).isNotNull().isNotEmpty().hasSize(1);
    assertThat(authorsDto.get(0)).isNotNull();
    assertThat(authorsDto.get(0).getId()).isEqualTo(1L);
    assertThat(authorsDto.get(0).getName()).isEqualTo("Yuval Noah Harari");
    assertThat(authorsDto.get(0).getCreatedAt()).isNotNull();
    assertThat(authorsDto.get(0).getUpdatedAt()).isNotNull();
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
