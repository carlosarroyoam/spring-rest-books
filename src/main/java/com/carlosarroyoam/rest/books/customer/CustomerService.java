package com.carlosarroyoam.rest.books.customer;

import com.carlosarroyoam.rest.books.core.constant.AppMessages;
import com.carlosarroyoam.rest.books.core.dto.PagedResponseDto;
import com.carlosarroyoam.rest.books.core.dto.PagedResponseDto.PagedResponseDtoMapper;
import com.carlosarroyoam.rest.books.customer.dto.CreateCustomerRequestDto;
import com.carlosarroyoam.rest.books.customer.dto.CustomerDto;
import com.carlosarroyoam.rest.books.customer.dto.CustomerDto.CustomerDtoMapper;
import com.carlosarroyoam.rest.books.customer.dto.CustomerSpecsDto;
import com.carlosarroyoam.rest.books.customer.dto.UpdateCustomerRequestDto;
import com.carlosarroyoam.rest.books.customer.entity.Customer;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
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

  public PagedResponseDto<CustomerDto> findAll(Pageable pageable, CustomerSpecsDto customerSpecs) {
    Specification<Customer> spec = Specification.unrestricted();
    spec = spec.and(CustomerSpecification.firstNameContains(customerSpecs.getFirstName()))
        .and(CustomerSpecification.lastNameContains(customerSpecs.getLastName()))
        .and(CustomerSpecification.emailContains(customerSpecs.getEmail()))
        .and(CustomerSpecification.usernameContains(customerSpecs.getUsername()));

    Page<Customer> customers = customerRepository.findAll(spec, pageable);

    return PagedResponseDtoMapper.INSTANCE
        .toPagedResponseDto(customers.map(CustomerDtoMapper.INSTANCE::toDto));
  }

  public CustomerDto findById(Long customerId) {
    Customer customerById = findCustomerEntityById(customerId);
    return CustomerDtoMapper.INSTANCE.toDto(customerById);
  }

  @Transactional
  public CustomerDto create(CreateCustomerRequestDto requestDto) {
    if (Boolean.TRUE.equals(customerRepository.existsByUsername(requestDto.getUsername()))) {
      log.warn(AppMessages.USERNAME_ALREADY_EXISTS_EXCEPTION);
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
          AppMessages.USERNAME_ALREADY_EXISTS_EXCEPTION);
    }

    if (Boolean.TRUE.equals(customerRepository.existsByEmail(requestDto.getEmail()))) {
      log.warn(AppMessages.EMAIL_ALREADY_EXISTS_EXCEPTION);
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
          AppMessages.EMAIL_ALREADY_EXISTS_EXCEPTION);
    }

    LocalDateTime now = LocalDateTime.now();
    Customer customer = CustomerDtoMapper.INSTANCE.toEntity(requestDto);
    customer.setCreatedAt(now);
    customer.setUpdatedAt(now);

    Customer createdCustomer = customerRepository.save(customer);

    keycloakService.createUser(requestDto, createdCustomer.getId());

    return CustomerDtoMapper.INSTANCE.toDto(createdCustomer);
  }

  @Transactional
  public void update(Long customerId, UpdateCustomerRequestDto requestDto) {
    Customer customerById = findCustomerEntityById(customerId);

    customerById.setFirstName(requestDto.getFirstName());
    customerById.setLastName(requestDto.getLastName());
    customerById.setUpdatedAt(LocalDateTime.now());
    customerRepository.save(customerById);
  }

  @Transactional
  public void deleteById(Long customerId) {
    Customer customerById = findCustomerEntityById(customerId);

    LocalDateTime now = LocalDateTime.now();
    customerById.setDeletedAt(now);
    customerById.setUpdatedAt(now);
    customerRepository.save(customerById);
  }

  private Customer findCustomerEntityById(Long customerId) {
    return customerRepository.findById(customerId).orElseThrow(() -> {
      log.warn(AppMessages.CUSTOMER_NOT_FOUND_EXCEPTION);
      return new ResponseStatusException(HttpStatus.NOT_FOUND,
          AppMessages.CUSTOMER_NOT_FOUND_EXCEPTION);
    });
  }
}
