package com.carlosarroyoam.rest.books.orders;

import com.carlosarroyoam.rest.books.core.constant.AppMessages;
import com.carlosarroyoam.rest.books.orders.entity.Order;
import com.carlosarroyoam.rest.books.orders.entity.OrderStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.springframework.data.jpa.domain.Specification;

public class OrderSpecification {
  private OrderSpecification() {
    throw new IllegalAccessError(AppMessages.ILLEGAL_ACCESS_EXCEPTION);
  }

  static Specification<Order> orderNumberEquals(String orderNumber) {
    return (order, cq, cb) -> {
      if (orderNumber == null || orderNumber.isBlank()) {
        return cb.conjunction();
      }

      return cb.equal(order.get("orderNumber"), orderNumber);
    };
  }

  static Specification<Order> totalGreaterThanOrEqual(BigDecimal minTotal) {
    return (order, cq, cb) -> {
      if (minTotal == null) {
        return cb.conjunction();
      }

      return cb.greaterThanOrEqualTo(order.get("total"), minTotal);
    };
  }

  static Specification<Order> totalLessThanOrEqual(BigDecimal maxTotal) {
    return (order, cq, cb) -> {
      if (maxTotal == null) {
        return cb.conjunction();
      }

      return cb.lessThanOrEqualTo(order.get("total"), maxTotal);
    };
  }

  static Specification<Order> shippingAddressContains(String shippingAddress) {
    return (order, cq, cb) -> {
      if (shippingAddress == null || shippingAddress.isBlank()) {
        return cb.conjunction();
      }

      return cb.like(cb.lower(order.get("shippingAddress")),
          "%" + shippingAddress.toLowerCase() + "%");
    };
  }

  static Specification<Order> statusEquals(OrderStatus status) {
    return (order, cq, cb) -> {
      if (status == null) {
        return cb.conjunction();
      }

      return cb.equal(order.get("status"), status);
    };
  }

  static Specification<Order> customerIdEquals(Long customerId) {
    return (order, cq, cb) -> {
      if (customerId == null) {
        return cb.conjunction();
      }

      return cb.equal(order.get("customer").get("id"), customerId);
    };
  }

  static Specification<Order> createdAtBetween(LocalDateTime startDate, LocalDateTime endDate) {
    return (order, cq, cb) -> {
      if (startDate == null && endDate == null) {
        return cb.conjunction();
      }

      if (startDate != null && endDate != null) {
        return cb.between(order.get("createdAt"), startDate, endDate);
      }

      if (startDate != null) {
        return cb.greaterThanOrEqualTo(order.get("createdAt"), startDate);
      }

      return cb.lessThanOrEqualTo(order.get("createdAt"), endDate);
    };
  }
}
