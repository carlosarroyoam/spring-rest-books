package com.carlosarroyoam.rest.books.customer;

import com.carlosarroyoam.rest.books.customer.entity.Customer;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

/** Acceso a datos de {@link Customer} mediante Spring Data JPA. */
public interface CustomerRepository
    extends JpaRepository<Customer, Long>, JpaSpecificationExecutor<Customer> {

  /**
   * Busca un cliente por su correo electrónico.
   *
   * @param email correo electrónico del cliente
   * @return el cliente encontrado, si existe
   */
  Optional<Customer> findByEmail(String email);

  /**
   * Busca un cliente por su nombre de usuario.
   *
   * @param username nombre de usuario del cliente
   * @return el cliente encontrado, si existe
   */
  Optional<Customer> findByUsername(String username);

  /**
   * Indica si existe un cliente con el correo electrónico dado.
   *
   * @param email correo electrónico a verificar
   * @return {@code true} si ya existe un cliente con ese correo
   */
  boolean existsByEmail(String email);

  /**
   * Indica si existe un cliente con el nombre de usuario dado.
   *
   * @param username nombre de usuario a verificar
   * @return {@code true} si ya existe un cliente con ese nombre de usuario
   */
  boolean existsByUsername(String username);
}
