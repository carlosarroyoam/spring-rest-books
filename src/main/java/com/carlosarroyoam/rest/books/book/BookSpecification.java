package com.carlosarroyoam.rest.books.book;

import com.carlosarroyoam.rest.books.author.entity.Author;
import com.carlosarroyoam.rest.books.book.entity.Book;
import com.carlosarroyoam.rest.books.core.constant.AppMessages;
import jakarta.persistence.criteria.Join;
import java.util.Arrays;
import java.util.List;
import org.springframework.data.jpa.domain.Specification;

public class BookSpecification {
  private BookSpecification() {
    throw new IllegalAccessError(AppMessages.ILLEGAL_ACCESS_EXCEPTION);
  }

  static Specification<Book> isbn(String isbn) {
    return (book, cq, cb) -> {
      if (isbn == null || isbn.isBlank()) {
        return cb.conjunction();
      }

      return cb.equal(book.get("isbn"), isbn);
    };
  }

  static Specification<Book> titleContains(String title) {
    return (book, cq, cb) -> {
      if (title == null || title.isBlank()) {
        return cb.conjunction();
      }

      return cb.like(cb.lower(book.get("title")), "%" + title.toLowerCase() + "%");
    };
  }

  static Specification<Book> author(String author) {
    return (book, cq, cb) -> {
      if (author == null || author.isBlank()) {
        return cb.conjunction();
      }

      Join<Book, Author> authorJoin = book.join("authors");

      return cb.like(cb.lower(authorJoin.get("name")), "%" + author.toLowerCase() + "%");
    };
  }

  static Specification<Book> isAvailableOnline(Boolean isAvailableOnline) {
    return (book, cq, cb) -> {
      if (isAvailableOnline == null) {
        return cb.conjunction();
      }

      return cb.equal(book.get("isAvailableOnline"), isAvailableOnline);
    };
  }
}
