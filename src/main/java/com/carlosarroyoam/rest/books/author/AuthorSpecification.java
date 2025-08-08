package com.carlosarroyoam.rest.books.author;

import com.carlosarroyoam.rest.books.author.entity.Author;
import com.carlosarroyoam.rest.books.core.constant.AppMessages;
import org.springframework.data.jpa.domain.Specification;

public class AuthorSpecification {
  private AuthorSpecification() {
    throw new IllegalAccessError(AppMessages.ILLEGAL_ACCESS_EXCEPTION);
  }

  static Specification<Author> nameContains(String name) {
    return (author, cq, cb) -> {
      if (name == null || name.isBlank()) {
        return cb.conjunction();
      }

      return cb.like(cb.lower(author.get("name")), "%" + name.toLowerCase() + "%");
    };
  }
}
