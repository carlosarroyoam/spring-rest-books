package com.carlosarroyoam.rest.books.order;

import com.carlosarroyoam.rest.books.core.constant.AppMessages;
import com.carlosarroyoam.rest.books.customer.entity.Customer_;
import com.carlosarroyoam.rest.books.order.entity.Order;
import com.carlosarroyoam.rest.books.order.entity.OrderStatus;
import com.carlosarroyoam.rest.books.order.entity.Order_;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.springframework.data.jpa.domain.Specification;

public class OrderSpecification {
  private OrderSpecification() {
    throw new IllegalAccessError(AppMessages.ILLEGAL_ACCESS_EXCEPTION);
  }

  static Specification<Order> orderNumberEquals(String rootNumber) {
    return (root, cq, cb) -> {
      if (rootNumber == null || rootNumber.isBlank()) {
        return cb.conjunction();
      }

      return cb.equal(root.get(Order_.orderNumber), rootNumber);
    };
  }

  static Specification<Order> totalGreaterThanOrEqual(BigDecimal minTotal) {
    return (root, cq, cb) -> {
      if (minTotal == null) {
        return cb.conjunction();
      }

      return cb.greaterThanOrEqualTo(root.get(Order_.total), minTotal);
    };
  }

  static Specification<Order> totalLessThanOrEqual(BigDecimal maxTotal) {
    return (root, cq, cb) -> {
      if (maxTotal == null) {
        return cb.conjunction();
      }

      return cb.lessThanOrEqualTo(root.get(Order_.total), maxTotal);
    };
  }

  static Specification<Order> shippingAddressContains(String shippingAddress) {
    return (root, cq, cb) -> {
      if (shippingAddress == null || shippingAddress.isBlank()) {
        return cb.conjunction();
      }

      return cb.like(cb.lower(root.get(Order_.shippingAddress)),
          "%" + shippingAddress.toLowerCase() + "%");
    };
  }

  static Specification<Order> statusEquals(OrderStatus status) {
    return (root, cq, cb) -> {
      if (status == null) {
        return cb.conjunction();
      }

      return cb.equal(root.get(Order_.status), status);
    };
  }

  static Specification<Order> customerIdEquals(Long customerId) {
    return (root, cq, cb) -> {
      if (customerId == null) {
        return cb.conjunction();
      }

      return cb.equal(root.get(Order_.customer).get(Customer_.id), customerId);
    };
  }

  static Specification<Order> createdAtBetween(LocalDateTime startDate, LocalDateTime endDate) {
    return (root, cq, cb) -> {
      if (startDate == null && endDate == null) {
        return cb.conjunction();
      }

      if (startDate != null && endDate != null) {
        return cb.between(root.get(Order_.createdAt), startDate, endDate);
      }

      if (startDate != null) {
        return cb.greaterThanOrEqualTo(root.get(Order_.createdAt), startDate);
      }

      return cb.lessThanOrEqualTo(root.get(Order_.createdAt), endDate);
    };
  }
}
