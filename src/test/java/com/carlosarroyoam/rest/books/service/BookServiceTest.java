package com.carlosarroyoam.rest.books.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import com.carlosarroyoam.rest.books.dto.BookResponse;
import com.carlosarroyoam.rest.books.entity.Book;
import com.carlosarroyoam.rest.books.mapper.AuthorMapper;
import com.carlosarroyoam.rest.books.mapper.BookMapper;
import com.carlosarroyoam.rest.books.repository.BookRepository;

@ExtendWith(MockitoExtension.class)
class BookServiceTest {
	@Spy
	private AuthorMapper authorMapper = Mappers.getMapper(AuthorMapper.class);

	@Spy
	@InjectMocks
	private BookMapper bookMapper = Mappers.getMapper(BookMapper.class);

	@Mock
	private BookRepository bookRepository;

	@InjectMocks
	private BookService bookService;

	@Test
	@DisplayName("Should return a list of books")
	void shouldReturnListBooks() {
		List<Book> expectedBooks = List.of(
				new Book("978-1-3035-0529-4", "Homo Deus: A Brief History of Tomorrow",
						"https://images.isbndb.com/covers/39/36/9781784703936.jpg", BigDecimal.valueOf(22.99d), false,
						LocalDate.of(2017, 1, 1), LocalDateTime.now(), LocalDateTime.now()),
				new Book("978-9-7389-4434-3", "Sapiens: A Brief History of Humankind",
						"https://images.isbndb.com/covers/60/97/9780062316097.jpg", BigDecimal.valueOf(20.79d), true,
						LocalDate.of(2022, 12, 1), LocalDateTime.now(), LocalDateTime.now()));
		Mockito.when(bookRepository.findAll()).thenReturn(expectedBooks);

		List<BookResponse> books = bookService.findAll(0, 25);

		assertThat(books).isNotNull().isNotEmpty().size().isEqualTo(2);
	}
}
