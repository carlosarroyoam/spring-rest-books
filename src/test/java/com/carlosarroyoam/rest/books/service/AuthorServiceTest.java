package com.carlosarroyoam.rest.books.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;

import com.carlosarroyoam.rest.books.constant.AppMessages;
import com.carlosarroyoam.rest.books.dto.AuthorDto;
import com.carlosarroyoam.rest.books.dto.AuthorDto.AuthorDtoMapper;
import com.carlosarroyoam.rest.books.dto.BookDto;
import com.carlosarroyoam.rest.books.dto.CreateAuthorRequestDto;
import com.carlosarroyoam.rest.books.dto.UpdateAuthorRequestDto;
import com.carlosarroyoam.rest.books.entity.Author;
import com.carlosarroyoam.rest.books.entity.Book;
import com.carlosarroyoam.rest.books.repository.AuthorRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
public class AuthorServiceTest {
  @Mock
  private AuthorRepository authorRepository;

  @InjectMocks
  private AuthorService authorService;

  @Test
  @DisplayName("Should return List<AuthorDto> when find all authors")
  void shouldReturnListOfAuthors() {
    List<Author> authors = List.of(Author.builder().build(), Author.builder().build());

    Page<Author> pagedAuthors = new PageImpl<>(authors);

    Mockito.when(authorRepository.findAll(any(Pageable.class))).thenReturn(pagedAuthors);

    List<AuthorDto> authorsDto = authorService.findAll(0, 25);

    assertThat(authorsDto).isNotNull();
    assertThat(authorsDto).isNotEmpty();
    assertThat(authorsDto).size().isEqualTo(2);
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

    Author author = AuthorDtoMapper.INSTANCE.toEntity(requestDto);
    author.setCreatedAt(LocalDateTime.now());
    author.setUpdatedAt(LocalDateTime.now());

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
  void shouldThrowWhenUpdateAuthorWithInvalidData() {
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

    assertThat(books).isNotNull();
    assertThat(books).isNotEmpty();
    assertThat(books).size().isEqualTo(2);
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
