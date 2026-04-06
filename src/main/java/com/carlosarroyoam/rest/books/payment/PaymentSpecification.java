package com.carlosarroyoam.rest.books.payment;

import com.carlosarroyoam.rest.books.core.constant.AppMessages;
import com.carlosarroyoam.rest.books.order.entity.Order_;
import com.carlosarroyoam.rest.books.payment.entity.Payment;
import com.carlosarroyoam.rest.books.payment.entity.PaymentMethod;
import com.carlosarroyoam.rest.books.payment.entity.PaymentStatus;
import com.carlosarroyoam.rest.books.payment.entity.Payment_;
import java.math.BigDecimal;
import org.springframework.data.jpa.domain.Specification;

public class PaymentSpecification {
  private PaymentSpecification() {
    throw new IllegalAccessError(AppMessages.ILLEGAL_ACCESS_EXCEPTION);
  }

  static Specification<Payment> methodEquals(PaymentMethod method) {
    return (root, cq, cb) -> {
      if (method == null) {
        return cb.conjunction();
      }

      return cb.equal(root.get(Payment_.method), method);
    };
  }

  static Specification<Payment> amountGreaterThanOrEqual(BigDecimal minAmount) {
    return (root, cq, cb) -> {
      if (minAmount == null) {
        return cb.conjunction();
      }

      return cb.greaterThanOrEqualTo(root.get(Payment_.amount), minAmount);
    };
  }

  static Specification<Payment> amountLessThanOrEqual(BigDecimal maxAmount) {
    return (root, cq, cb) -> {
      if (maxAmount == null) {
        return cb.conjunction();
      }

      return cb.lessThanOrEqualTo(root.get(Payment_.amount), maxAmount);
    };
  }

  static Specification<Payment> statusEquals(PaymentStatus status) {
    return (root, cq, cb) -> {
      if (status == null) {
        return cb.conjunction();
      }

      return cb.equal(root.get(Payment_.status), status);
    };
  }

  static Specification<Payment> transactionIdContains(String transactionId) {
    return (root, cq, cb) -> {
      if (transactionId == null || transactionId.isBlank()) {
        return cb.conjunction();
      }

      return cb.like(cb.lower(root.get(Payment_.transactionId)),
          "%" + transactionId.toLowerCase() + "%");
    };
  }

  static Specification<Payment> orderIdEquals(Long orderId) {
    return (root, cq, cb) -> {
      if (orderId == null) {
        return cb.conjunction();
      }

      return cb.equal(root.get(Payment_.order).get(Order_.id), orderId);
    };
  }
}