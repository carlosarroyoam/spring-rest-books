package com.carlosarroyoam.rest.books.cart;

import com.carlosarroyoam.rest.books.cart.entity.Cart;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/** Acceso a datos de {@link Cart} mediante Spring Data JPA. */
public interface CartRepository extends JpaRepository<Cart, Long> {

  /**
   * Busca el carrito de un cliente.
   *
   * @param customerId id del cliente
   * @return el carrito del cliente, si existe
   */
  Optional<Cart> findByCustomerId(Long customerId);

  /**
   * Indica si existe un carrito para el cliente dado.
   *
   * @param customerId id del cliente a verificar
   * @return {@code true} si ya existe un carrito para ese cliente
   */
  boolean existsByCustomerId(Long customerId);
}
