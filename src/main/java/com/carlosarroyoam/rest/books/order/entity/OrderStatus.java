package com.carlosarroyoam.rest.books.order.entity;

/** Estados posibles de una {@link Order}. */
public enum OrderStatus {
  PENDING,
  CONFIRMED,
  PROCESSING,
  SHIPPED,
  DELIVERED,
  CANCELLED,
  REFUNDED
}
