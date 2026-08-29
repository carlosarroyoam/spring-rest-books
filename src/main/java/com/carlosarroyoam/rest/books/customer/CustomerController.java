package com.carlosarroyoam.rest.books.customer;

import com.carlosarroyoam.rest.books.core.pagination.PagedResponse;
import com.carlosarroyoam.rest.books.customer.dto.CreateCustomerRequest;
import com.carlosarroyoam.rest.books.customer.dto.CustomerResponse;
import com.carlosarroyoam.rest.books.customer.dto.CustomerSpecs;
import com.carlosarroyoam.rest.books.customer.dto.UpdateCustomerRequest;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * Expone las operaciones REST sobre {@link com.carlosarroyoam.rest.books.customer.entity.Customer}:
 * listado paginado con filtros, consulta por id, alta, actualización y baja lógica.
 */
@RestController
@RequestMapping("/customers")
public class CustomerController {
  private final CustomerService customerService;

  public CustomerController(CustomerService customerService) {
    this.customerService = customerService;
  }

  /**
   * Lista clientes de forma paginada, aplicando los filtros de {@link CustomerSpecs}. Requiere rol
   * {@code App/Admin}.
   *
   * @param customerSpecs filtros opcionales de búsqueda
   * @param pageable configuración de página y orden
   * @return la página de clientes encontrados
   */
  @GetMapping(produces = "application/json")
  @PreAuthorize("hasRole('App/Admin')")
  public ResponseEntity<PagedResponse<CustomerResponse>> findAll(
      @Valid @ModelAttribute CustomerSpecs customerSpecs,
      @PageableDefault(page = 0, size = 25, sort = "id") Pageable pageable) {
    PagedResponse<CustomerResponse> customers = customerService.findAll(customerSpecs, pageable);
    return ResponseEntity.ok(customers);
  }

  /**
   * Busca un cliente por su id. Requiere rol {@code App/Admin}.
   *
   * @param customerId id del cliente a buscar
   * @return el cliente encontrado
   */
  @GetMapping(path = "/{customerId}", produces = "application/json")
  @PreAuthorize("hasRole('App/Admin')")
  public ResponseEntity<CustomerResponse> findById(@PathVariable Long customerId) {
    CustomerResponse customerById = customerService.findById(customerId);
    return ResponseEntity.ok(customerById);
  }

  /**
   * Registra un nuevo cliente. Endpoint público, sin autenticación requerida.
   *
   * @param request datos del cliente a crear
   * @param builder utilizado para construir la URI del recurso creado
   * @return respuesta 201 con la cabecera {@code Location} del cliente creado
   */
  @PostMapping(consumes = "application/json")
  public ResponseEntity<Void> create(
      @Valid @RequestBody CreateCustomerRequest request, UriComponentsBuilder builder) {
    CustomerResponse createdCustomer = customerService.create(request);
    UriComponents uriComponents =
        builder.path("/customers/{customerId}").buildAndExpand(createdCustomer.getId());
    return ResponseEntity.created(uriComponents.toUri()).build();
  }

  /**
   * Actualiza el nombre y apellido de un cliente.
   *
   * @param customerId id del cliente a actualizar
   * @param request nuevos datos del cliente
   */
  @PutMapping(value = "/{customerId}", consumes = "application/json")
  public ResponseEntity<Void> update(
      @PathVariable Long customerId, @Valid @RequestBody UpdateCustomerRequest request) {
    customerService.update(customerId, request);
    return ResponseEntity.noContent().build();
  }

  /**
   * Elimina lógicamente un cliente. Requiere rol {@code App/Admin}.
   *
   * @param customerId id del cliente a eliminar
   */
  @DeleteMapping("/{customerId}")
  @PreAuthorize("hasRole('App/Admin')")
  public ResponseEntity<Void> deleteById(@PathVariable Long customerId) {
    customerService.deleteById(customerId);
    return ResponseEntity.noContent().build();
  }
}
