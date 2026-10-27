package com.carlosarroyoam.rest.books.core.constant;

/** Contiene las constantes de uso general de la aplicación. No es instanciable. */
public class CommonConstants {
  public static final String ILLEGAL_ACCESS_ERROR = "Illegal access to utility class";

  /** Constructor privado que impide instanciar la clase. */
  private CommonConstants() {
    throw new IllegalAccessError(ILLEGAL_ACCESS_ERROR);
  }
}
