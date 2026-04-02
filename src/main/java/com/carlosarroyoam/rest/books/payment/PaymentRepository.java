package com.carlosarroyoam.rest.books.payment;

import com.carlosarroyoam.rest.books.payment.entity.Payment;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
  boolean existsByOrderId(Long orderId);

  Optional<Payment> findByOrderId(Long orderId);
}
