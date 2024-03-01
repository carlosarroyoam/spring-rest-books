package com.carlosarroyoam.rest.books.constant;

public class AppMessages {

	public static final String STANDARD_ILLEGAL_ACCESS_EXCEPTION_MESSAGE_UTILITY_CLASS = "Illegal access to utility class";

	public static final String BOOK_NOT_FOUND_EXCEPTION = "Book not found";

	public static final String AUTHOR_NOT_FOUND_EXCEPTION = "Author not found";

	public static final String USER_NOT_FOUND_EXCEPTION = "User not found";
	public static final String EMAIL_ALREADY_EXISTS_EXCEPTION = "Email already exists";
	public static final String USERNAME_ALREADY_EXISTS_EXCEPTION = "Username already exists";

	public static final String UNAUTHORIZED_CREDENTIALS_EXCEPTION = "Incorrect username or password";
	public static final String PASSWORDS_NOT_MATCH_EXCEPTION = "Passwords doesn't match";

	private AppMessages() {
		throw new IllegalAccessError(STANDARD_ILLEGAL_ACCESS_EXCEPTION_MESSAGE_UTILITY_CLASS);
	}

}