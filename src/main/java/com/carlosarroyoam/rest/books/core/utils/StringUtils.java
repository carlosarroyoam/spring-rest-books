package com.carlosarroyoam.rest.books.core.utils;

import com.carlosarroyoam.rest.books.core.constant.AppMessages;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

public class StringUtils {
  private StringUtils() {
    throw new IllegalAccessError(AppMessages.ILLEGAL_ACCESS_EXCEPTION);
  }

  public static List<String> commaSeparatedToList(String input) {
    return Optional.ofNullable(input)
        .filter(Predicate.not(String::isEmpty))
        .map(value -> Arrays.stream(value.replace("\"", "").split(",", -1))
            .map(String::trim)
            .toList())
        .orElse(Collections.emptyList());
  }
}
