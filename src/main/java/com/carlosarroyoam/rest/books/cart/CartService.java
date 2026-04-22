package com.carlosarroyoam.rest.books.cart;

import com.carlosarroyoam.rest.books.book.BookRepository;
import com.carlosarroyoam.rest.books.book.entity.Book;
import com.carlosarroyoam.rest.books.cart.dto.CartResponse;
import com.carlosarroyoam.rest.books.cart.dto.CartResponse.CartResponseMapper;
import com.carlosarroyoam.rest.books.cart.dto.UpdateCartItemRequest;
import com.carlosarroyoam.rest.books.cart.entity.Cart;
import com.carlosarroyoam.rest.books.cart.entity.CartItem;
import com.carlosarroyoam.rest.books.core.constant.AppMessages;
import java.time.LocalDateTime;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class CartService {
  private static final Logger log = LoggerFactory.getLogger(CartService.class);
  private final CartRepository cartRepository;
  private final CartItemRepository cartItemRepository;
  private final BookRepository bookRepository;

  public CartService(
      CartRepository cartRepository,
      CartItemRepository cartItemRepository,
      BookRepository bookRepository) {
    this.cartRepository = cartRepository;
    this.cartItemRepository = cartItemRepository;
    this.bookRepository = bookRepository;
  }

  @Transactional(readOnly = true)
  public CartResponse findByCustomerId(Long customerId) {
    Cart cartByCustomerId = findCartByCustomerIdOrFail(customerId);
    return CartResponseMapper.INSTANCE.toDto(cartByCustomerId);
  }

  @Transactional
  public void updateCartItem(Long customerId, UpdateCartItemRequest request) {
    Cart cartByCustomerId = findCartByCustomerIdOrFail(customerId);
    Book bookById = findBookByIdOrFail(request.getBookId());

    Optional<CartItem> cartItemOptional =
        cartByCustomerId.getItems().stream()
            .filter(item -> item.getBook().getId().equals(request.getBookId()))
            .findFirst();

    CartItem cartItem =
        cartItemOptional.orElseGet(
            () ->
                CartItem.builder()
                    .book(bookById)
                    .quantity(request.getQuantity())
                    .addedAt(LocalDateTime.now())
                    .cart(cartByCustomerId)
                    .build());

    cartItem.setQuantity(request.getQuantity());
    cartItem.setAddedAt(LocalDateTime.now());
    cartItemRepository.save(cartItem);
  }

  @Transactional
  public void deleteCartItem(Long customerId, Long cartItemId) {
    Cart cartByCustomerId = findCartByCustomerIdOrFail(customerId);

    cartByCustomerId.getItems().stream()
        .filter(item -> item.getId().equals(cartItemId))
        .findFirst()
        .ifPresent(cartItem -> cartItemRepository.deleteById(cartItem.getId()));
  }

  private Cart findCartByCustomerIdOrFail(Long customerId) {
    return cartRepository
        .findByCustomerId(customerId)
        .orElseThrow(
            () -> {
              log.warn(AppMessages.CART_NOT_FOUND_EXCEPTION);
              return new ResponseStatusException(
                  HttpStatus.NOT_FOUND, AppMessages.CART_NOT_FOUND_EXCEPTION);
            });
  }

  private Book findBookByIdOrFail(Long bookId) {
    return bookRepository
        .findById(bookId)
        .orElseThrow(
            () -> {
              log.warn(AppMessages.BOOK_NOT_FOUND_EXCEPTION);
              return new ResponseStatusException(
                  HttpStatus.NOT_FOUND, AppMessages.BOOK_NOT_FOUND_EXCEPTION);
            });
  }
}
