package com.carlosarroyoam.rest.books.cart;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.carlosarroyoam.rest.books.book.BookRepository;
import com.carlosarroyoam.rest.books.book.entity.Book;
import com.carlosarroyoam.rest.books.cart.dto.CartResponse;
import com.carlosarroyoam.rest.books.cart.dto.UpdateCartItemRequest;
import com.carlosarroyoam.rest.books.cart.entity.Cart;
import com.carlosarroyoam.rest.books.cart.entity.CartItem;
import com.carlosarroyoam.rest.books.core.constant.AppMessages;
import com.carlosarroyoam.rest.books.customer.entity.Customer;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {
  @Mock private CartRepository cartRepository;

  @Mock private CartItemRepository cartItemRepository;

  @Mock private BookRepository bookRepository;

  @InjectMocks private CartService cartService;

  private Book book;
  private Cart cart;
  private CartItem cartItem;

  @BeforeEach
  void setUp() {
    LocalDateTime now = LocalDateTime.now();

    book = Book.builder().id(1L).build();

    cartItem = CartItem.builder().id(1L).quantity(1).book(book).addedAt(now).build();

    cart =
        Cart.builder()
            .id(1L)
            .items(List.of(cartItem))
            .customer(Customer.builder().id(1L).build())
            .createdAt(now)
            .updatedAt(now)
            .build();
  }

  @Test
  @DisplayName("Given cart exists, when find by customer id, then returns cart")
  void givenCartExists_whenFindByCustomerId_thenReturnsCart() {
    when(cartRepository.findByCustomerId(anyLong())).thenReturn(Optional.of(cart));

    CartResponse cartResponse = cartService.findByCustomerId(1L);

    assertThat(cartResponse).isNotNull();
    assertThat(cartResponse.getId()).isEqualTo(1L);
    assertThat(cartResponse.getItems()).isNotNull();
    assertThat(cartResponse.getCustomer()).isNotNull();
  }

  @Test
  @DisplayName("Given no cart exists, when find by customer id, then throws not found exception")
  void givenNoCartExists_whenFindByCustomerId_thenThrowsNotFoundException() {
    when(cartRepository.findByCustomerId(anyLong())).thenReturn(Optional.empty());

    assertThatThrownBy(() -> cartService.findByCustomerId(1L))
        .isInstanceOf(ResponseStatusException.class)
        .hasMessageContaining(HttpStatus.NOT_FOUND.toString())
        .hasMessageContaining(AppMessages.CART_NOT_FOUND_EXCEPTION);
  }

  @Test
  @DisplayName("Given cart and book exist, when update cart item, then updates cart item")
  void givenCartAndBookExist_whenUpdateCartItem_thenUpdatesCartItem() {
    UpdateCartItemRequest request = UpdateCartItemRequest.builder().quantity(1).bookId(1L).build();

    when(cartRepository.findByCustomerId(anyLong())).thenReturn(Optional.of(cart));
    when(bookRepository.findById(anyLong())).thenReturn(Optional.of(book));
    when(cartItemRepository.save(any(CartItem.class))).thenReturn(cartItem);

    cartService.updateCartItem(1L, request);

    verify(cartItemRepository).save(any(CartItem.class));
  }

  @Test
  @DisplayName("Given cart has no item, when update cart item, then creates cart item")
  void givenCartHasNoItem_whenUpdateCartItem_thenCreatesCartItem() {
    UpdateCartItemRequest request = UpdateCartItemRequest.builder().quantity(1).bookId(1L).build();

    Cart cartWithoutItems = Cart.builder().id(1L).build();

    when(cartRepository.findByCustomerId(anyLong())).thenReturn(Optional.of(cartWithoutItems));
    when(bookRepository.findById(anyLong())).thenReturn(Optional.of(book));
    when(cartItemRepository.save(any(CartItem.class))).thenReturn(cartItem);

    cartService.updateCartItem(1L, request);

    verify(cartItemRepository).save(any(CartItem.class));
  }

  @Test
  @DisplayName("Given cart does not exist, when update cart item, then throws not found exception")
  void givenCartDoesNotExist_whenUpdateCartItem_thenThrowsNotFoundException() {
    UpdateCartItemRequest request = UpdateCartItemRequest.builder().build();

    when(cartRepository.findByCustomerId(anyLong())).thenReturn(Optional.empty());

    assertThatThrownBy(() -> cartService.updateCartItem(1L, request))
        .isInstanceOf(ResponseStatusException.class)
        .hasMessageContaining(HttpStatus.NOT_FOUND.toString())
        .hasMessageContaining(AppMessages.CART_NOT_FOUND_EXCEPTION);
  }

  @Test
  @DisplayName("Given book does not exist, when update cart item, then throws not found exception")
  void givenBookDoesNotExist_whenUpdateCartItem_thenThrowsNotFoundException() {
    UpdateCartItemRequest request = UpdateCartItemRequest.builder().bookId(1L).build();

    when(cartRepository.findByCustomerId(anyLong())).thenReturn(Optional.of(cart));
    when(bookRepository.findById(anyLong())).thenReturn(Optional.empty());

    assertThatThrownBy(() -> cartService.updateCartItem(1L, request))
        .isInstanceOf(ResponseStatusException.class)
        .hasMessageContaining(HttpStatus.NOT_FOUND.toString())
        .hasMessageContaining(AppMessages.BOOK_NOT_FOUND_EXCEPTION);
  }

  @Test
  @DisplayName("Given cart item exists, when delete cart item, then deletes cart item")
  void givenCartItemExists_whenDeleteCartItem_thenDeletesCartItem() {
    when(cartRepository.findByCustomerId(anyLong())).thenReturn(Optional.of(cart));

    cartService.deleteCartItem(1L, 1L);

    verify(cartItemRepository).deleteById(1L);
  }

  @Test
  @DisplayName("Given cart does not exist, when delete cart item, then throws not found exception")
  void givenCartDoesNotExist_whenDeleteCartItem_thenThrowsNotFoundException() {
    when(cartRepository.findByCustomerId(anyLong())).thenReturn(Optional.empty());

    assertThatThrownBy(() -> cartService.deleteCartItem(1L, 1L))
        .isInstanceOf(ResponseStatusException.class)
        .hasMessageContaining(HttpStatus.NOT_FOUND.toString())
        .hasMessageContaining(AppMessages.CART_NOT_FOUND_EXCEPTION);
  }

  @Test
  @DisplayName(
      "Given cart item does not exist, when delete cart item, then throws not found exception")
  void givenCartItemDoesNotExist_whenDeleteCartItem_thenThrowsNotFoundException() {
    Cart cartWithoutItems = Cart.builder().id(1L).build();

    when(cartRepository.findByCustomerId(anyLong())).thenReturn(Optional.of(cartWithoutItems));

    assertThatThrownBy(() -> cartService.deleteCartItem(1L, 1L))
        .isInstanceOf(ResponseStatusException.class)
        .hasMessageContaining(HttpStatus.NOT_FOUND.toString())
        .hasMessageContaining(AppMessages.CART_ITEM_NOT_FOUND_EXCEPTION);
  }
}
