package com.carlosarroyoam.rest.books.core.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Constraint(validatedBy = CommaSeparatedLongValidator.class)
@Target({ ElementType.FIELD, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface CommaSeparatedLong {
  String message() default "Invalid comma-separated long values";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};
}
