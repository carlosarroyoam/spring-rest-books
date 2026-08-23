package com.carlosarroyoam.rest.books.core.constant;

/** Nombres de los claims personalizados presentes en el JWT emitido por Keycloak. */
public class CustomClaimNames {
  public static final String CUSTOMER_ID = "customer_id";

  /** Constructor privado: clase de constantes no instanciable. */
  private CustomClaimNames() {
    throw new IllegalAccessError(AppMessages.ILLEGAL_ACCESS_EXCEPTION);
  }
}
