package com.carlosarroyoam.rest.books.customer;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.carlosarroyoam.rest.books.core.dto.PagedResponse;
import com.carlosarroyoam.rest.books.core.dto.PaginationResponse;
import com.carlosarroyoam.rest.books.core.exception.GlobalExceptionHandler;
import com.carlosarroyoam.rest.books.customer.dto.CreateCustomerRequest;
import com.carlosarroyoam.rest.books.customer.dto.CustomerResponse;
import com.carlosarroyoam.rest.books.customer.dto.CustomerSpecs;
import com.carlosarroyoam.rest.books.customer.dto.UpdateCustomerRequest;
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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
class CustomerControllerTest {
  private ObjectMapper mapper;
  private MockMvc mockMvc;

  @Mock private CustomerService customerService;

  @InjectMocks private CustomerController customerController;

  @BeforeEach
  void setup() {
    mapper = new ObjectMapper();
    mapper.findAndRegisterModules();

    mockMvc =
        MockMvcBuilders.standaloneSetup(customerController)
            .setControllerAdvice(GlobalExceptionHandler.class)
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
        .andExpect(jsonPath("$.pagination.totalItems").value(1))
        .andExpect(jsonPath("$.pagination.totalPages").value(1));
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
      "DELETE /customers/{id} - Given customer exists, when delete, then returns no content")
  void givenCustomerExists_whenDeleteCustomer_thenReturnsNoContent() throws Exception {
    mockMvc.perform(delete("/customers/{customerId}", 1L)).andExpect(status().isNoContent());
  }
}
