package com.example.demospringrest.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.example.demospringrest.entities.Book;

@Service
public class BookPriceCalculatorService {
	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	public double calculatePrice(Book book) {
		return book.getPrice() + 10;
	}
}
