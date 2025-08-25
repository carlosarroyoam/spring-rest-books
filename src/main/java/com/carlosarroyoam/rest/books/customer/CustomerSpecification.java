package com.carlosarroyoam.rest.books.customer;

import com.carlosarroyoam.rest.books.core.constant.AppMessages;
import com.carlosarroyoam.rest.books.customer.entity.Customer;
import org.springframework.data.jpa.domain.Specification;

public class CustomerSpecification {
  private CustomerSpecification() {
    throw new IllegalAccessError(AppMessages.ILLEGAL_ACCESS_EXCEPTION);
  }

  static Specification<Customer> firstNameContains(String firstName) {
    return (customer, cq, cb) -> {
      if (firstName == null || firstName.isBlank()) {
        return cb.conjunction();
      }

      return cb.like(cb.lower(customer.get("firstName")), "%" + firstName.toLowerCase() + "%");
    };
  }

  static Specification<Customer> lastNameContains(String lastName) {
    return (customer, cq, cb) -> {
      if (lastName == null || lastName.isBlank()) {
        return cb.conjunction();
      }

      return cb.like(cb.lower(customer.get("lastName")), "%" + lastName.toLowerCase() + "%");
    };
  }

  static Specification<Customer> emailContains(String email) {
    return (customer, cq, cb) -> {
      if (email == null || email.isBlank()) {
        return cb.conjunction();
      }

      return cb.like(cb.lower(customer.get("email")), "%" + email.toLowerCase() + "%");
    };
  }

  static Specification<Customer> usernameContains(String username) {
    return (customer, cq, cb) -> {
      if (username == null || username.isBlank()) {
        return cb.conjunction();
      }

      return cb.like(cb.lower(customer.get("username")), "%" + username.toLowerCase() + "%");
    };
  }
}
