package com.carlosarroyoam.bookservice.services;

import java.time.LocalDate;
import java.util.List;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import com.carlosarroyoam.bookservice.entities.Book;
import com.carlosarroyoam.bookservice.repositories.BookRepository;

@ExtendWith(MockitoExtension.class)
class BookServiceTest {
	@Mock
	BookRepository bookRepository;

	@InjectMocks
	BookService bookService;

	@Test
	@DisplayName("Test method return empty list when there's no books")
	void testFindAll() {
		List<Book> expectedBooks = List.of();

		Mockito.when(bookRepository.findAll()).thenReturn(expectedBooks);

		List<Book> books = bookService.findAll();

		Assertions.assertThat(books).isNotNull().isEmpty();
	}

	@Test
	@DisplayName("Tests findAll return list of books")
	void testFindAllReturnBooks() {
		List<Book> expectedBooks = List.of(new Book("Homo Deus", "Yuval Noah", 12.99d, LocalDate.of(2018, 12, 1), true),
				new Book("Homo Deus", "Yuval Noah", 12.99d, LocalDate.of(2018, 12, 1), true));

		Mockito.when(bookRepository.findAll()).thenReturn(expectedBooks);

		List<Book> books = bookService.findAll();

		Assertions.assertThat(books).isNotNull().isNotEmpty().size().isEqualTo(2);
	}
}
