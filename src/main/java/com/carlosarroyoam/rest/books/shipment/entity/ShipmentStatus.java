package com.carlosarroyoam.rest.books.shipment.entity;

/** Estados posibles de un {@link Shipment}. */
public enum ShipmentStatus {
  PENDING,
  SHIPPED,
  DELIVERED,
  RETURNED,
  CANCELLED
}
