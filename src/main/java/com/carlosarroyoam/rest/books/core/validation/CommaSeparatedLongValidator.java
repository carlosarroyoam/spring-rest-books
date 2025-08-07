package com.carlosarroyoam.rest.books.core.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.Arrays;

public class CommaSeparatedLongValidator
    implements ConstraintValidator<CommaSeparatedLong, String> {
  @Override
  public boolean isValid(String value, ConstraintValidatorContext context) {
    if (value == null || value.trim().isEmpty()) {
      return true;
    }

    try {
      Arrays.stream(value.split(",")).map(String::trim).forEach(Long::parseLong);
      return true;
    } catch (NumberFormatException e) {
      return false;
    }
  }
}
