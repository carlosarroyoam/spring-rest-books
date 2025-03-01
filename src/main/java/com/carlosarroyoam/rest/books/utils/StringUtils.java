package com.carlosarroyoam.rest.books.utils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class StringUtils {
  private StringUtils() {
    throw new IllegalAccessError("Illegal access to utility class");
  }

  public static List<String> comaSeparatedToList(String value) {
    if (value.equals(null)) {
      return Collections.emptyList();
    }

    return Arrays.asList(value.replace("\"", "").split(",", -1))
        .stream()
        .map(String::trim)
        .toList();
  }
}
