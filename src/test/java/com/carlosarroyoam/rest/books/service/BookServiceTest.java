package com.carlosarroyoam.rest.books.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.carlosarroyoam.rest.books.dto.BookDto;
import com.carlosarroyoam.rest.books.entity.Book;
import com.carlosarroyoam.rest.books.repository.BookRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class BookServiceTest {
  @Mock
  private BookRepository bookRepository;

  @InjectMocks
  private BookService bookService;

  @Test
  @DisplayName("Should return a list of books")
  void shouldReturnListBooks() {
    List<Book> expectedBooks = List.of(
        Book.builder()
            .isbn("978-1-3035-0529-4")
            .title("Homo Deus: A Brief History of Tomorrow")
            .coverUrl("https://images.isbndb.com/covers/39/36/9781784703936.jpg")
            .price(new BigDecimal("22.99"))
            .isAvailableOnline(false)
            .publishedAt(LocalDate.parse("2017-01-01"))
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build(),
        Book.builder()
            .isbn("978-9-7389-4434-3")
            .title("Sapiens: A Brief History of Humankind")
            .coverUrl("https://images.isbndb.com/covers/60/97/9780062316097.jpg")
            .price(new BigDecimal("20.79"))
            .isAvailableOnline(false)
            .publishedAt(LocalDate.parse("2022-12-01"))
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build());

    Page<Book> pagedBooks = new PageImpl<Book>(expectedBooks);

    Mockito.when(bookRepository.findAll(ArgumentMatchers.any(Pageable.class)))
        .thenReturn(pagedBooks);

    List<BookDto> books = bookService.findAll(0, 25);

    assertThat(books).isNotNull();
    assertThat(books).isNotEmpty();
    assertThat(books).size().isEqualTo(2);
  }
}
