package com.carlosarroyoam.rest.books.book;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.carlosarroyoam.rest.books.author.dto.AuthorDto;
import com.carlosarroyoam.rest.books.author.entity.Author;
import com.carlosarroyoam.rest.books.book.dto.BookDto;
import com.carlosarroyoam.rest.books.book.dto.BookFilterDto;
import com.carlosarroyoam.rest.books.book.dto.CreateBookRequestDto;
import com.carlosarroyoam.rest.books.book.dto.UpdateBookRequestDto;
import com.carlosarroyoam.rest.books.book.entity.Book;
import com.carlosarroyoam.rest.books.core.constant.AppMessages;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
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

  @Test
  @DisplayName("Should return List<BookDto> when find all books")
  void shouldReturnListOfBooks() {
    List<Book> books = List.of(Book.builder().build(), Book.builder().build());

    when(bookRepository.findAll(ArgumentMatchers.<Specification<Book>>any(), any(Pageable.class)))
        .thenReturn(new PageImpl<>(books));

    List<BookDto> booksDto = bookService.findAll(PageRequest.of(0, 25),
        BookFilterDto.builder().build());

    assertThat(booksDto).isNotNull().isNotEmpty().hasSize(2);
  }

  @Test
  @DisplayName("Should return BookDto when find book by id with existing id")
  void shouldReturnWhenFindBookByIdWithExisitingId() {
    Book book = Book.builder().id(1L).build();

    when(bookRepository.findById(any())).thenReturn(Optional.of(book));

    BookDto bookDto = bookService.findById(1L);

    assertThat(bookDto).isNotNull();
    assertThat(bookDto.getId()).isEqualTo(1L);
  }

  @Test
  @DisplayName("Should throw ResponseStatusException when find a book by id with non existing id")
  void shouldThrowWhenFindBookByIdWithNonExisitingId() {
    when(bookRepository.findById(any())).thenReturn(Optional.empty());

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
        .build();

    Book book = Book.builder()
        .isbn("978-9-7389-4434-3")
        .title("Sapiens: A Brief History of Humankind")
        .build();

    when(bookRepository.existsByIsbn(any())).thenReturn(false);
    when(bookRepository.save(any(Book.class))).thenReturn(book);

    BookDto bookDto = bookService.create(requestDto);

    assertThat(bookDto).isNotNull();
    assertThat(bookDto.getIsbn()).isEqualTo("978-9-7389-4434-3");
    assertThat(bookDto.getTitle()).isEqualTo("Sapiens: A Brief History of Humankind");
  }

  @Test
  @DisplayName("Should thow ResponseStatusException when create a book with existing ISBN")
  void shouldThrowWhenCreateBookWithExistingIsbn() {
    CreateBookRequestDto requestDto = CreateBookRequestDto.builder().build();

    when(bookRepository.existsByIsbn(any())).thenReturn(true);

    assertThatThrownBy(() -> bookService.create(requestDto))
        .isInstanceOf(ResponseStatusException.class)
        .hasMessageContaining(HttpStatus.BAD_REQUEST.toString())
        .hasMessageContaining(AppMessages.ISBN_ALREADY_EXISTS_EXCEPTION);
  }

  @Test
  @DisplayName("Should update book with valid data")
  void shouldUpdateBookWithValidData() {
    UpdateBookRequestDto requestDto = UpdateBookRequestDto.builder()
        .title("Sapiens: A Brief History of Humankind")
        .price(new BigDecimal("20.99"))
        .isAvailableOnline(true)
        .build();

    Book book = Book.builder()
        .id(1L)
        .title("Sapiens")
        .price(new BigDecimal("20.79"))
        .isAvailableOnline(false)
        .build();

    when(bookRepository.findById(any())).thenReturn(Optional.of(book));
    when(bookRepository.save(any(Book.class))).thenReturn(book);

    bookService.update(1L, requestDto);

    verify(bookRepository).save(book);
    assertThat(book.getTitle()).isEqualTo("Sapiens: A Brief History of Humankind");
    assertThat(book.getPrice()).isEqualTo(new BigDecimal("20.99"));
    assertThat(book.getIsAvailableOnline()).isTrue();
  }

  @Test
  @DisplayName("Should throw ResponseStatusException when update book with non existing id")
  void shouldThrowWhenUpdateBookWithInvalidData() {
    UpdateBookRequestDto requestDto = UpdateBookRequestDto.builder().build();

    when(bookRepository.findById(any())).thenReturn(Optional.empty());

    assertThatThrownBy(() -> bookService.update(1L, requestDto))
        .isInstanceOf(ResponseStatusException.class)
        .hasMessageContaining(HttpStatus.NOT_FOUND.toString())
        .hasMessageContaining(AppMessages.BOOK_NOT_FOUND_EXCEPTION);
  }

  @Test
  @DisplayName("Should delete book with existing id")
  void shouldDeleteBookWithExistingId() {
    when(bookRepository.existsById(any())).thenReturn(true);

    bookService.deleteById(1L);

    verify(bookRepository).deleteById(1L);
  }

  @Test
  @DisplayName("Should throw ResponseStatusException when delete book with non existing id")
  void shouldThrowWhenDeleteBookWithNonExistingId() {
    when(bookRepository.existsById(any())).thenReturn(false);

    assertThatThrownBy(() -> bookService.deleteById(1L)).isInstanceOf(ResponseStatusException.class)
        .hasMessageContaining(HttpStatus.NOT_FOUND.toString())
        .hasMessageContaining(AppMessages.BOOK_NOT_FOUND_EXCEPTION);
  }

  @Test
  @DisplayName("Should return List<AuthorDto> when find authors by book id with existing id")
  void shouldReturnWhenFindAuthorsByBookIdWithExistingId() {
    Book book = Book.builder()
        .id(1L)
        .authors(List.of(Author.builder().build(), Author.builder().build()))
        .build();

    when(bookRepository.findById(any())).thenReturn(Optional.of(book));

    List<AuthorDto> authors = bookService.findAuthorsByBookId(1L);

    assertThat(authors).isNotNull().isNotEmpty().hasSize(2);
  }

  @Test
  @DisplayName("Should throw ResponseStatusException when find authors by book id with non existing id")
  void shouldThrowWhenFindAuthorsByBookIdWithNonExistingId() {
    when(bookRepository.findById(any())).thenReturn(Optional.empty());

    assertThatThrownBy(() -> bookService.findAuthorsByBookId(1L))
        .isInstanceOf(ResponseStatusException.class)
        .hasMessageContaining(HttpStatus.NOT_FOUND.toString())
        .hasMessageContaining(AppMessages.BOOK_NOT_FOUND_EXCEPTION);
  }
}
