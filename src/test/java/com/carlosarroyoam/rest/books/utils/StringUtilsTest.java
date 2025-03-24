package com.carlosarroyoam.rest.books.utils;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class StringUtilsTest {
  @Test
  @DisplayName("Should return List<String> when comma separated to list with values")
  public void shouldReturnEmtyListWhenCommaSeparatedToListWithValues() {
    List<String> result = StringUtils.commaSeparatedToList("value1, value2, value3");

    assertThat(result).isNotNull();
    assertThat(result).isNotEmpty();
    assertThat(result).size().isEqualTo(3);
  }

  @Test
  @DisplayName("Should return List<String> when comma separated to list with unquoted values")
  public void shouldReturnEmtyListWhenCommaSeparatedToListWithUnquotedValues() {
    List<String> result = StringUtils.commaSeparatedToList("\"value1\", \"value2\", \"value3\"");

    assertThat(result).isNotNull();
    assertThat(result).isNotEmpty();
    assertThat(result).size().isEqualTo(3);
  }

  @Test
  @DisplayName("Should return empty List<String> when comma separated to list with emtpy string")
  public void shouldReturnEmtyListWhenCommaSeparatedToListWithEmptyString() {
    List<String> result = StringUtils.commaSeparatedToList("");

    assertThat(result).isNotNull();
    assertThat(result).isEmpty();
  }

  @Test
  @DisplayName("Should return empty List<String> when comma separated to list with nulls")
  public void shouldReturnEmtyListWhenCommaSeparatedToListWithNulls() {
    List<String> result = StringUtils.commaSeparatedToList(null);

    assertThat(result).isNotNull();
    assertThat(result).isEmpty();
  }
}
