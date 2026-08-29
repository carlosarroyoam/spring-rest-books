package com.carlosarroyoam.rest.books.cart;

import com.carlosarroyoam.rest.books.book.BookRepository;
import com.carlosarroyoam.rest.books.book.entity.Book;
import com.carlosarroyoam.rest.books.cart.dto.CartResponse;
import com.carlosarroyoam.rest.books.cart.dto.CartResponse.CartResponseMapper;
import com.carlosarroyoam.rest.books.cart.dto.UpdateCartItemRequest;
import com.carlosarroyoam.rest.books.cart.entity.Cart;
import com.carlosarroyoam.rest.books.cart.entity.CartItem;
import com.carlosarroyoam.rest.books.core.constant.AppMessages;
import com.carlosarroyoam.rest.books.core.exception.ResourceNotFoundException;
import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Contiene la lógica de negocio del {@link Cart} de un cliente: consulta, y alta, actualización o
 * baja de sus {@link CartItem}.
 */
@Service
public class CartService {
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

  /**
   * Busca el carrito de un cliente.
   *
   * @param customerId id del cliente
   * @return el carrito del cliente
   */
  @Transactional(readOnly = true)
  public CartResponse findByCustomerId(Long customerId) {
    Cart cartByCustomerId = findCartByCustomerIdOrFail(customerId);
    return CartResponseMapper.INSTANCE.toDto(cartByCustomerId);
  }

  /**
   * Agrega un libro al carrito del cliente o actualiza su cantidad si ya estaba presente.
   *
   * @param customerId id del cliente dueño del carrito
   * @param request libro y cantidad a agregar o actualizar
   */
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

  /**
   * Elimina un ítem del carrito del cliente. No hace nada si el ítem no pertenece a su carrito.
   *
   * @param customerId id del cliente dueño del carrito
   * @param cartItemId id del ítem a eliminar
   */
  @Transactional
  public void deleteCartItem(Long customerId, Long cartItemId) {
    Cart cartByCustomerId = findCartByCustomerIdOrFail(customerId);

    cartByCustomerId.getItems().stream()
        .filter(item -> item.getId().equals(cartItemId))
        .findFirst()
        .ifPresent(cartItem -> cartItemRepository.deleteById(cartItem.getId()));
  }

  /**
   * Busca el carrito de un cliente o lanza {@code 404 Not Found} si no existe.
   *
   * @param customerId id del cliente
   * @return el carrito del cliente
   */
  private Cart findCartByCustomerIdOrFail(Long customerId) {
    return cartRepository
        .findByCustomerId(customerId)
        .orElseThrow(() -> new ResourceNotFoundException(AppMessages.CART_NOT_FOUND_EXCEPTION));
  }

  /**
   * Busca un libro por id o lanza {@code 404 Not Found} si no existe.
   *
   * @param bookId id del libro a buscar
   * @return el libro encontrado
   */
  private Book findBookByIdOrFail(Long bookId) {
    return bookRepository
        .findById(bookId)
        .orElseThrow(() -> new ResourceNotFoundException(AppMessages.BOOK_NOT_FOUND_EXCEPTION));
  }
}
