package com.carlosarroyoam.rest.books.shoppingcart;

import com.carlosarroyoam.rest.books.shoppingcart.dto.ShoppingCartDto;
import com.carlosarroyoam.rest.books.shoppingcart.dto.UpdateCartItemRequestDto;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/shopping-cart")
public class ShoppingCartController {
  private final ShoppingCartService shoppingCartService;

  public ShoppingCartController(final ShoppingCartService shoppingCartService) {
    this.shoppingCartService = shoppingCartService;
  }

  @GetMapping(value = "/{userId}", produces = "application/json")
  public ResponseEntity<ShoppingCartDto> findByUserId(@PathVariable Long userId) {
    ShoppingCartDto shoppingCartByUserId = shoppingCartService.findByUserId(userId);
    return ResponseEntity.ok(shoppingCartByUserId);
  }

  @PutMapping(value = "/{userId}/items", consumes = "application/json")
  public ResponseEntity<Void> updateCartItem(@PathVariable Long userId,
      @Valid @RequestBody UpdateCartItemRequestDto requestDto) {
    shoppingCartService.updateCartItem(userId, requestDto);
    return ResponseEntity.noContent().build();
  }

  @DeleteMapping(value = "/{userId}/items/{cartItemId}")
  public ResponseEntity<Void> deleteCartItem(@PathVariable Long userId,
      @PathVariable Long cartItemId) {
    shoppingCartService.deleteCartItem(userId, cartItemId);
    return ResponseEntity.noContent().build();
  }
}
