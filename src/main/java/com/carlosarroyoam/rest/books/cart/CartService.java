package com.carlosarroyoam.rest.books.cart;

import com.carlosarroyoam.rest.books.book.BookRepository;
import com.carlosarroyoam.rest.books.cart.dto.CartDto;
import com.carlosarroyoam.rest.books.cart.dto.CartDto.ShoppingCartDtoMapper;
import com.carlosarroyoam.rest.books.cart.dto.CartItemDto.CartItemDtoMapper;
import com.carlosarroyoam.rest.books.cart.dto.UpdateCartItemRequestDto;
import com.carlosarroyoam.rest.books.cart.entity.Cart;
import com.carlosarroyoam.rest.books.cart.entity.CartItem;
import com.carlosarroyoam.rest.books.core.constant.AppMessages;
import com.carlosarroyoam.rest.books.user.UserRepository;
import com.carlosarroyoam.rest.books.user.entity.User;
import java.time.LocalDateTime;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class CartService {
  private static final Logger log = LoggerFactory.getLogger(CartService.class);
  private final CartRepository cartRepository;
  private final CartItemRepository cartItemRepository;
  private final UserRepository userRepository;
  private final BookRepository bookRepository;

  public CartService(CartRepository cartRepository, CartItemRepository cartItemRepository,
      UserRepository userRepository, BookRepository bookRepository) {
    this.cartRepository = cartRepository;
    this.cartItemRepository = cartItemRepository;
    this.userRepository = userRepository;
    this.bookRepository = bookRepository;
  }

  public CartDto findByUsername(String username) {
    User userByUsername = userRepository.findByUsername(username).orElseThrow(() -> {
      log.warn(AppMessages.USER_NOT_FOUND_EXCEPTION);
      return new ResponseStatusException(HttpStatus.NOT_FOUND,
          AppMessages.USER_NOT_FOUND_EXCEPTION);
    });

    Cart shoppingCartByUserId = cartRepository.findByUserId(userByUsername.getId())
        .orElseThrow(() -> {
          log.warn(AppMessages.CART_NOT_FOUND_EXCEPTION);
          return new ResponseStatusException(HttpStatus.NOT_FOUND,
              AppMessages.CART_NOT_FOUND_EXCEPTION);
        });

    return ShoppingCartDtoMapper.INSTANCE.toDto(shoppingCartByUserId);
  }

  public void updateCartItem(Long cartId, UpdateCartItemRequestDto requestDto) {
    Cart cartById = cartRepository.findById(cartId).orElseThrow(() -> {
      log.warn(AppMessages.CART_NOT_FOUND_EXCEPTION);
      return new ResponseStatusException(HttpStatus.NOT_FOUND,
          AppMessages.CART_NOT_FOUND_EXCEPTION);
    });

    if (!bookRepository.existsById(requestDto.getBookId())) {
      log.warn(AppMessages.BOOK_NOT_FOUND_EXCEPTION);
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, AppMessages.BOOK_NOT_FOUND_EXCEPTION);
    }

    Optional<CartItem> cartItemOptional = cartById.getItems()
        .stream()
        .filter(item -> item.getBookId().equals(requestDto.getBookId()))
        .findFirst();

    CartItem cartItem = cartItemOptional.isPresent() ? cartItemOptional.get()
        : CartItemDtoMapper.INSTANCE.updateCartItemToEntity(requestDto);
    cartItem.setQuantity(requestDto.getQuantity());
    cartItem.setAddedAt(LocalDateTime.now());
    cartItem.setCartId(cartById.getId());
    cartItemRepository.save(cartItem);
  }

  public void deleteCartItem(Long cartId, Long cartItemId) {
    Cart cartById = cartRepository.findById(cartId).orElseThrow(() -> {
      log.warn(AppMessages.CART_NOT_FOUND_EXCEPTION);
      return new ResponseStatusException(HttpStatus.NOT_FOUND,
          AppMessages.CART_NOT_FOUND_EXCEPTION);
    });

    Optional<CartItem> cartItemOptional = cartById.getItems()
        .stream()
        .filter(item -> item.getId().equals(cartItemId))
        .findFirst();

    if (cartItemOptional.isEmpty()) {
      log.warn(AppMessages.CART_ITEM_NOT_FOUND_EXCEPTION);
      throw new ResponseStatusException(HttpStatus.NOT_FOUND,
          AppMessages.CART_ITEM_NOT_FOUND_EXCEPTION);
    }

    cartItemRepository.deleteById(cartItemOptional.get().getId());
  }
}
