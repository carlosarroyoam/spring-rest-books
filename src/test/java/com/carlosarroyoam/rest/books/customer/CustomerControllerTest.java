package com.carlosarroyoam.rest.books.customer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

import com.carlosarroyoam.rest.books.core.exception.ControllerAdvisor;
import com.carlosarroyoam.rest.books.customer.dto.CreateCustomerRequestDto;
import com.carlosarroyoam.rest.books.customer.dto.CustomerDto;
import com.carlosarroyoam.rest.books.customer.dto.CustomerFilterDto;
import com.carlosarroyoam.rest.books.customer.dto.UpdateCustomerRequestDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
class CustomerControllerTest {
  private ObjectMapper mapper;
  private MockMvc mockMvc;

  @Mock
  private CustomerService customerService;

  @InjectMocks
  private CustomerController customerController;

  @BeforeEach
  void setup() {
    mapper = new ObjectMapper();
    mapper.findAndRegisterModules();
    mockMvc = MockMvcBuilders.standaloneSetup(customerController)
        .setControllerAdvice(ControllerAdvisor.class)
        .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
        .build();
  }

  @Test
  @DisplayName("Should return List<CustomerDto> when find all customers")
  void shouldReturnListOfCustomersWhenFindAllCustomers() throws Exception {
    List<CustomerDto> customers = List.of(CustomerDto.builder().build());

    when(customerService.findAll(any(Pageable.class), any(CustomerFilterDto.class)))
        .thenReturn(customers);

    MvcResult mvcResult = mockMvc.perform(get("/customers").queryParam("page", "0")
        .queryParam("size", "25")
        .accept(MediaType.APPLICATION_JSON)).andReturn();

    String responseJson = mvcResult.getResponse().getContentAsString();
    CollectionType collectionType = mapper.getTypeFactory()
        .constructCollectionType(List.class, CustomerDto.class);
    List<CustomerDto> responseDto = mapper.readValue(responseJson, collectionType);

    assertThat(mvcResult.getResponse().getStatus()).isEqualTo(HttpStatus.OK.value());
    assertThat(mvcResult.getResponse().getContentType())
        .isEqualTo(MediaType.APPLICATION_JSON_VALUE);
    assertThat(responseDto).isNotNull().isNotEmpty().hasSize(1);
  }

  @Test
  @DisplayName("Should return CustomerDto when find customer by id")
  void shouldReturnCustomerDtoWhenFindCustomerById() throws Exception {
    CustomerDto customer = CustomerDto.builder().id(1L).build();

    when(customerService.findById(anyLong())).thenReturn(customer);

    MvcResult mvcResult = mockMvc
        .perform(get("/customers/{customerId}", 1L).accept(MediaType.APPLICATION_JSON))
        .andReturn();

    String responseJson = mvcResult.getResponse().getContentAsString();
    CustomerDto responseDto = mapper.readValue(responseJson, CustomerDto.class);

    assertThat(mvcResult.getResponse().getStatus()).isEqualTo(HttpStatus.OK.value());
    assertThat(mvcResult.getResponse().getContentType())
        .isEqualTo(MediaType.APPLICATION_JSON_VALUE);
    assertThat(responseDto).isNotNull();
    assertThat(responseDto.getId()).isEqualTo(1L);
  }

  @Test
  @DisplayName("Should return created when create a customer")
  void shouldReturnCreatedWhenCreateCustomer() throws Exception {
    CreateCustomerRequestDto requestDto = CreateCustomerRequestDto.builder()
        .firstName("Carlos Alberto")
        .lastName("Arroyo Martínez")
        .password("secret123#")
        .email("carroyom@mail.com")
        .username("carroyom")
        .build();

    CustomerDto customer = CustomerDto.builder().id(1L).build();

    when(customerService.create(any(CreateCustomerRequestDto.class))).thenReturn(customer);

    MvcResult mvcResult = mockMvc
        .perform(post("/customers").content(mapper.writeValueAsString(requestDto))
            .contentType(MediaType.APPLICATION_JSON))
        .andReturn();

    assertThat(mvcResult.getResponse().getStatus()).isEqualTo(HttpStatus.CREATED.value());
    assertThat(mvcResult.getResponse().getHeader("location"))
        .isEqualTo("http://localhost/customers/1");
  }

  @Test
  @DisplayName("Should return no content when update customer")
  void shouldReturnNoContentUpdateCustomer() throws Exception {
    UpdateCustomerRequestDto requestDto = UpdateCustomerRequestDto.builder()
        .firstName("Carlos Alberto")
        .lastName("Arroyo Martínez")
        .build();

    MvcResult mvcResult = mockMvc
        .perform(put("/customers/{customerId}", 1L).content(mapper.writeValueAsString(requestDto))
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))
        .andReturn();

    assertThat(mvcResult.getResponse().getStatus()).isEqualTo(HttpStatus.NO_CONTENT.value());
  }

  @Test
  @DisplayName("Should return no content when delete customer")
  void shouldReturnNoContentWhenDeleteCustomer() throws Exception {
    MvcResult mvcResult = mockMvc
        .perform(delete("/customers/{customerId}", 1L).accept(MediaType.APPLICATION_JSON))
        .andReturn();

    assertThat(mvcResult.getResponse().getStatus()).isEqualTo(HttpStatus.NO_CONTENT.value());
  }
}
