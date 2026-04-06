package com.carlosarroyoam.rest.books.customer;

import com.carlosarroyoam.rest.books.core.constant.AppMessages;
import com.carlosarroyoam.rest.books.customer.entity.Customer;
import com.carlosarroyoam.rest.books.customer.entity.Customer_;
import org.springframework.data.jpa.domain.Specification;

public class CustomerSpecification {
  private CustomerSpecification() {
    throw new IllegalAccessError(AppMessages.ILLEGAL_ACCESS_EXCEPTION);
  }

  static Specification<Customer> firstNameContains(String firstName) {
    return (root, cq, cb) -> {
      if (firstName == null || firstName.isBlank()) {
        return cb.conjunction();
      }

      return cb.like(cb.lower(root.get(Customer_.firstName)), "%" + firstName.toLowerCase() + "%");
    };
  }

  static Specification<Customer> lastNameContains(String lastName) {
    return (root, cq, cb) -> {
      if (lastName == null || lastName.isBlank()) {
        return cb.conjunction();
      }

      return cb.like(cb.lower(root.get(Customer_.lastName)), "%" + lastName.toLowerCase() + "%");
    };
  }

  static Specification<Customer> emailContains(String email) {
    return (root, cq, cb) -> {
      if (email == null || email.isBlank()) {
        return cb.conjunction();
      }

      return cb.like(cb.lower(root.get(Customer_.email)), "%" + email.toLowerCase() + "%");
    };
  }

  static Specification<Customer> usernameContains(String username) {
    return (root, cq, cb) -> {
      if (username == null || username.isBlank()) {
        return cb.conjunction();
      }

      return cb.like(cb.lower(root.get(Customer_.username)), "%" + username.toLowerCase() + "%");
    };
  }
}
