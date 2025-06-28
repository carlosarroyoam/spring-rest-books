package com.carlosarroyoam.rest.books.utils;

import com.carlosarroyoam.rest.books.constant.AppMessages;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class StringUtils {
  private StringUtils() {
    throw new IllegalAccessError(AppMessages.ILLEGAL_ACCESS_EXCEPTION);
  }

  public static List<String> commaSeparatedToList(String input) {
    return Optional.ofNullable(input)
        .filter(value -> !value.isEmpty())
        .map(value -> Arrays.stream(value.replace("\"", "")
            .split(",", -1))
            .map(String::trim)
            .toList())
        .orElse(Collections.emptyList());
  }
}
