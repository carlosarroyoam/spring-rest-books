package com.carlosarroyoam.rest.books.payment;

import com.carlosarroyoam.rest.books.payment.entity.Payment;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

/** Acceso a datos de {@link Payment} mediante Spring Data JPA. */
public interface PaymentRepository
    extends JpaRepository<Payment, Long>, JpaSpecificationExecutor<Payment> {

  /**
   * Indica si existe un pago para la orden dada.
   *
   * @param orderId id de la orden a verificar
   * @return {@code true} si ya existe un pago para esa orden
   */
  boolean existsByOrderId(Long orderId);

  /**
   * Busca el pago de una orden.
   *
   * @param orderId id de la orden
   * @return el pago de la orden, si existe
   */
  Optional<Payment> findByOrderId(Long orderId);
}
