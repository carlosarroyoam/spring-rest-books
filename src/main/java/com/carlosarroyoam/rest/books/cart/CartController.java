package com.carlosarroyoam.rest.books.cart;

import com.carlosarroyoam.rest.books.cart.dto.CartDto;
import com.carlosarroyoam.rest.books.cart.dto.UpdateCartItemRequestDto;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.StandardClaimNames;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/carts")
public class CartController {
  private final CartService shoppingCartService;

  public CartController(final CartService shoppingCartService) {
    this.shoppingCartService = shoppingCartService;
  }

  @GetMapping(produces = "application/json")
  public ResponseEntity<CartDto> findByUserId(@AuthenticationPrincipal Jwt jwt) {
    String username = jwt.getClaim(StandardClaimNames.PREFERRED_USERNAME);
    CartDto shoppingCartByUserId = shoppingCartService.findByUsername(username);
    return ResponseEntity.ok(shoppingCartByUserId);
  }

  @PutMapping(value = "/{cartId}/items", consumes = "application/json")
  public ResponseEntity<Void> updateCartItem(@PathVariable Long cartId,
      @Valid @RequestBody UpdateCartItemRequestDto requestDto) {
    shoppingCartService.updateCartItem(cartId, requestDto);
    return ResponseEntity.noContent().build();
  }

  @DeleteMapping(value = "/{cartId}/items/{cartItemId}")
  public ResponseEntity<Void> deleteCartItem(@PathVariable Long cartId,
      @PathVariable Long cartItemId) {
    shoppingCartService.deleteCartItem(cartId, cartItemId);
    return ResponseEntity.noContent().build();
  }
}
