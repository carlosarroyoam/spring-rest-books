package com.carlosarroyoam.rest.books.constant;

public class AppMessages {
  public static final String ILLEGAL_ACCESS_EXCEPTION = "Illegal access to utility class";

  public static final String BOOK_NOT_FOUND_EXCEPTION = "Book not found";
  public static final String ISBN_ALREADY_EXISTS_EXCEPTION = "ISBN already exists";

  public static final String AUTHOR_NOT_FOUND_EXCEPTION = "Author not found";

  public static final String USER_NOT_FOUND_EXCEPTION = "User not found";
  public static final String EMAIL_ALREADY_EXISTS_EXCEPTION = "Email already exists";
  public static final String USERNAME_ALREADY_EXISTS_EXCEPTION = "Username already exists";

  private AppMessages() {
    throw new IllegalAccessError(ILLEGAL_ACCESS_EXCEPTION);
  }
}
