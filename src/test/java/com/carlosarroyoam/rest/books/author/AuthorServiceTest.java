package com.carlosarroyoam.rest.books.author;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;

import com.carlosarroyoam.rest.books.author.dto.AuthorDto;
import com.carlosarroyoam.rest.books.author.dto.CreateAuthorRequestDto;
import com.carlosarroyoam.rest.books.author.dto.UpdateAuthorRequestDto;
import com.carlosarroyoam.rest.books.author.entity.Author;
import com.carlosarroyoam.rest.books.book.dto.BookDto;
import com.carlosarroyoam.rest.books.book.entity.Book;
import com.carlosarroyoam.rest.books.core.constant.AppMessages;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class AuthorServiceTest {
  @Mock
  private AuthorRepository authorRepository;

  @InjectMocks
  private AuthorService authorService;

  @Test
  @DisplayName("Should return List<AuthorDto> when find all authors")
  void shouldReturnListOfAuthors() {
    List<Author> authors = List.of(Author.builder().build(), Author.builder().build());

    Mockito.when(authorRepository.findAll(any(Pageable.class))).thenReturn(new PageImpl<>(authors));

    List<AuthorDto> authorsDto = authorService.findAll(PageRequest.of(0, 25));

    assertThat(authorsDto).isNotNull().isNotEmpty().size().isEqualTo(2);
  }

  @Test
  @DisplayName("Should return AuthorDto when find author by id with existing id")
  void shouldReturnWhenFindAuthorByIdWithExistingId() {
    Author author = Author.builder().id(1L).build();

    Mockito.when(authorRepository.findById(any())).thenReturn(Optional.of(author));

    AuthorDto authorDto = authorService.findById(1L);

    assertThat(authorDto).isNotNull();
    assertThat(authorDto.getId()).isEqualTo(1L);
  }

  @Test
  @DisplayName("Should throw ResponseStatusException when find an author by id with non existing id")
  void shouldThrowWhenFindAuthorByIdWithNonExistingId() {
    Mockito.when(authorRepository.findById(any())).thenReturn(Optional.empty());

    assertThatThrownBy(() -> authorService.findById(1L)).isInstanceOf(ResponseStatusException.class)
        .hasMessageContaining(HttpStatus.NOT_FOUND.toString())
        .hasMessageContaining(AppMessages.AUTHOR_NOT_FOUND_EXCEPTION);
  }

  @Test
  @DisplayName("Should return AuthorDto when create an author with valid data")
  void shouldReturnWhenCreateAuthorWithValidData() {
    CreateAuthorRequestDto requestDto = CreateAuthorRequestDto.builder()
        .name("Yuval Noah Harari")
        .build();

    Author author = Author.builder().name("Yuval Noah Harari").build();

    Mockito.when(authorRepository.save(any(Author.class))).thenReturn(author);

    AuthorDto authorDto = authorService.create(requestDto);

    assertThat(authorDto).isNotNull();
    assertThat(authorDto.getName()).isEqualTo("Yuval Noah Harari");
  }

  @Test
  @DisplayName("Should update author with valid data")
  void shouldUpdateAuthorWithValidData() {
    UpdateAuthorRequestDto requestDto = UpdateAuthorRequestDto.builder()
        .name("Yuval Noah Harari")
        .build();

    Author author = Author.builder().id(1L).name("Yuval Harari").build();

    Mockito.when(authorRepository.findById(any())).thenReturn(Optional.of(author));
    Mockito.when(authorRepository.save(any(Author.class))).thenReturn(author);

    authorService.update(1L, requestDto);

    Mockito.verify(authorRepository).save(author);
    assertThat(author.getName()).isEqualTo("Yuval Noah Harari");
  }

  @Test
  @DisplayName("Should throw ResponseStatusException when update author with non existing id")
  void shouldUpdateAuthorWithNonExistingId() {
    UpdateAuthorRequestDto requestDto = UpdateAuthorRequestDto.builder().build();

    Mockito.when(authorRepository.findById(any())).thenReturn(Optional.empty());

    assertThatThrownBy(() -> authorService.update(1L, requestDto))
        .isInstanceOf(ResponseStatusException.class)
        .hasMessageContaining(HttpStatus.NOT_FOUND.toString())
        .hasMessageContaining(AppMessages.AUTHOR_NOT_FOUND_EXCEPTION);
  }

  @Test
  @DisplayName("Should delete author with existing id")
  void shouldDeleteAuthorWithExistingId() {
    Mockito.when(authorRepository.existsById(any())).thenReturn(true);

    authorService.deleteById(1L);

    Mockito.verify(authorRepository).deleteById(1L);
  }

  @Test
  @DisplayName("Should throw ResponseStatusException when delete author with non existing id")
  void shouldThrowWhenDeleteAuthorWithNonExistingId() {
    Mockito.when(authorRepository.existsById(any())).thenReturn(false);

    assertThatThrownBy(() -> authorService.deleteById(1L))
        .isInstanceOf(ResponseStatusException.class)
        .hasMessageContaining(HttpStatus.NOT_FOUND.toString())
        .hasMessageContaining(AppMessages.AUTHOR_NOT_FOUND_EXCEPTION);
  }

  @Test
  @DisplayName("Should return List<BookDto> when find books by author id with existing id")
  void shouldReturnWhenFindBooksByAuthorIdWithExistingId() {
    Author author = Author.builder()
        .id(1L)
        .books(List.of(Book.builder().build(), Book.builder().build()))
        .build();

    Mockito.when(authorRepository.findById(any())).thenReturn(Optional.of(author));

    List<BookDto> books = authorService.findBooksByAuthorId(1L);

    assertThat(books).isNotNull().isNotEmpty().size().isEqualTo(2);
  }

  @Test
  @DisplayName("Should throw ResponseStatusException when find books by author id with non existing id")
  void shouldThrowWhenFindBooksByAuthorIdWithNonExistingId() {
    Mockito.when(authorRepository.findById(any())).thenReturn(Optional.empty());

    assertThatThrownBy(() -> authorService.findBooksByAuthorId(1L))
        .isInstanceOf(ResponseStatusException.class)
        .hasMessageContaining(HttpStatus.NOT_FOUND.toString())
        .hasMessageContaining(AppMessages.AUTHOR_NOT_FOUND_EXCEPTION);
  }
}
