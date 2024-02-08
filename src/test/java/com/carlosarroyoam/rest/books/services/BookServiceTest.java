package com.carlosarroyoam.rest.books.services;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import com.carlosarroyoam.rest.books.dtos.BookResponse;
import com.carlosarroyoam.rest.books.entities.Book;
import com.carlosarroyoam.rest.books.repositories.BookRepository;

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
				new Book("Homo Deus", "Yuval Noah", BigDecimal.valueOf(12.99d), LocalDate.of(2018, 12, 1), true),
				new Book("Homo Deus", "Yuval Noah", BigDecimal.valueOf(12.99d), LocalDate.of(2018, 12, 1), true));
		Mockito.when(bookRepository.findAll()).thenReturn(expectedBooks);

		List<BookResponse> books = bookService.findAll();

		assertThat(books).isNotNull().isNotEmpty().size().isEqualTo(2);
	}

}
