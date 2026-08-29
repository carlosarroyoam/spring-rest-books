package com.carlosarroyoam.rest.books.customer;

import com.carlosarroyoam.rest.books.core.constant.AppMessages;
import com.carlosarroyoam.rest.books.core.exception.ResourceAlreadyExistsException;
import com.carlosarroyoam.rest.books.core.exception.ResourceNotFoundException;
import com.carlosarroyoam.rest.books.core.pagination.PagedResponse;
import com.carlosarroyoam.rest.books.core.pagination.PagedResponse.PagedResponseMapper;
import com.carlosarroyoam.rest.books.core.specification.SpecificationBuilder;
import com.carlosarroyoam.rest.books.customer.dto.CreateCustomerRequest;
import com.carlosarroyoam.rest.books.customer.dto.CustomerResponse;
import com.carlosarroyoam.rest.books.customer.dto.CustomerResponse.CustomerResponseMapper;
import com.carlosarroyoam.rest.books.customer.dto.CustomerSpecs;
import com.carlosarroyoam.rest.books.customer.dto.UpdateCustomerRequest;
import com.carlosarroyoam.rest.books.customer.entity.Customer;
import com.carlosarroyoam.rest.books.customer.entity.CustomerStatus;
import com.carlosarroyoam.rest.books.customer.entity.Customer_;
import java.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Contiene la lógica de negocio de {@link Customer}: búsqueda paginada con filtros, alta con
 * validación de correo y nombre de usuario únicos y provisión del usuario en Keycloak a través de
 * {@link KeycloakService}, actualización y baja lógica.
 */
@Service
public class CustomerService {
  private static final Logger log = LoggerFactory.getLogger(CustomerService.class);
  private final CustomerRepository customerRepository;
  private final KeycloakService keycloakService;

  public CustomerService(CustomerRepository customerRepository, KeycloakService keycloakService) {
    this.customerRepository = customerRepository;
    this.keycloakService = keycloakService;
  }

  /**
   * Busca clientes de forma paginada aplicando los filtros de {@link CustomerSpecs}.
   *
   * @param customerSpecs filtros opcionales de búsqueda
   * @param pageable configuración de página y orden
   * @return la página de clientes encontrados
   */
  @Transactional(readOnly = true)
  public PagedResponse<CustomerResponse> findAll(CustomerSpecs customerSpecs, Pageable pageable) {
    Specification<Customer> spec =
        SpecificationBuilder.<Customer>builder()
            .likeIfPresent(root -> root.get(Customer_.firstName), customerSpecs.getFirstName())
            .likeIfPresent(root -> root.get(Customer_.lastName), customerSpecs.getLastName())
            .likeIfPresent(root -> root.get(Customer_.email), customerSpecs.getEmail())
            .likeIfPresent(root -> root.get(Customer_.username), customerSpecs.getUsername())
            .equalsIfPresent(root -> root.get(Customer_.status), customerSpecs.getStatus())
            .build();

    Page<Customer> customers = customerRepository.findAll(spec, pageable);

    return PagedResponseMapper.INSTANCE.toPagedResponse(
        customers.map(CustomerResponseMapper.INSTANCE::toDto));
  }

  /**
   * Busca un cliente por su id.
   *
   * @param customerId id del cliente a buscar
   * @return el cliente encontrado
   */
  @Transactional(readOnly = true)
  public CustomerResponse findById(Long customerId) {
    Customer customerById = findCustomerByIdOrFail(customerId);
    return CustomerResponseMapper.INSTANCE.toDto(customerById);
  }

  /**
   * Crea un cliente con estado {@code ACTIVE} y provisiona su usuario en Keycloak, rechazando
   * nombre de usuario y correo electrónico duplicados. Si la provisión en Keycloak falla (incluido
   * el caso en que ya exista un usuario con el mismo nombre de usuario o correo electrónico), el
   * alta del cliente se revierte por completo.
   *
   * @param request datos del cliente a crear
   * @return el cliente creado
   */
  @Transactional
  public CustomerResponse create(CreateCustomerRequest request) {
    if (customerRepository.existsByUsername(request.getUsername())) {
      log.warn(AppMessages.USERNAME_ALREADY_EXISTS_EXCEPTION);
      throw new ResourceAlreadyExistsException(AppMessages.USERNAME_ALREADY_EXISTS_EXCEPTION);
    }

    if (customerRepository.existsByEmail(request.getEmail())) {
      log.warn(AppMessages.EMAIL_ALREADY_EXISTS_EXCEPTION);
      throw new ResourceAlreadyExistsException(AppMessages.EMAIL_ALREADY_EXISTS_EXCEPTION);
    }

    LocalDateTime now = LocalDateTime.now();
    Customer customer =
        Customer.builder()
            .firstName(request.getFirstName())
            .lastName(request.getLastName())
            .email(request.getEmail())
            .username(request.getUsername())
            .status(CustomerStatus.ACTIVE)
            .createdAt(now)
            .updatedAt(now)
            .build();

    Customer createdCustomer = customerRepository.save(customer);
    keycloakService.createUser(request, createdCustomer.getId());
    return CustomerResponseMapper.INSTANCE.toDto(createdCustomer);
  }

  /**
   * Actualiza el nombre y apellido de un cliente existente.
   *
   * @param customerId id del cliente a actualizar
   * @param request nuevos datos del cliente
   */
  @Transactional
  public void update(Long customerId, UpdateCustomerRequest request) {
    LocalDateTime now = LocalDateTime.now();
    Customer customerById = findCustomerByIdOrFail(customerId);
    customerById.setFirstName(request.getFirstName());
    customerById.setLastName(request.getLastName());
    customerById.setUpdatedAt(now);
    customerRepository.save(customerById);
  }

  /**
   * Marca un cliente como {@code DELETED} y registra la fecha de baja, sin eliminar el registro.
   *
   * @param customerId id del cliente a eliminar
   */
  @Transactional
  public void deleteById(Long customerId) {
    LocalDateTime now = LocalDateTime.now();
    Customer customerById = findCustomerByIdOrFail(customerId);
    customerById.setStatus(CustomerStatus.DELETED);
    customerById.setUpdatedAt(now);
    customerById.setDeletedAt(now);
    customerRepository.save(customerById);
  }

  /**
   * Busca un cliente por id o lanza {@code 404 Not Found} si no existe.
   *
   * @param customerId id del cliente a buscar
   * @return el cliente encontrado
   */
  private Customer findCustomerByIdOrFail(Long customerId) {
    return customerRepository
        .findById(customerId)
        .orElseThrow(
            () -> {
              log.warn(AppMessages.CUSTOMER_NOT_FOUND_EXCEPTION);
              return new ResourceNotFoundException(AppMessages.CUSTOMER_NOT_FOUND_EXCEPTION);
            });
  }
}
