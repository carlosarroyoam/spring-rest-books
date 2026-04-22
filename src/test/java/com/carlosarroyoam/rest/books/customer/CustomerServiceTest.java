package com.carlosarroyoam.rest.books.customer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.carlosarroyoam.rest.books.core.constant.AppMessages;
import com.carlosarroyoam.rest.books.core.dto.PagedResponse;
import com.carlosarroyoam.rest.books.customer.dto.CreateCustomerRequest;
import com.carlosarroyoam.rest.books.customer.dto.CustomerResponse;
import com.carlosarroyoam.rest.books.customer.dto.CustomerSpecs;
import com.carlosarroyoam.rest.books.customer.dto.UpdateCustomerRequest;
import com.carlosarroyoam.rest.books.customer.entity.Customer;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class CustomerServiceTest {
  @Mock private CustomerRepository customerRepository;

  @Mock private KeycloakService keycloakService;

  @InjectMocks private CustomerService customerService;

  private Customer customer;

  @BeforeEach
  void setUp() {
    LocalDateTime now = LocalDateTime.now();

    customer =
        Customer.builder()
            .id(1L)
            .firstName("Carlos Alberto")
            .lastName("Arroyo Martínez")
            .email("carroyom@mail.com")
            .username("carroyom")
            .createdAt(now)
            .updatedAt(now)
            .build();
  }

  @Test
  @DisplayName("Given customers exist, when find all, then returns paged customers")
  void givenCustomersExist_whenFindAll_thenReturnsPagedCustomers() {
    Pageable pageable = PageRequest.of(0, 25);
    List<Customer> customers = List.of(customer);

    when(customerRepository.findAll(
            ArgumentMatchers.<Specification<Customer>>any(), any(Pageable.class)))
        .thenReturn(new PageImpl<>(customers, pageable, customers.size()));

    PagedResponse<CustomerResponse> response =
        customerService.findAll(CustomerSpecs.builder().build(), PageRequest.of(0, 25));

    assertThat(response).isNotNull();
    assertThat(response.getItems()).isNotNull().hasSize(1);
    assertThat(response.getPagination()).isNotNull();
    assertThat(response.getPagination().getPage()).isZero();
    assertThat(response.getPagination().getSize()).isEqualTo(25);
    assertThat(response.getPagination().getTotalItems()).isEqualTo(1);
    assertThat(response.getPagination().getTotalPages()).isEqualTo(1);
  }

  @Test
  @DisplayName("Given customer exists, when find by id, then returns customer")
  void givenCustomerExists_whenFindById_thenReturnsCustomer() {
    when(customerRepository.findById(anyLong())).thenReturn(Optional.of(customer));

    CustomerResponse customerResponse = customerService.findById(1L);

    assertThat(customerResponse).isNotNull();
    assertThat(customerResponse.getId()).isEqualTo(1L);
  }

  @Test
  @DisplayName("Given customer does not exist, when find by id, then throws not found exception")
  void givenCustomerDoesNotExist_whenFindById_thenThrowsNotFoundException() {
    when(customerRepository.findById(any())).thenReturn(Optional.empty());

    assertThatThrownBy(() -> customerService.findById(1L))
        .isInstanceOf(ResponseStatusException.class)
        .hasMessageContaining(HttpStatus.NOT_FOUND.toString())
        .hasMessageContaining(AppMessages.CUSTOMER_NOT_FOUND_EXCEPTION);
  }

  @Test
  @DisplayName("Given valid customer data, when create, then returns created customer")
  void givenValidCustomerData_whenCreate_thenReturnsCreatedCustomer() {
    CreateCustomerRequest request =
        CreateCustomerRequest.builder()
            .firstName("Cathy Stefania")
            .lastName("Guido Rojas")
            .email("cguidor@mail.com")
            .username("cguidor")
            .build();

    Customer savedCustomer =
        Customer.builder().firstName("Cathy Stefania").lastName("Guido Rojas").build();

    when(customerRepository.existsByUsername(anyString())).thenReturn(false);
    when(customerRepository.existsByEmail(anyString())).thenReturn(false);
    when(customerRepository.save(any(Customer.class))).thenReturn(savedCustomer);

    CustomerResponse customerResponse = customerService.create(request);

    assertThat(customerResponse).isNotNull();
    assertThat(customerResponse.getFirstName()).isEqualTo("Cathy Stefania");
    assertThat(customerResponse.getLastName()).isEqualTo("Guido Rojas");
  }

  @Test
  @DisplayName(
      "Given customer with existing username, when create, then throws bad request exception")
  void givenCustomerWithExistingUsername_whenCreate_thenThrowsBadRequestException() {
    CreateCustomerRequest request = CreateCustomerRequest.builder().build();

    when(customerRepository.existsByUsername(any())).thenReturn(true);

    assertThatThrownBy(() -> customerService.create(request))
        .isInstanceOf(ResponseStatusException.class)
        .hasMessageContaining(HttpStatus.BAD_REQUEST.toString())
        .hasMessageContaining(AppMessages.USERNAME_ALREADY_EXISTS_EXCEPTION);
  }

  @Test
  @DisplayName("Given customer with existing email, when create, then throws bad request exception")
  void givenCustomerWithExistingEmail_whenCreate_thenThrowsBadRequestException() {
    CreateCustomerRequest request = CreateCustomerRequest.builder().build();

    when(customerRepository.existsByEmail(any())).thenReturn(true);

    assertThatThrownBy(() -> customerService.create(request))
        .isInstanceOf(ResponseStatusException.class)
        .hasMessageContaining(HttpStatus.BAD_REQUEST.toString())
        .hasMessageContaining(AppMessages.EMAIL_ALREADY_EXISTS_EXCEPTION);
  }

  @Test
  @DisplayName("Given customer exists, when update with valid data, then updates customer")
  void givenCustomerExists_whenUpdateWithValidData_thenUpdatesCustomer() {
    UpdateCustomerRequest request = UpdateCustomerRequest.builder().firstName("Carlos").build();

    Customer updatedCustomer = Customer.builder().firstName("Carlos").build();

    when(customerRepository.findById(any())).thenReturn(Optional.of(customer));
    when(customerRepository.save(any(Customer.class))).thenReturn(updatedCustomer);

    customerService.update(1L, request);

    verify(customerRepository).save(customer);
    assertThat(customer.getId()).isEqualTo(1L);
    assertThat(customer.getFirstName()).isEqualTo("Carlos");
  }

  @Test
  @DisplayName("Given customer does not exist, when update, then throws not found exception")
  void givenCustomerDoesNotExist_whenUpdate_thenThrowsNotFoundException() {
    UpdateCustomerRequest request = UpdateCustomerRequest.builder().build();

    when(customerRepository.findById(any())).thenReturn(Optional.empty());

    assertThatThrownBy(() -> customerService.update(1L, request))
        .isInstanceOf(ResponseStatusException.class)
        .hasMessageContaining(HttpStatus.NOT_FOUND.toString())
        .hasMessageContaining(AppMessages.CUSTOMER_NOT_FOUND_EXCEPTION);
  }

  @Test
  @DisplayName("Given customer exists, when deactivate, then deactivates customer")
  void givenCustomerExists_whenDeactivate_thenDeactivatesCustomer() {
    when(customerRepository.findById(any())).thenReturn(Optional.of(customer));
    when(customerRepository.save(any(Customer.class))).thenReturn(customer);

    customerService.deleteById(1L);

    verify(customerRepository).save(customer);
  }

  @Test
  @DisplayName("Given customer does not exist, when deactivate, then throws not found exception")
  void givenCustomerDoesNotExist_whenDeactivate_thenThrowsNotFoundException() {
    when(customerRepository.findById(any())).thenReturn(Optional.empty());

    assertThatThrownBy(() -> customerService.deleteById(1L))
        .isInstanceOf(ResponseStatusException.class)
        .hasMessageContaining(HttpStatus.NOT_FOUND.toString())
        .hasMessageContaining(AppMessages.CUSTOMER_NOT_FOUND_EXCEPTION);
  }
}
