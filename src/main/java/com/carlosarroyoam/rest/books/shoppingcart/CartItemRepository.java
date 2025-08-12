package com.carlosarroyoam.rest.books.shoppingcart;

import com.carlosarroyoam.rest.books.shoppingcart.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {
}
