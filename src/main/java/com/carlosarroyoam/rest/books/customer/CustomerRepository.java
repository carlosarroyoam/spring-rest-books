package com.carlosarroyoam.rest.books.customer;

import com.carlosarroyoam.rest.books.customer.entity.Customer;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface CustomerRepository
    extends JpaRepository<Customer, Long>, JpaSpecificationExecutor<Customer> {
  Optional<Customer> findByEmail(String email);

  Optional<Customer> findByUsername(String username);

  boolean existsByEmail(String email);

  boolean existsByUsername(String username);
}
