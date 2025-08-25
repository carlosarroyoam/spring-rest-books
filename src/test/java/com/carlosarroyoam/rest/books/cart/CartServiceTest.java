package com.carlosarroyoam.rest.books.cart;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.carlosarroyoam.rest.books.book.BookRepository;
import com.carlosarroyoam.rest.books.book.entity.Book;
import com.carlosarroyoam.rest.books.cart.dto.CartDto;
import com.carlosarroyoam.rest.books.cart.dto.UpdateCartItemRequestDto;
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
  @Mock
  private CartRepository cartRepository;

  @Mock
  private CartItemRepository cartItemRepository;

  @Mock
  private BookRepository bookRepository;

  @InjectMocks
  private CartService cartService;

  private Cart cart;
  private CartItem cartItem;

  @BeforeEach
  void setUp() {
    LocalDateTime now = LocalDateTime.now();

    cartItem = CartItem.builder()
        .id(1L)
        .quantity(1)
        .bookId(1L)
        .book(Book.builder().id(1L).build())
        .addedAt(now)
        .build();

    cart = Cart.builder()
        .id(1L)
        .items(List.of(cartItem))
        .customerId(1L)
        .customer(Customer.builder().id(1L).build())
        .createdAt(now)
        .updatedAt(now)
        .build();
  }

  @Test
  @DisplayName("Should return CartDto when find cart by customer id")
  void shouldReturnCartDtoWhenFindCartByCustomerId() {
    when(cartRepository.findByCustomerId(anyLong())).thenReturn(Optional.of(cart));

    CartDto cartDto = cartService.findByCustomerId(1L);

    assertThat(cartDto).isNotNull();
    assertThat(cartDto.getId()).isEqualTo(1L);
    assertThat(cartDto.getItems()).isNotNull();
    assertThat(cartDto.getItems().get(0)).isNotNull();
    assertThat(cartDto.getItems().get(0).getId()).isEqualTo(1L);
    assertThat(cartDto.getItems().get(0).getQuantity()).isEqualTo(1);
    assertThat(cartDto.getItems().get(0).getBook()).isNotNull();
    assertThat(cartDto.getItems().get(0).getBook().getId()).isEqualTo(1L);
    assertThat(cartDto.getItems().get(0).getAddedAt()).isNotNull();
    assertThat(cartDto.getCustomer()).isNotNull();
    assertThat(cartDto.getCustomer().getId()).isEqualTo(1L);
    assertThat(cartDto.getCreatedAt()).isNotNull();
    assertThat(cartDto.getUpdatedAt()).isNotNull();
  }

  @Test
  @DisplayName("Should throw ResponseStatusException when find cart by customer id with no carts")
  void shouldThrowWhenFindCartByUsernameWithNoCarts() {
    when(cartRepository.findByCustomerId(anyLong())).thenReturn(Optional.ofNullable(null));

    assertThatThrownBy(() -> cartService.findByCustomerId(1L))
        .isInstanceOf(ResponseStatusException.class)
        .hasMessageContaining(HttpStatus.NOT_FOUND.toString())
        .hasMessageContaining(AppMessages.CART_NOT_FOUND_EXCEPTION);
  }

  @Test
  @DisplayName("Should update cart item with valid data")
  void shouldUpdateCartItemWhitValidData() {
    UpdateCartItemRequestDto requestDto = UpdateCartItemRequestDto.builder()
        .quantity(1)
        .bookId(1L)
        .build();
    when(cartRepository.findByCustomerId(anyLong())).thenReturn(Optional.of(cart));
    when(bookRepository.existsById(anyLong())).thenReturn(Boolean.TRUE);
    when(cartItemRepository.save(any(CartItem.class))).thenReturn(cartItem);

    cartService.updateCartItem(1L, requestDto);

    verify(cartItemRepository).save(any(CartItem.class));
  }

  @Test
  @DisplayName("Should update cart item with non existing cart item")
  void shouldUpdateCartItemWhitNonExistingCartItem() {
    UpdateCartItemRequestDto requestDto = UpdateCartItemRequestDto.builder()
        .quantity(1)
        .bookId(1L)
        .build();
    Cart cartWithoutItems = Cart.builder().id(1L).build();
    when(cartRepository.findByCustomerId(anyLong())).thenReturn(Optional.of(cartWithoutItems));
    when(bookRepository.existsById(anyLong())).thenReturn(Boolean.TRUE);
    when(cartItemRepository.save(any(CartItem.class))).thenReturn(cartItem);

    cartService.updateCartItem(1L, requestDto);

    verify(cartItemRepository).save(any(CartItem.class));
  }

  @Test
  @DisplayName("Should throw ResponseStatusException when update cart item with non existing cart id")
  void shouldThrowWhenUpdateCartItemWhitNonExistingCartId() {
    UpdateCartItemRequestDto requestDto = UpdateCartItemRequestDto.builder().build();

    when(cartRepository.findByCustomerId(anyLong())).thenReturn(Optional.ofNullable(null));

    assertThatThrownBy(() -> cartService.updateCartItem(1L, requestDto))
        .isInstanceOf(ResponseStatusException.class)
        .hasMessageContaining(HttpStatus.NOT_FOUND.toString())
        .hasMessageContaining(AppMessages.CART_NOT_FOUND_EXCEPTION);
  }

  @Test
  @DisplayName("Should throw ResponseStatusException when update cart item with non existing book id")
  void shouldThrowWhenUpdateCartItemWhitNonExistingBookId() {
    UpdateCartItemRequestDto requestDto = UpdateCartItemRequestDto.builder().bookId(1L).build();

    when(cartRepository.findByCustomerId(anyLong())).thenReturn(Optional.of(cart));
    when(bookRepository.existsById(anyLong())).thenReturn(Boolean.FALSE);

    assertThatThrownBy(() -> cartService.updateCartItem(1L, requestDto))
        .isInstanceOf(ResponseStatusException.class)
        .hasMessageContaining(HttpStatus.NOT_FOUND.toString())
        .hasMessageContaining(AppMessages.BOOK_NOT_FOUND_EXCEPTION);
  }

  @Test
  @DisplayName("Should delete cart item with existing id")
  void shouldThrowWhenDeleteCartItemWhitExistingId() {
    when(cartRepository.findByCustomerId(anyLong())).thenReturn(Optional.of(cart));

    cartService.deleteCartItem(1L, 1L);

    verify(cartItemRepository).deleteById(anyLong());
  }

  @Test
  @DisplayName("Should throw ResponseStatusException when delete cart item with non existing cart item id")
  void shouldThrowWhenDeleteCartItemWhitNonExistingCartItemId() {
    when(cartRepository.findByCustomerId(anyLong())).thenReturn(Optional.ofNullable(null));

    assertThatThrownBy(() -> cartService.deleteCartItem(1L, 1L))
        .isInstanceOf(ResponseStatusException.class)
        .hasMessageContaining(HttpStatus.NOT_FOUND.toString())
        .hasMessageContaining(AppMessages.CART_NOT_FOUND_EXCEPTION);
  }

  @Test
  @DisplayName("Should throw ResponseStatusException when delete cart item with non existing cart id")
  void shouldThrowWhenDeleteCartItemWhitNonExistingCartId() {
    Cart cartWithoutItems = Cart.builder().id(1L).build();

    when(cartRepository.findByCustomerId(anyLong())).thenReturn(Optional.of(cartWithoutItems));

    assertThatThrownBy(() -> cartService.deleteCartItem(1L, 1L))
        .isInstanceOf(ResponseStatusException.class)
        .hasMessageContaining(HttpStatus.NOT_FOUND.toString())
        .hasMessageContaining(AppMessages.CART_ITEM_NOT_FOUND_EXCEPTION);
  }
}
