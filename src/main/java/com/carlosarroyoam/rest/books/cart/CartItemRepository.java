package com.carlosarroyoam.rest.books.cart;

import com.carlosarroyoam.rest.books.cart.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {
}
