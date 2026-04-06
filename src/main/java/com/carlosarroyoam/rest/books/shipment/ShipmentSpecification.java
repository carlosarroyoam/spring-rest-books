package com.carlosarroyoam.rest.books.shipment;

import com.carlosarroyoam.rest.books.core.constant.AppMessages;
import com.carlosarroyoam.rest.books.order.entity.Order_;
import com.carlosarroyoam.rest.books.shipment.entity.Shipment;
import com.carlosarroyoam.rest.books.shipment.entity.ShipmentStatus;
import com.carlosarroyoam.rest.books.shipment.entity.Shipment_;
import org.springframework.data.jpa.domain.Specification;

public class ShipmentSpecification {
  private ShipmentSpecification() {
    throw new IllegalAccessError(AppMessages.ILLEGAL_ACCESS_EXCEPTION);
  }

  static Specification<Shipment> addressContains(String address) {
    return (root, cq, cb) -> {
      if (address == null || address.isBlank()) {
        return cb.conjunction();
      }

      return cb.like(cb.lower(root.get(Shipment_.address)), "%" + address.toLowerCase() + "%");
    };
  }

  static Specification<Shipment> phoneContains(String phone) {
    return (root, cq, cb) -> {
      if (phone == null || phone.isBlank()) {
        return cb.conjunction();
      }

      return cb.like(cb.lower(root.get(Shipment_.phone)), "%" + phone.toLowerCase() + "%");
    };
  }

  static Specification<Shipment> attentionNameContains(String attentionName) {
    return (root, cq, cb) -> {
      if (attentionName == null || attentionName.isBlank()) {
        return cb.conjunction();
      }

      return cb.like(cb.lower(root.get(Shipment_.attentionName)),
          "%" + attentionName.toLowerCase() + "%");
    };
  }

  static Specification<Shipment> statusEquals(ShipmentStatus status) {
    return (root, cq, cb) -> {
      if (status == null) {
        return cb.conjunction();
      }

      return cb.equal(root.get(Shipment_.status), status);
    };
  }

  static Specification<Shipment> orderIdEquals(Long orderId) {
    return (root, cq, cb) -> {
      if (orderId == null) {
        return cb.conjunction();
      }

      return cb.equal(root.get(Shipment_.order).get(Order_.id), orderId);
    };
  }
}