package com.carlosarroyoam.rest.books.core.constant;

public class AppMessages {
  public static final String ILLEGAL_ACCESS_EXCEPTION = "Illegal access to utility class";

  public static final String BOOK_NOT_FOUND_EXCEPTION = "Book not found";
  public static final String ISBN_ALREADY_EXISTS_EXCEPTION = "ISBN already exists";

  public static final String AUTHOR_NOT_FOUND_EXCEPTION = "Author not found";

  public static final String USER_NOT_FOUND_EXCEPTION = "User not found";
  public static final String EMAIL_ALREADY_EXISTS_EXCEPTION = "Email already exists";
  public static final String USERNAME_ALREADY_EXISTS_EXCEPTION = "Username already exists";

  public static final String SHOPPING_CART_NOT_FOUND_EXCEPTION = "Shopping cart not found";
  public static final String CART_ITEM_NOT_FOUND_EXCEPTION = "Cart item not found";

  private AppMessages() {
    throw new IllegalAccessError(ILLEGAL_ACCESS_EXCEPTION);
  }
}
