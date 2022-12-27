package com.carlosarroyoam.bookservice.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.carlosarroyoam.bookservice.entities.Book;

@Service
public class BookPriceCalculatorService {
    private final Logger logger = LoggerFactory.getLogger(BookPriceCalculatorService.class);

    public double calculatePrice(Book book) {
        logger.debug("Calculated price: {}", book.getPrice() + 10);

        return book.getPrice() + 10;
    }
}
