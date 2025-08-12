package com.carlosarroyoam.rest.books.shoppingcart;

import com.carlosarroyoam.rest.books.shoppingcart.entity.ShoppingCart;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ShoppingCartRepository extends JpaRepository<ShoppingCart, Long> {
  Optional<ShoppingCart> findByUserId(Long userId);

  boolean existsByUserId(Long userId);
}
