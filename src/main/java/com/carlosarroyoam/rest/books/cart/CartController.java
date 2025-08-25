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
  private final CartService cartService;

  public CartController(final CartService cartService) {
    this.cartService = cartService;
  }

  @GetMapping(produces = "application/json")
  public ResponseEntity<CartDto> findByUsername(@AuthenticationPrincipal Jwt jwt) {
    String username = jwt.getClaim(StandardClaimNames.PREFERRED_USERNAME);
    CartDto cartByUsername = cartService.findByUsername(username);
    return ResponseEntity.ok(cartByUsername);
  }

  @PutMapping(value = "/items", consumes = "application/json")
  public ResponseEntity<Void> updateCartItem(
      @Valid @RequestBody UpdateCartItemRequestDto requestDto, @AuthenticationPrincipal Jwt jwt) {
    String username = jwt.getClaim(StandardClaimNames.PREFERRED_USERNAME);
    cartService.updateCartItem(username, requestDto);
    return ResponseEntity.noContent().build();
  }

  @DeleteMapping(value = "/items/{cartItemId}")
  public ResponseEntity<Void> deleteCartItem(@PathVariable Long cartItemId,
      @AuthenticationPrincipal Jwt jwt) {
    String username = jwt.getClaim(StandardClaimNames.PREFERRED_USERNAME);
    cartService.deleteCartItem(username, cartItemId);
    return ResponseEntity.noContent().build();
  }
}
