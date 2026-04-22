package com.carlosarroyoam.rest.books.customer;

import com.carlosarroyoam.rest.books.core.constant.AppMessages;
import com.carlosarroyoam.rest.books.core.dto.PagedResponse;
import com.carlosarroyoam.rest.books.core.dto.PagedResponse.PagedResponseMapper;
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
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class CustomerService {
  private static final Logger log = LoggerFactory.getLogger(CustomerService.class);
  private final CustomerRepository customerRepository;
  private final KeycloakService keycloakService;

  public CustomerService(CustomerRepository customerRepository, KeycloakService keycloakService) {
    this.customerRepository = customerRepository;
    this.keycloakService = keycloakService;
  }

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

  @Transactional(readOnly = true)
  public CustomerResponse findById(Long customerId) {
    Customer customerById = findCustomerByIdOrFail(customerId);
    return CustomerResponseMapper.INSTANCE.toDto(customerById);
  }

  @Transactional
  public CustomerResponse create(CreateCustomerRequest request) {
    if (customerRepository.existsByUsername(request.getUsername())) {
      log.warn(AppMessages.USERNAME_ALREADY_EXISTS_EXCEPTION);
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, AppMessages.USERNAME_ALREADY_EXISTS_EXCEPTION);
    }

    if (customerRepository.existsByEmail(request.getEmail())) {
      log.warn(AppMessages.EMAIL_ALREADY_EXISTS_EXCEPTION);
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, AppMessages.EMAIL_ALREADY_EXISTS_EXCEPTION);
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

  @Transactional
  public void update(Long customerId, UpdateCustomerRequest request) {
    LocalDateTime now = LocalDateTime.now();
    Customer customerById = findCustomerByIdOrFail(customerId);
    customerById.setFirstName(request.getFirstName());
    customerById.setLastName(request.getLastName());
    customerById.setUpdatedAt(now);
    customerRepository.save(customerById);
  }

  @Transactional
  public void deleteById(Long customerId) {
    LocalDateTime now = LocalDateTime.now();
    Customer customerById = findCustomerByIdOrFail(customerId);
    customerById.setStatus(CustomerStatus.DELETED);
    customerById.setUpdatedAt(now);
    customerById.setDeletedAt(now);
    customerRepository.save(customerById);
  }

  private Customer findCustomerByIdOrFail(Long customerId) {
    return customerRepository
        .findById(customerId)
        .orElseThrow(
            () -> {
              log.warn(AppMessages.CUSTOMER_NOT_FOUND_EXCEPTION);
              return new ResponseStatusException(
                  HttpStatus.NOT_FOUND, AppMessages.CUSTOMER_NOT_FOUND_EXCEPTION);
            });
  }
}
