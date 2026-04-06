package com.carlosarroyoam.rest.books.core.specification;

import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import org.springframework.data.jpa.domain.Specification;

public class SpecificationBuilder<T> {
  private final List<Specification<T>> specs = new ArrayList<>();

  public static <T> SpecificationBuilder<T> builder() {
    return new SpecificationBuilder<>();
  }

  public Specification<T> build() {
    return specs.stream().reduce(Specification::and).orElse((root, query, cb) -> cb.conjunction());
  }

  public <Y> SpecificationBuilder<T> equalsIfPresent(Function<Root<T>, Path<Y>> path, Y value) {
    if (value != null) {
      specs.add((root, query, cb) -> cb.equal(path.apply(root), value));
    }
    return this;
  }

  public SpecificationBuilder<T> likeIfPresent(Function<Root<T>, Path<String>> path, String value) {
    if (value != null && !value.isBlank()) {
      specs.add((root, query, cb) -> cb.like(cb.lower(path.apply(root)),
          "%" + value.toLowerCase() + "%"));
    }
    return this;
  }

  public <Y extends Comparable<? super Y>> SpecificationBuilder<T> betweenIfPresent(
      Function<Root<T>, Path<Y>> path, Y min, Y max) {
    if (min != null && max != null) {
      specs.add((root, query, cb) -> cb.between(path.apply(root), min, max));
    } else if (min != null) {
      specs.add((root, query, cb) -> cb.greaterThanOrEqualTo(path.apply(root), min));
    } else if (max != null) {
      specs.add((root, query, cb) -> cb.lessThanOrEqualTo(path.apply(root), max));
    }
    return this;
  }

  public <Y> SpecificationBuilder<T> inIfPresent(Function<Root<T>, Path<Y>> path, List<Y> values) {
    if (values != null && !values.isEmpty()) {
      specs.add((root, query, cb) -> path.apply(root).in(values));
    }
    return this;
  }

  public SpecificationBuilder<T> inIdsIfPresent(Function<Root<T>, Path<Long>> path, String csvIds) {
    if (csvIds != null && !csvIds.isBlank()) {

      List<Long> ids = Arrays.stream(csvIds.split(","))
          .map(String::trim)
          .filter(s -> !s.isEmpty())
          .map(Long::parseLong)
          .toList();

      if (!ids.isEmpty()) {
        specs.add((root, query, cb) -> path.apply(root).in(ids));
      }
    }

    return this;
  }
}
