package com.carlosarroyoam.rest.books.shipment;

import com.carlosarroyoam.rest.books.core.constant.AppMessages;
import com.carlosarroyoam.rest.books.shipment.entity.Shipment;
import com.carlosarroyoam.rest.books.shipment.entity.ShipmentStatus;
import org.springframework.data.jpa.domain.Specification;

public class ShipmentSpecification {
  private ShipmentSpecification() {
    throw new IllegalAccessError(AppMessages.ILLEGAL_ACCESS_EXCEPTION);
  }

  static Specification<Shipment> addressContains(String address) {
    return (shipment, cq, cb) -> {
      if (address == null || address.isBlank()) {
        return cb.conjunction();
      }

      return cb.like(cb.lower(shipment.get("address")), "%" + address.toLowerCase() + "%");
    };
  }

  static Specification<Shipment> phoneContains(String phone) {
    return (shipment, cq, cb) -> {
      if (phone == null || phone.isBlank()) {
        return cb.conjunction();
      }

      return cb.like(cb.lower(shipment.get("phone")), "%" + phone.toLowerCase() + "%");
    };
  }

  static Specification<Shipment> attentionNameContains(String attentionName) {
    return (shipment, cq, cb) -> {
      if (attentionName == null || attentionName.isBlank()) {
        return cb.conjunction();
      }

      return cb.like(cb.lower(shipment.get("attentionName")),
          "%" + attentionName.toLowerCase() + "%");
    };
  }

  static Specification<Shipment> statusEquals(ShipmentStatus status) {
    return (shipment, cq, cb) -> {
      if (status == null) {
        return cb.conjunction();
      }

      return cb.equal(shipment.get("status"), status);
    };
  }

  static Specification<Shipment> orderIdEquals(Long orderId) {
    return (shipment, cq, cb) -> {
      if (orderId == null) {
        return cb.conjunction();
      }

      return cb.equal(shipment.get("order").get("id"), orderId);
    };
  }
}