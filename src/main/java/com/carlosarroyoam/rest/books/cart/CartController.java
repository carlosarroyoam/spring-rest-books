package com.carlosarroyoam.rest.books.cart;

import com.carlosarroyoam.rest.books.cart.dto.CartDto;
import com.carlosarroyoam.rest.books.cart.dto.UpdateCartItemRequestDto;
import com.carlosarroyoam.rest.books.core.constant.CustomClaimNames;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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

  public CartController(CartService cartService) {
    this.cartService = cartService;
  }

  @GetMapping(produces = "application/json")
  @PreAuthorize("hasRole('App/Customer')")
  public ResponseEntity<CartDto> findByUsername(@AuthenticationPrincipal Jwt jwt) {
    Long customerId = jwt.getClaim(CustomClaimNames.CUSTOMER_ID);
    CartDto cartByUsername = cartService.findByCustomerId(customerId);
    return ResponseEntity.ok(cartByUsername);
  }

  @PutMapping(value = "/items", consumes = "application/json")
  @PreAuthorize("hasRole('App/Customer')")
  public ResponseEntity<Void> updateCartItem(
      @Valid @RequestBody UpdateCartItemRequestDto requestDto, @AuthenticationPrincipal Jwt jwt) {
    Long customerId = jwt.getClaim(CustomClaimNames.CUSTOMER_ID);
    cartService.updateCartItem(customerId, requestDto);
    return ResponseEntity.noContent().build();
  }

  @DeleteMapping(value = "/items/{cartItemId}")
  @PreAuthorize("hasRole('App/Customer')")
  public ResponseEntity<Void> deleteCartItem(@PathVariable Long cartItemId,
      @AuthenticationPrincipal Jwt jwt) {
    Long customerId = jwt.getClaim(CustomClaimNames.CUSTOMER_ID);
    cartService.deleteCartItem(customerId, cartItemId);
    return ResponseEntity.noContent().build();
  }
}
