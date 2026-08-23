package com.carlosarroyoam.rest.books.payment.entity;

/** Métodos de pago admitidos para un {@link Payment}. */
public enum PaymentMethod {
  CASH_ON_DELIVERY,
  CREDIT_CARD,
  DEBIT_CARD,
  BANK_TRANSFER
}
