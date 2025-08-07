package com.carlosarroyoam.rest.books.book;

import com.carlosarroyoam.rest.books.book.entity.Book;
import com.carlosarroyoam.rest.books.core.constant.AppMessages;
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

      return cb.like(book.get("title"), "%" + title + "%");
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
