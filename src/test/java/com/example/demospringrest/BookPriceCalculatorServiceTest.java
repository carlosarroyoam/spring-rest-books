package com.example.demospringrest;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.example.demospringrest.entities.Book;
import com.example.demospringrest.services.BookPriceCalculatorService;

class BookPriceCalculatorServiceTest {
	@Test
	@DisplayName("Tests the calculation of the price of a book")
	void calculatePrice() {
		// having
		Book book = new Book(null, "Homo Deus", "Yuval Noah", 12.99d, LocalDate.of(2018, 12, 1), true);
		BookPriceCalculatorService bookPriceCalculatorService = new BookPriceCalculatorService();

		// then
		double price = bookPriceCalculatorService.calculatePrice(book);

		// assert
		assertTrue(price > 0);
	}
}
