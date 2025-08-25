package com.carlosarroyoam.rest.books.customer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.carlosarroyoam.rest.books.core.constant.AppMessages;
import com.carlosarroyoam.rest.books.customer.dto.CreateCustomerRequestDto;
import com.carlosarroyoam.rest.books.customer.dto.CustomerDto;
import com.carlosarroyoam.rest.books.customer.dto.CustomerDto.CustomerDtoMapper;
import com.carlosarroyoam.rest.books.customer.dto.CustomerFilterDto;
import com.carlosarroyoam.rest.books.customer.dto.UpdateCustomerRequestDto;
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
  @Mock
  private CustomerRepository customerRepository;

  @Mock
  private KeycloakService keycloakService;

  @InjectMocks
  private CustomerService customerService;

  private Customer customer;

  @BeforeEach
  void setUp() {
    LocalDateTime now = LocalDateTime.now();

    customer = Customer.builder()
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
  @DisplayName("Should return List<CustomerDto> when find all customers")
  void shouldReturnListOfCustomers() {
    List<Customer> customers = List.of(customer);

    when(customerRepository.findAll(ArgumentMatchers.<Specification<Customer>>any(),
        any(Pageable.class))).thenReturn(new PageImpl<>(customers));

    List<CustomerDto> customersDto = customerService.findAll(PageRequest.of(0, 25),
        CustomerFilterDto.builder().build());

    assertThat(customersDto).isNotNull().isNotEmpty().hasSize(1);
    assertThat(customersDto.get(0)).isNotNull();
    assertThat(customersDto.get(0).getId()).isEqualTo(1L);
    assertThat(customersDto.get(0).getFirstName()).isEqualTo("Carlos Alberto");
    assertThat(customersDto.get(0).getLastName()).isEqualTo("Arroyo Martínez");
    assertThat(customersDto.get(0).getEmail()).isEqualTo("carroyom@mail.com");
    assertThat(customersDto.get(0).getUsername()).isEqualTo("carroyom");
    assertThat(customersDto.get(0).getCreatedAt()).isNotNull();
    assertThat(customersDto.get(0).getUpdatedAt()).isNotNull();
  }

  @Test
  @DisplayName("Should return CustomerDto when find customer by id with existing id")
  void shouldReturnWhenFindCustomerByIdWithExistingId() {
    when(customerRepository.findById(any())).thenReturn(Optional.of(customer));

    CustomerDto customerDto = customerService.findById(1L);

    assertThat(customerDto).isNotNull();
    assertThat(customerDto.getId()).isEqualTo(1L);
    assertThat(customerDto.getFirstName()).isEqualTo("Carlos Alberto");
    assertThat(customerDto.getLastName()).isEqualTo("Arroyo Martínez");
    assertThat(customerDto.getEmail()).isEqualTo("carroyom@mail.com");
    assertThat(customerDto.getUsername()).isEqualTo("carroyom");
    assertThat(customerDto.getCreatedAt()).isNotNull();
    assertThat(customerDto.getUpdatedAt()).isNotNull();
  }

  @Test
  @DisplayName("Should throw ResponseStatusException when find a customer by id with non existing id")
  void shouldThrowWhenFindCustomerByIdWithNonExistingId() {
    when(customerRepository.findById(any())).thenReturn(Optional.empty());

    assertThatThrownBy(() -> customerService.findById(1L))
        .isInstanceOf(ResponseStatusException.class)
        .hasMessageContaining(HttpStatus.NOT_FOUND.toString())
        .hasMessageContaining(AppMessages.CUSTOMER_NOT_FOUND_EXCEPTION);
  }

  @Test
  @DisplayName("Should return CustomerDto when create a customer with valid data")
  void shouldReturnWhenCreateCustomerWithValidData() {
    CreateCustomerRequestDto requestDto = CreateCustomerRequestDto.builder()
        .firstName("Cathy Stefania")
        .lastName("Guido Rojas")
        .email("cguidor@mail.com")
        .username("cguidor")
        .build();

    when(customerRepository.existsByUsername(any())).thenReturn(false);
    when(customerRepository.existsByEmail(any())).thenReturn(false);
    when(customerRepository.save(any(Customer.class)))
        .thenReturn(CustomerDtoMapper.INSTANCE.createRequestToEntity(requestDto));

    CustomerDto customerDto = customerService.create(requestDto);

    assertThat(customerDto).isNotNull();
    assertThat(customerDto.getFirstName()).isEqualTo("Cathy Stefania");
    assertThat(customerDto.getLastName()).isEqualTo("Guido Rojas");
    assertThat(customerDto.getEmail()).isEqualTo("cguidor@mail.com");
    assertThat(customerDto.getUsername()).isEqualTo("cguidor");
  }

  @Test
  @DisplayName("Should throw ResponseStatusException when create a customer with existing username")
  void shouldThrowWhenCreateCustomerWithExistingUsername() {
    CreateCustomerRequestDto requestDto = CreateCustomerRequestDto.builder().build();

    when(customerRepository.existsByUsername(any())).thenReturn(true);

    assertThatThrownBy(() -> customerService.create(requestDto))
        .isInstanceOf(ResponseStatusException.class)
        .hasMessageContaining(HttpStatus.BAD_REQUEST.toString())
        .hasMessageContaining(AppMessages.USERNAME_ALREADY_EXISTS_EXCEPTION);
  }

  @Test
  @DisplayName("Should throw ResponseStatusException when create a customer with existing email")
  void shouldThrowWhenCreateCustomerWithExistingEmail() {
    CreateCustomerRequestDto requestDto = CreateCustomerRequestDto.builder().build();

    when(customerRepository.existsByEmail(any())).thenReturn(true);

    assertThatThrownBy(() -> customerService.create(requestDto))
        .isInstanceOf(ResponseStatusException.class)
        .hasMessageContaining(HttpStatus.BAD_REQUEST.toString())
        .hasMessageContaining(AppMessages.EMAIL_ALREADY_EXISTS_EXCEPTION);
  }

  @Test
  @DisplayName("Should update customer with valid data")
  void shouldUpdateCustomerWithValidData() {
    UpdateCustomerRequestDto requestDto = UpdateCustomerRequestDto.builder()
        .firstName("Carlos")
        .lastName("Arroyo")
        .build();

    when(customerRepository.findById(any())).thenReturn(Optional.of(customer));
    when(customerRepository.save(any(Customer.class))).thenReturn(customer);

    customerService.update(1L, requestDto);

    verify(customerRepository).save(customer);
    assertThat(customer.getId()).isEqualTo(1L);
    assertThat(customer.getFirstName()).isEqualTo("Carlos");
    assertThat(customer.getLastName()).isEqualTo("Arroyo");
    assertThat(customer.getEmail()).isEqualTo("carroyom@mail.com");
    assertThat(customer.getUsername()).isEqualTo("carroyom");
    assertThat(customer.getCreatedAt()).isNotNull();
    assertThat(customer.getUpdatedAt()).isNotNull();
  }

  @Test
  @DisplayName("Should throw ResponseStatusException when update customer with non existing id")
  void shouldThrowWhenUpdateCustomerWithInvalidData() {
    UpdateCustomerRequestDto requestDto = UpdateCustomerRequestDto.builder().build();

    when(customerRepository.findById(any())).thenReturn(Optional.empty());

    assertThatThrownBy(() -> customerService.update(1L, requestDto))
        .isInstanceOf(ResponseStatusException.class)
        .hasMessageContaining(HttpStatus.NOT_FOUND.toString())
        .hasMessageContaining(AppMessages.CUSTOMER_NOT_FOUND_EXCEPTION);
  }

  @Test
  @DisplayName("Should deactivate customer with existing id")
  void shouldDeactivateCustomerWithExistingId() {
    when(customerRepository.findById(any())).thenReturn(Optional.of(customer));
    when(customerRepository.save(any(Customer.class))).thenReturn(customer);

    customerService.deleteById(1L);

    verify(customerRepository).save(customer);
  }

  @Test
  @DisplayName("Should throw ResponseStatusException when deactivate customer with non existing id")
  void shouldThrowWhenDeactivateCustomerWithNonExistingId() {
    when(customerRepository.findById(any())).thenReturn(Optional.empty());

    assertThatThrownBy(() -> customerService.deleteById(1L))
        .isInstanceOf(ResponseStatusException.class)
        .hasMessageContaining(HttpStatus.NOT_FOUND.toString())
        .hasMessageContaining(AppMessages.CUSTOMER_NOT_FOUND_EXCEPTION);
  }
}
