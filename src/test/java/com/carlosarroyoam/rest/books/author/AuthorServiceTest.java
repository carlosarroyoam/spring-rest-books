package com.carlosarroyoam.rest.books.author;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.carlosarroyoam.rest.books.author.dto.AuthorResponse;
import com.carlosarroyoam.rest.books.author.dto.AuthorSpecs;
import com.carlosarroyoam.rest.books.author.dto.CreateAuthorRequest;
import com.carlosarroyoam.rest.books.author.dto.UpdateAuthorRequest;
import com.carlosarroyoam.rest.books.author.entity.Author;
import com.carlosarroyoam.rest.books.book.dto.BookResponse;
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

@ExtendWith(MockitoExtension.class)
class AuthorServiceTest {
  @Mock private AuthorRepository authorRepository;

  @InjectMocks private AuthorService authorService;

  private Author author;
  private Book book;

  @BeforeEach
  void setUp() {
    LocalDateTime now = LocalDateTime.now();

    book =
        Book.builder()
            .id(1L)
            .isbn("978-1-3035-0529-4")
            .title("Homo Deus: A Brief History of Tomorrow")
            .coverUrl("https://images.isbndb.com/covers/39/36/9781784703936.jpg")
            .price(new BigDecimal("22.99"))
            .isAvailableOnline(Boolean.FALSE)
            .publishedAt(LocalDate.parse("2017-01-01"))
            .createdAt(now)
            .updatedAt(now)
            .build();

    author =
        Author.builder()
            .id(1L)
            .name("Yuval Noah Harari")
            .bio(
                "Israeli public intellectual, historian and professor in the Department of History at Hebrew University of Jerusalem.")
            .books(List.of(book))
            .createdAt(now)
            .updatedAt(now)
            .build();
  }

  @Test
  @DisplayName("Given authors exist, when find all, then returns paged authors")
  void givenAuthorsExist_whenFindAll_thenReturnsPagedAuthors() {
    Pageable pageable = PageRequest.of(0, 25);
    List<Author> authors = List.of(author);

    when(authorRepository.findAll(
            ArgumentMatchers.<Specification<Author>>any(), any(Pageable.class)))
        .thenReturn(new PageImpl<>(authors, pageable, authors.size()));

    PagedResponse<AuthorResponse> response =
        authorService.findAll(AuthorSpecs.builder().build(), PageRequest.of(0, 25));

    assertThat(response).isNotNull();
    assertThat(response.getItems()).isNotNull().hasSize(1);
    assertThat(response.getPagination()).isNotNull();
    assertThat(response.getPagination().getPage()).isZero();
    assertThat(response.getPagination().getSize()).isEqualTo(25);
    assertThat(response.getPagination().getTotalItems()).isEqualTo(1);
    assertThat(response.getPagination().getTotalPages()).isEqualTo(1);
  }

  @Test
  @DisplayName("Given author exists, when find by id, then returns author")
  void givenAuthorExists_whenFindById_thenReturnsAuthor() {
    when(authorRepository.findById(anyLong())).thenReturn(Optional.of(author));

    AuthorResponse authorResponse = authorService.findById(1L);

    assertThat(authorResponse).isNotNull();
    assertThat(authorResponse.getId()).isEqualTo(1L);
  }

  @Test
  @DisplayName("Given author does not exist, when find by id, then throws not found exception")
  void givenAuthorDoesNotExist_whenFindById_thenThrowsNotFoundException() {
    when(authorRepository.findById(anyLong())).thenReturn(Optional.empty());

    assertThatThrownBy(() -> authorService.findById(1L))
        .isInstanceOf(ResponseStatusException.class)
        .hasMessageContaining(HttpStatus.NOT_FOUND.toString())
        .hasMessageContaining(AppMessages.AUTHOR_NOT_FOUND_EXCEPTION);
  }

  @Test
  @DisplayName("Given valid author data, when create, then returns created author")
  void givenValidAuthorData_whenCreate_thenReturnsCreatedAuthor() {
    CreateAuthorRequest request =
        CreateAuthorRequest.builder()
            .name("Itzik Yahav")
            .bio("Senior software engineer and author specializing in C# and .NET.")
            .build();

    Author savedAuthor = Author.builder().id(2L).name("Itzik Yahav").build();

    when(authorRepository.save(any(Author.class))).thenReturn(savedAuthor);

    AuthorResponse authorResponse = authorService.create(request);

    assertThat(authorResponse).isNotNull();
    assertThat(authorResponse.getId()).isEqualTo(2L);
    assertThat(authorResponse.getName()).isEqualTo("Itzik Yahav");
  }

  @Test
  @DisplayName("Given author exists, when update with valid data, then updates author")
  void givenAuthorExists_whenUpdateWithValidData_thenUpdatesAuthor() {
    UpdateAuthorRequest request =
        UpdateAuthorRequest.builder().name("Yuval Noah Harari").bio("Updated biography").build();

    Author updatedAuthor =
        Author.builder().id(1L).name("Yuval Noah Harari").bio("Updated biography").build();

    when(authorRepository.findById(anyLong())).thenReturn(Optional.of(author));
    when(authorRepository.save(any(Author.class))).thenReturn(updatedAuthor);

    authorService.update(1L, request);

    verify(authorRepository).findById(1L);
    verify(authorRepository).save(any(Author.class));
    assertThat(author.getId()).isEqualTo(1L);
    assertThat(author.getName()).isEqualTo("Yuval Noah Harari");
    assertThat(author.getBio()).isEqualTo("Updated biography");
  }

  @Test
  @DisplayName("Given author does not exist, when update, then throws not found exception")
  void givenAuthorDoesNotExist_whenUpdate_thenThrowsNotFoundException() {
    UpdateAuthorRequest request = UpdateAuthorRequest.builder().build();

    when(authorRepository.findById(anyLong())).thenReturn(Optional.empty());

    assertThatThrownBy(() -> authorService.update(1L, request))
        .isInstanceOf(ResponseStatusException.class)
        .hasMessageContaining(HttpStatus.NOT_FOUND.toString())
        .hasMessageContaining(AppMessages.AUTHOR_NOT_FOUND_EXCEPTION);
  }

  @Test
  @DisplayName("Given author exists, when delete, then deletes author")
  void givenAuthorExists_whenDelete_thenDeletesAuthor() {
    when(authorRepository.findById(anyLong())).thenReturn(Optional.of(author));

    authorService.deleteById(1L);

    verify(authorRepository).save(any(Author.class));
  }

  @Test
  @DisplayName("Given author does not exist, when delete, then throws not found exception")
  void givenAuthorDoesNotExist_whenDelete_thenThrowsNotFoundException() {
    when(authorRepository.findById(anyLong())).thenReturn(Optional.empty());

    assertThatThrownBy(() -> authorService.deleteById(1L))
        .isInstanceOf(ResponseStatusException.class)
        .hasMessageContaining(HttpStatus.NOT_FOUND.toString())
        .hasMessageContaining(AppMessages.AUTHOR_NOT_FOUND_EXCEPTION);
  }

  @Test
  @DisplayName("Given author exists, when find books by author id, then returns books")
  void givenAuthorExists_whenFindBooksByAuthorId_thenReturnsBooks() {
    when(authorRepository.findById(anyLong())).thenReturn(Optional.of(author));

    List<BookResponse> books = authorService.findBooksByAuthorId(1L);

    assertThat(books)
        .hasSize(1)
        .first()
        .satisfies(
            actualBook -> {
              assertThat(actualBook.getId()).isEqualTo(1L);
              assertThat(actualBook.getIsbn()).isEqualTo("978-1-3035-0529-4");
              assertThat(actualBook.getTitle()).isEqualTo("Homo Deus: A Brief History of Tomorrow");
              assertThat(actualBook.getPrice()).isEqualTo(new BigDecimal("22.99"));
            });
  }

  @Test
  @DisplayName(
      "Given author does not exist, when find books by author id, then throws not found exception")
  void givenAuthorDoesNotExist_whenFindBooksByAuthorId_thenThrowsNotFoundException() {
    when(authorRepository.findById(anyLong())).thenReturn(Optional.empty());

    assertThatThrownBy(() -> authorService.findBooksByAuthorId(1L))
        .isInstanceOf(ResponseStatusException.class)
        .hasMessageContaining(HttpStatus.NOT_FOUND.toString())
        .hasMessageContaining(AppMessages.AUTHOR_NOT_FOUND_EXCEPTION);
  }
}
