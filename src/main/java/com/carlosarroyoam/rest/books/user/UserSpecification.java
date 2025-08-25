package com.carlosarroyoam.rest.books.user;

import com.carlosarroyoam.rest.books.core.constant.AppMessages;
import com.carlosarroyoam.rest.books.user.entity.User;
import org.springframework.data.jpa.domain.Specification;

public class UserSpecification {
  private UserSpecification() {
    throw new IllegalAccessError(AppMessages.ILLEGAL_ACCESS_EXCEPTION);
  }

  static Specification<User> firstNameContains(String firstName) {
    return (user, cq, cb) -> {
      if (firstName == null || firstName.isBlank()) {
        return cb.conjunction();
      }

      return cb.like(cb.lower(user.get("firstName")), "%" + firstName.toLowerCase() + "%");
    };
  }

  static Specification<User> lastNameContains(String lastName) {
    return (user, cq, cb) -> {
      if (lastName == null || lastName.isBlank()) {
        return cb.conjunction();
      }

      return cb.like(cb.lower(user.get("lastName")), "%" + lastName.toLowerCase() + "%");
    };
  }

  static Specification<User> emailContains(String email) {
    return (user, cq, cb) -> {
      if (email == null || email.isBlank()) {
        return cb.conjunction();
      }

      return cb.like(cb.lower(user.get("email")), "%" + email.toLowerCase() + "%");
    };
  }

  static Specification<User> usernameContains(String username) {
    return (user, cq, cb) -> {
      if (username == null || username.isBlank()) {
        return cb.conjunction();
      }

      return cb.like(cb.lower(user.get("username")), "%" + username.toLowerCase() + "%");
    };
  }
}
