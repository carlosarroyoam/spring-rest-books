package com.carlosarroyoam.rest.books.book;

import com.carlosarroyoam.rest.books.author.entity.Author;
import com.carlosarroyoam.rest.books.author.entity.Author_;
import com.carlosarroyoam.rest.books.book.entity.Book;
import com.carlosarroyoam.rest.books.book.entity.Book_;
import com.carlosarroyoam.rest.books.core.constant.AppMessages;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import org.springframework.data.jpa.domain.Specification;

public class BookSpecification {
  private BookSpecification() {
    throw new IllegalAccessError(AppMessages.ILLEGAL_ACCESS_EXCEPTION);
  }

  static Specification<Book> isbnEquals(String isbn) {
    return (root, cq, cb) -> {
      if (isbn == null || isbn.isBlank()) {
        return cb.conjunction();
      }

      return cb.equal(root.get(Book_.isbn), isbn);
    };
  }

  static Specification<Book> titleContains(String title) {
    return (root, cq, cb) -> {
      if (title == null || title.isBlank()) {
        return cb.conjunction();
      }

      return cb.like(cb.lower(root.get(Book_.title)), "%" + title.toLowerCase() + "%");
    };
  }

  static Specification<Book> priceGreaterThanOrEqual(BigDecimal minPrice) {
    return (root, cq, cb) -> {
      if (minPrice == null) {
        return cb.conjunction();
      }

      return cb.greaterThanOrEqualTo(root.get(Book_.price), minPrice);
    };
  }

  static Specification<Book> priceLessThanOrEqual(BigDecimal maxPrice) {
    return (root, cq, cb) -> {
      if (maxPrice == null) {
        return cb.conjunction();
      }

      return cb.lessThanOrEqualTo(root.get(Book_.price), maxPrice);
    };
  }

  static Specification<Book> isAvailableOnline(Boolean isAvailableOnline) {
    return (root, cq, cb) -> {
      if (isAvailableOnline == null) {
        return cb.conjunction();
      }

      return cb.equal(root.get(Book_.isAvailableOnline), isAvailableOnline);
    };
  }

  static Specification<Book> authorIdIn(String authorIds) {
    return (root, cq, cb) -> {
      if (authorIds == null || authorIds.isBlank()) {
        return cb.conjunction();
      }

      List<Long> ids = Arrays.stream(authorIds.split(","))
          .map(String::trim)
          .map(Long::parseLong)
          .toList();

      Join<Book, Author> authorJoin = root.join(Book_.authors, JoinType.LEFT);

      return authorJoin.get(Author_.id).in(ids);
    };
  }
}
