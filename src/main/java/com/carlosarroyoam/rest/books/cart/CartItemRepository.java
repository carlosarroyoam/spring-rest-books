package com.carlosarroyoam.rest.books.cart;

import com.carlosarroyoam.rest.books.cart.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;

/** Acceso a datos de {@link CartItem} mediante Spring Data JPA. */
public interface CartItemRepository extends JpaRepository<CartItem, Long> {}
