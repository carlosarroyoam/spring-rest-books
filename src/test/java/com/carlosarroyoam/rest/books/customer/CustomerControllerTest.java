package com.carlosarroyoam.rest.books.customer;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.carlosarroyoam.rest.books.core.constant.MessageKeys;
import com.carlosarroyoam.rest.books.core.exception.ExceptionLogger;
import com.carlosarroyoam.rest.books.core.exception.GlobalExceptionHandler;
import com.carlosarroyoam.rest.books.core.exception.ProblemDetailFactory;
import com.carlosarroyoam.rest.books.core.exception.ResourceNotFoundException;
import com.carlosarroyoam.rest.books.core.pagination.PagedResponse;
import com.carlosarroyoam.rest.books.core.pagination.PaginationResponse;
import com.carlosarroyoam.rest.books.customer.dto.CreateCustomerRequest;
import com.carlosarroyoam.rest.books.customer.dto.CustomerResponse;
import com.carlosarroyoam.rest.books.customer.dto.CustomerSpecs;
import com.carlosarroyoam.rest.books.customer.dto.UpdateCustomerRequest;
import com.carlosarroyoam.rest.books.support.testutils.TestMessageSources;
import com.carlosarroyoam.rest.books.support.testutils.TestObjectMappers;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
class CustomerControllerTest {
  @Mock private CustomerService customerService;
  @InjectMocks private CustomerController customerController;

  private ObjectMapper mapper;
  private MockMvc mockMvc;

  @BeforeEach
  void setup() {
    mapper = TestObjectMappers.snakeCase();

    mockMvc =
        MockMvcBuilders.standaloneSetup(customerController)
            .setControllerAdvice(
                new GlobalExceptionHandler(
                    new ProblemDetailFactory(),
                    new ExceptionLogger(),
                    TestMessageSources.messages()))
            .setMessageConverters(
                new MappingJackson2HttpMessageConverter(TestObjectMappers.snakeCase()))
            .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
            .build();
  }

  @Test
  @DisplayName(
      "GET /customers - Given customers exist, when find all, then returns paged customers")
  void givenCustomersExist_whenFindAllCustomers_thenReturnsPagedCustomers() throws Exception {
    PagedResponse<CustomerResponse> pagedResponse =
        PagedResponse.<CustomerResponse>builder()
            .items(List.of(CustomerResponse.builder().id(1L).build()))
            .pagination(
                PaginationResponse.builder().page(0).size(25).totalItems(1).totalPages(1).build())
            .build();

    when(customerService.findAll(any(CustomerSpecs.class), any(Pageable.class)))
        .thenReturn(pagedResponse);

    mockMvc
        .perform(
            get("/customers")
                .queryParam("page", "0")
                .queryParam("size", "25")
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.items").isArray())
        .andExpect(jsonPath("$.items.length()").value(1))
        .andExpect(jsonPath("$.items[0].id").value(1))
        .andExpect(jsonPath("$.pagination.page").value(0))
        .andExpect(jsonPath("$.pagination.size").value(25))
        .andExpect(jsonPath("$.pagination.total_items").value(1))
        .andExpect(jsonPath("$.pagination.total_pages").value(1));
  }

  @Test
  @DisplayName(
      "GET /customers/{id} - Given customer exists, when find by id, then returns customer")
  void givenCustomerExists_whenFindCustomerById_thenReturnsCustomer() throws Exception {
    CustomerResponse customer = CustomerResponse.builder().id(1L).build();

    when(customerService.findById(anyLong())).thenReturn(customer);

    mockMvc
        .perform(get("/customers/{customerId}", 1L).accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.id").value(1));
  }

  @Test
  @DisplayName(
      "GET /customers/{id} - Given customer does not exist, when find by id, then returns not"
          + " found")
  void givenCustomerDoesNotExist_whenFindCustomerById_thenReturnsNotFound() throws Exception {
    when(customerService.findById(anyLong()))
        .thenThrow(new ResourceNotFoundException(MessageKeys.CUSTOMER_NOT_FOUND_EXCEPTION));

    mockMvc
        .perform(get("/customers/{customerId}", 99L).accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isNotFound())
        .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
        .andExpect(jsonPath("$.status").value(404))
        .andExpect(jsonPath("$.title").value("Not Found"))
        .andExpect(jsonPath("$.detail").value("Customer not found"))
        .andExpect(jsonPath("$.instance").value("/customers/99"));
  }

  @Test
  @DisplayName("POST /customers - Given valid customer data, when create, then returns created")
  void givenValidCustomerData_whenCreateCustomer_thenReturnsCreated() throws Exception {
    CreateCustomerRequest request =
        CreateCustomerRequest.builder()
            .firstName("Carlos Alberto")
            .lastName("Arroyo Martínez")
            .password("secret123#")
            .email("carroyom@mail.com")
            .username("carroyom")
            .build();

    CustomerResponse customer = CustomerResponse.builder().id(1L).build();

    when(customerService.create(any(CreateCustomerRequest.class))).thenReturn(customer);

    mockMvc
        .perform(
            post("/customers")
                .content(mapper.writeValueAsString(request))
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isCreated())
        .andExpect(header().string("location", "http://localhost/customers/1"));
  }

  @Test
  @DisplayName(
      "POST /customers - Given invalid customer data, when create, then returns problem detail")
  void givenInvalidCustomerData_whenCreateCustomer_thenReturnsUnprocessableEntity()
      throws Exception {
    mockMvc
        .perform(post("/customers").content("{}").contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isUnprocessableEntity())
        .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
        .andExpect(jsonPath("$.status").value(422))
        .andExpect(jsonPath("$.title").value("Unprocessable Entity"))
        .andExpect(jsonPath("$.detail").value("Invalid request data"))
        .andExpect(jsonPath("$.instance").value("/customers"))
        .andExpect(jsonPath("$.errors").isMap())
        .andExpect(jsonPath("$.errors.username").value("Username should not be blank"))
        .andExpect(jsonPath("$.errors.email").value("Email should not be blank"))
        .andExpect(jsonPath("$.errors.password").value("Password should not be null"));

    verifyNoInteractions(customerService);
  }

  @Test
  @DisplayName(
      "PUT /customers/{id} - Given valid customer data, when update, then returns no content")
  void givenValidCustomerData_whenUpdateCustomer_thenReturnsNoContent() throws Exception {
    UpdateCustomerRequest request =
        UpdateCustomerRequest.builder()
            .firstName("Carlos Alberto")
            .lastName("Arroyo Martínez")
            .build();

    mockMvc
        .perform(
            put("/customers/{customerId}", 1L)
                .content(mapper.writeValueAsString(request))
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isNoContent());
  }

  @Test
  @DisplayName(
      "PUT /customers/{id} - Given invalid customer data, when update, then returns problem"
          + " detail")
  void givenInvalidCustomerData_whenUpdateCustomer_thenReturnsUnprocessableEntity()
      throws Exception {
    mockMvc
        .perform(
            put("/customers/{customerId}", 1L)
                .content("{}")
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isUnprocessableEntity())
        .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
        .andExpect(jsonPath("$.status").value(422))
        .andExpect(jsonPath("$.detail").value("Invalid request data"))
        .andExpect(jsonPath("$.instance").value("/customers/1"))
        .andExpect(jsonPath("$.errors").isMap())
        .andExpect(jsonPath("$.errors.firstName").value("First name should not be blank"))
        .andExpect(jsonPath("$.errors.lastName").value("Last name should not be blank"));

    verifyNoInteractions(customerService);
  }

  @Test
  @DisplayName(
      "DELETE /customers/{id} - Given customer exists, when delete, then returns no content")
  void givenCustomerExists_whenDeleteCustomer_thenReturnsNoContent() throws Exception {
    mockMvc.perform(delete("/customers/{customerId}", 1L)).andExpect(status().isNoContent());
  }
}
