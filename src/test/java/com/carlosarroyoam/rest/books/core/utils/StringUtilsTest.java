package com.carlosarroyoam.rest.books.core.utils;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class StringUtilsTest {
  @Test
  @DisplayName("Given comma-separated values, when convert to list, then returns list with values")
  void givenCommaSeparatedValues_whenConvertToList_thenReturnsListWithValues() {
    List<String> result = StringUtils.commaSeparatedToList("value1, value2, value3");

    assertThat(result).isNotNull().isNotEmpty().hasSize(3);
  }

  @Test
  @DisplayName("Given unquoted values, when convert to list, then returns list with values")
  void givenUnquotedValues_whenConvertToList_thenReturnsListWithValues() {
    List<String> result = StringUtils.commaSeparatedToList("\"value1\", \"value2\", \"value3\"");

    assertThat(result).isNotNull().isNotEmpty().hasSize(3);
  }

  @Test
  @DisplayName("Given empty string, when convert to list, then returns empty list")
  void givenEmptyString_whenConvertToList_thenReturnsEmptyList() {
    List<String> result = StringUtils.commaSeparatedToList("");

    assertThat(result).isNotNull().isEmpty();
  }

  @Test
  @DisplayName("Given null input, when convert to list, then returns empty list")
  void givenNullInput_whenConvertToList_thenReturnsEmptyList() {
    List<String> result = StringUtils.commaSeparatedToList(null);

    assertThat(result).isNotNull().isEmpty();
  }
}
