package com.carlosarroyoam.rest.books.core.constant;

/**
 * Constantes centralizadas con las claves de los mensajes de error usados en las excepciones de la
 * API, reutilizadas entre los distintos servicios para mantener consistencia en las respuestas de
 * error. El texto de cada clave vive en {@code messages.properties} y lo resuelve {@link
 * com.carlosarroyoam.rest.books.core.exception.GlobalExceptionHandler}.
 */
public class MessageKeys {
  public static final String BOOK_NOT_FOUND_EXCEPTION = "book.not-found";
  public static final String ISBN_ALREADY_EXISTS_EXCEPTION = "book.isbn.already-exists";

  public static final String AUTHOR_NOT_FOUND_EXCEPTION = "author.not-found";

  public static final String CUSTOMER_NOT_FOUND_EXCEPTION = "customer.not-found";
  public static final String EMAIL_ALREADY_EXISTS_EXCEPTION = "customer.email.already-exists";
  public static final String USERNAME_ALREADY_EXISTS_EXCEPTION = "customer.username.already-exists";
  public static final String USER_NOT_CREATED_EXCEPTION = "customer.user-not-created";

  public static final String CART_NOT_FOUND_EXCEPTION = "cart.not-found";
  public static final String CART_ITEM_NOT_FOUND_EXCEPTION = "cart.item.not-found";

  public static final String ORDER_NOT_FOUND_EXCEPTION = "order.not-found";

  public static final String PAYMENT_NOT_FOUND_EXCEPTION = "payment.not-found";
  public static final String PAYMENT_ALREADY_EXISTS_EXCEPTION = "payment.already-exists";

  public static final String SHIPMENT_NOT_FOUND_EXCEPTION = "shipment.not-found";

  /** Constructor privado que impide instanciar la clase. */
  private MessageKeys() {
    throw new IllegalAccessError(CommonConstants.ILLEGAL_ACCESS_ERROR);
  }
}
