package com.carlosarroyoam.rest.books.author;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.carlosarroyoam.rest.books.author.dto.AuthorDto;
import com.carlosarroyoam.rest.books.author.dto.AuthorDto.AuthorDtoMapper;
import com.carlosarroyoam.rest.books.author.dto.AuthorFilterDto;
import com.carlosarroyoam.rest.books.author.dto.CreateAuthorRequestDto;
import com.carlosarroyoam.rest.books.author.dto.UpdateAuthorRequestDto;
import com.carlosarroyoam.rest.books.author.entity.Author;
import com.carlosarroyoam.rest.books.book.dto.BookDto;
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
class AuthorServiceTest {
  @Mock
  private AuthorRepository authorRepository;

  @InjectMocks
  private AuthorService authorService;

  private Author author;
  private Book book;

  @BeforeEach
  void setUp() {
    LocalDateTime now = LocalDateTime.now();

    book = Book.builder()
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

    author = Author.builder()
        .id(1L)
        .name("Yuval Noah Harari")
        .books(List.of(book))
        .createdAt(now)
        .updatedAt(now)
        .build();
  }

  @Test
  @DisplayName("Should return List<AuthorDto> when find all authors")
  void shouldReturnListOfAuthors() {
    List<Author> authors = List.of(author);

    when(authorRepository.findAll(ArgumentMatchers.<Specification<Author>>any(),
        any(Pageable.class))).thenReturn(new PageImpl<>(authors));

    List<AuthorDto> authorsDto = authorService.findAll(PageRequest.of(0, 25),
        AuthorFilterDto.builder().build());

    assertThat(authorsDto).isNotNull().isNotEmpty().hasSize(1);
    assertThat(authorsDto.get(0)).isNotNull();
    assertThat(authorsDto.get(0).getId()).isEqualTo(1L);
    assertThat(authorsDto.get(0).getName()).isEqualTo("Yuval Noah Harari");
    assertThat(authorsDto.get(0).getCreatedAt()).isNotNull();
    assertThat(authorsDto.get(0).getUpdatedAt()).isNotNull();
  }

  @Test
  @DisplayName("Should return AuthorDto when find author by id with existing id")
  void shouldReturnWhenFindAuthorByIdWithExistingId() {
    when(authorRepository.findById(anyLong())).thenReturn(Optional.of(author));

    AuthorDto authorDto = authorService.findById(1L);

    assertThat(authorDto).isNotNull();
    assertThat(authorDto.getId()).isNotNull();
    assertThat(authorDto.getName()).isNotNull();
    assertThat(authorDto.getCreatedAt()).isNotNull();
    assertThat(authorDto.getUpdatedAt()).isNotNull();
  }

  @Test
  @DisplayName("Should throw ResponseStatusException when find an author by id with non existing id")
  void shouldThrowWhenFindAuthorByIdWithNonExistingId() {
    when(authorRepository.findById(anyLong())).thenReturn(Optional.empty());

    assertThatThrownBy(() -> authorService.findById(1L)).isInstanceOf(ResponseStatusException.class)
        .hasMessageContaining(HttpStatus.NOT_FOUND.toString())
        .hasMessageContaining(AppMessages.AUTHOR_NOT_FOUND_EXCEPTION);
  }

  @Test
  @DisplayName("Should return AuthorDto when create an author with valid data")
  void shouldReturnWhenCreateAuthorWithValidData() {
    CreateAuthorRequestDto requestDto = CreateAuthorRequestDto.builder()
        .name("Itzik Yahav")
        .build();

    when(authorRepository.save(any(Author.class)))
        .thenReturn(AuthorDtoMapper.INSTANCE.toEntity(requestDto));

    AuthorDto authorDto = authorService.create(requestDto);

    assertThat(authorDto).isNotNull();
    assertThat(authorDto.getName()).isEqualTo("Itzik Yahav");
  }

  @Test
  @DisplayName("Should update author with valid data")
  void shouldUpdateAuthorWithValidData() {
    UpdateAuthorRequestDto requestDto = UpdateAuthorRequestDto.builder().name("Yuval").build();

    when(authorRepository.findById(anyLong())).thenReturn(Optional.of(author));
    when(authorRepository.save(any(Author.class))).thenReturn(author);

    authorService.update(1L, requestDto);

    verify(authorRepository).save(any(Author.class));
    assertThat(author.getId()).isEqualTo(1L);
    assertThat(author.getName()).isEqualTo("Yuval");
    assertThat(author.getCreatedAt()).isNotNull();
    assertThat(author.getUpdatedAt()).isNotNull();
  }

  @Test
  @DisplayName("Should throw ResponseStatusException when update author with non existing id")
  void shouldUpdateAuthorWithNonExistingId() {
    UpdateAuthorRequestDto requestDto = UpdateAuthorRequestDto.builder().build();

    when(authorRepository.findById(anyLong())).thenReturn(Optional.empty());

    assertThatThrownBy(() -> authorService.update(1L, requestDto))
        .isInstanceOf(ResponseStatusException.class)
        .hasMessageContaining(HttpStatus.NOT_FOUND.toString())
        .hasMessageContaining(AppMessages.AUTHOR_NOT_FOUND_EXCEPTION);
  }

  @Test
  @DisplayName("Should delete author with existing id")
  void shouldDeleteAuthorWithExistingId() {
    when(authorRepository.existsById(anyLong())).thenReturn(true);

    authorService.deleteById(1L);

    verify(authorRepository).deleteById(anyLong());
  }

  @Test
  @DisplayName("Should throw ResponseStatusException when delete author with non existing id")
  void shouldThrowWhenDeleteAuthorWithNonExistingId() {
    when(authorRepository.existsById(anyLong())).thenReturn(false);

    assertThatThrownBy(() -> authorService.deleteById(1L))
        .isInstanceOf(ResponseStatusException.class)
        .hasMessageContaining(HttpStatus.NOT_FOUND.toString())
        .hasMessageContaining(AppMessages.AUTHOR_NOT_FOUND_EXCEPTION);
  }

  @Test
  @DisplayName("Should return List<BookDto> when find books by author id with existing id")
  void shouldReturnWhenFindBooksByAuthorIdWithExistingId() {
    when(authorRepository.findById(anyLong())).thenReturn(Optional.of(author));

    List<BookDto> books = authorService.findBooksByAuthorId(1L);

    assertThat(books).isNotNull().isNotEmpty().hasSize(1);
    assertThat(books.get(0)).isNotNull();
    assertThat(books.get(0).getId()).isEqualTo(1L);
    assertThat(books.get(0).getIsbn()).isEqualTo("978-1-3035-0529-4");
    assertThat(books.get(0).getTitle()).isEqualTo("Homo Deus: A Brief History of Tomorrow");
    assertThat(books.get(0).getCoverUrl())
        .isEqualTo("https://images.isbndb.com/covers/39/36/9781784703936.jpg");
    assertThat(books.get(0).getPrice()).isEqualTo(new BigDecimal("22.99"));
    assertThat(books.get(0).getIsAvailableOnline()).isEqualTo(Boolean.FALSE);
    assertThat(books.get(0).getPublishedAt()).isEqualTo(LocalDate.parse("2017-01-01"));
    assertThat(books.get(0).getCreatedAt()).isNotNull();
    assertThat(books.get(0).getUpdatedAt()).isNotNull();
  }

  @Test
  @DisplayName("Should throw ResponseStatusException when find books by author id with non existing id")
  void shouldThrowWhenFindBooksByAuthorIdWithNonExistingId() {
    when(authorRepository.findById(anyLong())).thenReturn(Optional.empty());

    assertThatThrownBy(() -> authorService.findBooksByAuthorId(1L))
        .isInstanceOf(ResponseStatusException.class)
        .hasMessageContaining(HttpStatus.NOT_FOUND.toString())
        .hasMessageContaining(AppMessages.AUTHOR_NOT_FOUND_EXCEPTION);
  }
}
