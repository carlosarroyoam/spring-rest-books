package com.carlosarroyoam.rest.books.core.specification;

import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Root;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import org.springframework.data.jpa.domain.Specification;

/**
 * Construye instancias de {@link Specification} combinando condiciones opcionales con AND,
 * omitiendo aquellas cuyo valor es {@code null} o está vacío.
 *
 * @param <T> tipo de la entidad sobre la que se construye la especificación
 */
public class SpecificationBuilder<T> {
  private final List<Specification<T>> specs = new ArrayList<>();

  /**
   * Crea una nueva instancia del builder, sin condiciones acumuladas.
   *
   * @param <T> tipo de la entidad sobre la que se construye la especificación
   * @return un builder vacío
   */
  public static <T> SpecificationBuilder<T> builder() {
    return new SpecificationBuilder<>();
  }

  /**
   * Combina todas las condiciones acumuladas con AND.
   *
   * @return la especificación resultante, o una condición siempre verdadera si no se agregó
   *     ninguna condición
   */
  public Specification<T> build() {
    return specs.stream().reduce(Specification::and).orElse((root, query, cb) -> cb.conjunction());
  }

  /**
   * Agrega una condición de igualdad si el valor no es {@code null}.
   *
   * @param path función que obtiene el atributo a comparar
   * @param value valor a comparar; si es {@code null} no se agrega ninguna condición
   * @return este builder
   */
  public <Y> SpecificationBuilder<T> equalsIfPresent(Function<Root<T>, Path<Y>> path, Y value) {
    if (value != null) {
      specs.add((root, query, cb) -> cb.equal(path.apply(root), value));
    }
    return this;
  }

  /**
   * Agrega una condición {@code LIKE} case-insensitive si el valor no es {@code null} ni está en
   * blanco.
   *
   * @param path función que obtiene el atributo de texto a comparar
   * @param value valor a buscar como subcadena
   * @return este builder
   */
  public SpecificationBuilder<T> likeIfPresent(Function<Root<T>, Path<String>> path, String value) {
    if (value != null && !value.isBlank()) {
      specs.add(
          (root, query, cb) ->
              cb.like(cb.lower(path.apply(root)), "%" + value.toLowerCase() + "%"));
    }
    return this;
  }

  /**
   * Agrega una condición de rango entre {@code min} y {@code max}. Si solo se define uno de los
   * dos límites, agrega la condición equivalente de mayor-o-igual o menor-o-igual.
   *
   * @param path función que obtiene el atributo a comparar
   * @param min límite inferior del rango, opcional
   * @param max límite superior del rango, opcional
   * @return este builder
   */
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

  /**
   * Agrega una condición de rango de fechas, cubriendo desde el inicio del día de {@code start}
   * hasta el final del día de {@code end}.
   *
   * @param path función que obtiene el atributo de fecha/hora a comparar
   * @param start fecha de inicio del rango, opcional
   * @param end fecha de fin del rango, opcional
   * @return este builder
   */
  public SpecificationBuilder<T> betweenDatesIfPresent(
      Function<Root<T>, Path<LocalDateTime>> path, LocalDate start, LocalDate end) {
    if (start != null) {
      specs.add(
          (root, query, cb) -> cb.greaterThanOrEqualTo(path.apply(root), start.atStartOfDay()));
    }

    if (end != null) {
      specs.add(
          (root, query, cb) -> cb.lessThanOrEqualTo(path.apply(root), end.atTime(23, 59, 59)));
    }

    return this;
  }

  /**
   * Agrega una condición {@code IN} si la lista de valores no es {@code null} ni está vacía.
   *
   * @param path función que obtiene el atributo a comparar
   * @param values valores permitidos
   * @return este builder
   */
  public <Y> SpecificationBuilder<T> inIfPresent(Function<Root<T>, Path<Y>> path, List<Y> values) {
    if (values != null && !values.isEmpty()) {
      specs.add((root, query, cb) -> path.apply(root).in(values));
    }
    return this;
  }
}
