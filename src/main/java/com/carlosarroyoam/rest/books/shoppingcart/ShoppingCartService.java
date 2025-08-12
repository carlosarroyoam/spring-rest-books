package com.carlosarroyoam.rest.books.shoppingcart;

import com.carlosarroyoam.rest.books.book.BookRepository;
import com.carlosarroyoam.rest.books.core.constant.AppMessages;
import com.carlosarroyoam.rest.books.shoppingcart.dto.CartItemDto.CartItemDtoMapper;
import com.carlosarroyoam.rest.books.shoppingcart.dto.ShoppingCartDto;
import com.carlosarroyoam.rest.books.shoppingcart.dto.ShoppingCartDto.ShoppingCartDtoMapper;
import com.carlosarroyoam.rest.books.shoppingcart.dto.UpdateCartItemRequestDto;
import com.carlosarroyoam.rest.books.shoppingcart.entity.CartItem;
import com.carlosarroyoam.rest.books.shoppingcart.entity.ShoppingCart;
import java.time.LocalDateTime;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ShoppingCartService {
  private static final Logger log = LoggerFactory.getLogger(ShoppingCartService.class);
  private final ShoppingCartRepository shoppingCartRepository;
  private final CartItemRepository cartItemRepository;
  private final BookRepository bookRepository;

  public ShoppingCartService(ShoppingCartRepository shoppingCartRepository,
      CartItemRepository cartItemRepository, BookRepository bookRepository) {
    this.shoppingCartRepository = shoppingCartRepository;
    this.cartItemRepository = cartItemRepository;
    this.bookRepository = bookRepository;
  }

  public ShoppingCartDto findByUserId(Long userId) {
    ShoppingCart shoppingCartByUserId = shoppingCartRepository.findByUserId(userId)
        .orElseThrow(() -> {
          log.warn(AppMessages.SHOPPING_CART_NOT_FOUND_EXCEPTION);
          return new ResponseStatusException(HttpStatus.NOT_FOUND,
              AppMessages.SHOPPING_CART_NOT_FOUND_EXCEPTION);
        });

    return ShoppingCartDtoMapper.INSTANCE.toDto(shoppingCartByUserId);
  }

  public void updateCartItem(Long userId, UpdateCartItemRequestDto requestDto) {
    ShoppingCart shoppingCartByUserId = shoppingCartRepository.findByUserId(userId)
        .orElseThrow(() -> {
          log.warn(AppMessages.SHOPPING_CART_NOT_FOUND_EXCEPTION);
          return new ResponseStatusException(HttpStatus.NOT_FOUND,
              AppMessages.SHOPPING_CART_NOT_FOUND_EXCEPTION);
        });

    if (!bookRepository.existsById(requestDto.getBookId())) {
      log.warn(AppMessages.BOOK_NOT_FOUND_EXCEPTION);
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, AppMessages.BOOK_NOT_FOUND_EXCEPTION);
    }

    Optional<CartItem> cartItemOptional = shoppingCartByUserId.getItems()
        .stream()
        .filter(item -> item.getBookId().equals(requestDto.getBookId()))
        .findFirst();

    CartItem cartItem = cartItemOptional.isPresent() ? cartItemOptional.get()
        : CartItemDtoMapper.INSTANCE.updateCartItemToEntity(requestDto);
    cartItem.setQuantity(requestDto.getQuantity());
    cartItem.setAddedAt(LocalDateTime.now());
    cartItemRepository.save(cartItem);
  }

  public void deleteCartItem(Long userId, Long cartItemId) {
    if (!shoppingCartRepository.existsByUserId(userId)) {
      log.warn(AppMessages.SHOPPING_CART_NOT_FOUND_EXCEPTION);
      throw new ResponseStatusException(HttpStatus.NOT_FOUND,
          AppMessages.SHOPPING_CART_NOT_FOUND_EXCEPTION);
    }

    CartItem cartItem = cartItemRepository.findById(cartItemId).orElseThrow(() -> {
      log.warn(AppMessages.CART_ITEM_NOT_FOUND_EXCEPTION);
      throw new ResponseStatusException(HttpStatus.NOT_FOUND,
          AppMessages.CART_ITEM_NOT_FOUND_EXCEPTION);
    });

    cartItemRepository.delete(cartItem);
  }
}
