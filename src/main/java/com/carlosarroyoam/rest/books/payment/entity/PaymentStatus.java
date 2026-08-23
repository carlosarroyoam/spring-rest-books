package com.carlosarroyoam.rest.books.payment.entity;

/** Estados posibles de un {@link Payment}. */
public enum PaymentStatus {
  PENDING,
  COMPLETED,
  FAILED,
  CANCELLED,
  REFUNDED
}
