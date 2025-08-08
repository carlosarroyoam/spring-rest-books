package com.carlosarroyoam.rest.books.user;

import com.carlosarroyoam.rest.books.core.constant.AppMessages;
import com.carlosarroyoam.rest.books.user.entity.User;
import org.springframework.data.jpa.domain.Specification;

public class UserSpecification {
  private UserSpecification() {
    throw new IllegalAccessError(AppMessages.ILLEGAL_ACCESS_EXCEPTION);
  }

  static Specification<User> nameContains(String name) {
    return (user, cq, cb) -> {
      if (name == null || name.isBlank()) {
        return cb.conjunction();
      }

      return cb.like(cb.lower(user.get("name")), "%" + name.toLowerCase() + "%");
    };
  }

  static Specification<User> ageEquals(Byte age) {
    return (user, cq, cb) -> {
      if (age == null) {
        return cb.conjunction();
      }

      return cb.equal(user.get("age"), age);
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

  static Specification<User> isActive(Boolean isActive) {
    return (user, cq, cb) -> {
      if (isActive == null) {
        return cb.conjunction();
      }

      return cb.equal(user.get("isActive"), isActive);
    };
  }

  static Specification<User> roleIdEquals(Integer roleId) {
    return (user, cq, cb) -> {
      if (roleId == null) {
        return cb.conjunction();
      }

      return cb.equal(user.get("role").get("id"), roleId);
    };
  }
}
