package com.carlosarroyoam.rest.books.payment;

import com.carlosarroyoam.rest.books.core.constant.AppMessages;
import com.carlosarroyoam.rest.books.payment.entity.Payment;
import com.carlosarroyoam.rest.books.payment.entity.PaymentMethod;
import com.carlosarroyoam.rest.books.payment.entity.PaymentStatus;
import java.math.BigDecimal;
import org.springframework.data.jpa.domain.Specification;

public class PaymentSpecification {
  private PaymentSpecification() {
    throw new IllegalAccessError(AppMessages.ILLEGAL_ACCESS_EXCEPTION);
  }

  static Specification<Payment> methodEquals(PaymentMethod method) {
    return (payment, cq, cb) -> {
      if (method == null) {
        return cb.conjunction();
      }

      return cb.equal(payment.get("method"), method);
    };
  }

  static Specification<Payment> amountGreaterThanOrEqual(BigDecimal minAmount) {
    return (payment, cq, cb) -> {
      if (minAmount == null) {
        return cb.conjunction();
      }

      return cb.greaterThanOrEqualTo(payment.get("amount"), minAmount);
    };
  }

  static Specification<Payment> amountLessThanOrEqual(BigDecimal maxAmount) {
    return (payment, cq, cb) -> {
      if (maxAmount == null) {
        return cb.conjunction();
      }

      return cb.lessThanOrEqualTo(payment.get("amount"), maxAmount);
    };
  }

  static Specification<Payment> statusEquals(PaymentStatus status) {
    return (payment, cq, cb) -> {
      if (status == null) {
        return cb.conjunction();
      }

      return cb.equal(payment.get("status"), status);
    };
  }

  static Specification<Payment> transactionIdContains(String transactionId) {
    return (payment, cq, cb) -> {
      if (transactionId == null || transactionId.isBlank()) {
        return cb.conjunction();
      }

      return cb.like(cb.lower(payment.get("transactionId")),
          "%" + transactionId.toLowerCase() + "%");
    };
  }

  static Specification<Payment> orderIdEquals(Long orderId) {
    return (payment, cq, cb) -> {
      if (orderId == null) {
        return cb.conjunction();
      }

      return cb.equal(payment.get("order").get("id"), orderId);
    };
  }
}