package com.carlosarroyoam.rest.books.cart;

import com.carlosarroyoam.rest.books.cart.entity.Cart;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CartRepository extends JpaRepository<Cart, Long> {
  Optional<Cart> findByCustomerId(Long customerId);

  boolean existsByCustomerId(Long customerId);
}
