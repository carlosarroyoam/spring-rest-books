package com.carlosarroyoam.rest.books.customer;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.carlosarroyoam.rest.books.common.JsonUtils;
import com.carlosarroyoam.rest.books.customer.dto.CreateCustomerRequestDto;
import com.carlosarroyoam.rest.books.customer.dto.UpdateCustomerRequestDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.Customization;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.skyscreamer.jsonassert.comparator.CustomComparator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.wiremock.spring.ConfigureWireMock;
import org.wiremock.spring.EnableWireMock;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@AutoConfigureMockMvc
@EnableWireMock({ @ConfigureWireMock(port = 8089) })
@Transactional
class CustomerControllerIT {
  @Autowired
  private WebApplicationContext webApplicationContext;

  @Autowired
  private ObjectMapper mapper;

  @Autowired
  private MockMvc mockMvc;

  @BeforeEach
  void setup() {
    mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
        .apply(SecurityMockMvcConfigurers.springSecurity())
        .defaultRequest(get("/").with(jwt().jwt(jwt -> jwt.claim("preferred_username", "carroyom"))
            .authorities(new SimpleGrantedAuthority("ROLE_App/Admin"))))
        .build();
  }

  @Test
  @DisplayName("Should return List<CustomerDto> when find all customers")
  void shouldReturnListOfCustomersWhenFindAllCustomers() throws Exception {
    String expectedJson = JsonUtils.readJson("/customers/find-all.json");

    String responseJson = mockMvc.perform(get("/customers").param("page", "0").param("size", "25"))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
        .andReturn()
        .getResponse()
        .getContentAsString();

    JSONAssert.assertEquals(expectedJson, responseJson, false);
  }

  @Test
  @DisplayName("Should return List<CustomerDto> when find all customers with filters")
  void shouldReturnListOfCustomersWhenFindAllCustomersWithFilters() throws Exception {
    String expectedJson = JsonUtils.readJson("/customers/find-all_with_filters.json");

    String responseJson = mockMvc
        .perform(get("/customers").param("page", "0")
            .param("size", "25")
            .param("firstName", "Carlos Alberto")
            .param("lastName", "Arroyo Martínez")
            .param("email", "carroyom@mail.com")
            .param("username", "carroyom"))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
        .andReturn()
        .getResponse()
        .getContentAsString();

    JSONAssert.assertEquals(expectedJson, responseJson, false);
  }

  @Test
  @DisplayName("Should return CustomerDto when find customer by id with existing id")
  void shouldReturnCustomerDtoWhenFindCustomerByIdWithExistingId() throws Exception {
    String expectedJson = JsonUtils.readJson("/customers/find-by-id.json");

    String responseJson = mockMvc.perform(get("/customers/{customerId}", 1L))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
        .andReturn()
        .getResponse()
        .getContentAsString();

    JSONAssert.assertEquals(expectedJson, responseJson, false);
  }

  @Test
  @DisplayName("Should throw AppExceptionDto when find customer by id with non existing id")
  void shouldThrowWhenFindCustomerByIdWithNonExistingId() throws Exception {
    String expectedJson = JsonUtils.readJson("/customers/find-by-id_with_non_existing_id.json");

    String responseJson = mockMvc.perform(get("/customers/{customerId}", 1000L))
        .andExpect(status().isNotFound())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
        .andReturn()
        .getResponse()
        .getContentAsString();

    JSONAssert.assertEquals(expectedJson, responseJson, new CustomComparator(
        JSONCompareMode.LENIENT, new Customization("timestamp", (o1, o2) -> true)));
  }

  @Test
  @DisplayName("Should return created when create a customer with valid data")
  void shouldReturnCreatedWhenCreateCustomerWithValidData() throws Exception {
    CreateCustomerRequestDto requestDto = CreateCustomerRequestDto.builder()
        .firstName("Carlos Alberto")
        .lastName("Arroyo Martínez")
        .password("secret123#")
        .email("carroyom2@mail.com")
        .username("carroyom2")
        .build();

    mockMvc
        .perform(post("/customers").contentType(MediaType.APPLICATION_JSON)
            .content(mapper.writeValueAsString(requestDto)))
        .andExpect(status().isCreated())
        .andExpect(header().string("Location", "http://localhost/customers/3"));
  }

  @Test
  @DisplayName("Should throw AppExceptionDto when create a customer with existing username")
  void shouldThrowWhenCreateCustomerWithExistingUsername() throws Exception {
    String expectedJson = JsonUtils.readJson("/customers/create_with_existing_username.json");

    CreateCustomerRequestDto requestDto = CreateCustomerRequestDto.builder()
        .firstName("Carlos Alberto")
        .lastName("Arroyo Martínez")
        .password("secret123#")
        .email("carroyom2@mail.com")
        .username("carroyom")
        .build();

    String responseJson = mockMvc
        .perform(post("/customers").contentType(MediaType.APPLICATION_JSON)
            .content(mapper.writeValueAsString(requestDto)))
        .andExpect(status().isBadRequest())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
        .andReturn()
        .getResponse()
        .getContentAsString();

    JSONAssert.assertEquals(expectedJson, responseJson, new CustomComparator(
        JSONCompareMode.LENIENT, new Customization("timestamp", (o1, o2) -> true)));
  }

  @Test
  @DisplayName("Should throw AppExceptionDto when create a customer with existing email")
  void shouldThrowWhenCreateCustomerWithExistingEmail() throws Exception {
    String expectedJson = JsonUtils.readJson("/customers/create_with_existing_email.json");

    CreateCustomerRequestDto requestDto = CreateCustomerRequestDto.builder()
        .firstName("Carlos Alberto")
        .lastName("Arroyo Martínez")
        .password("secret123#")
        .email("carroyom@mail.com")
        .username("carroyom2")
        .build();

    String responseJson = mockMvc
        .perform(post("/customers").contentType(MediaType.APPLICATION_JSON)
            .content(mapper.writeValueAsString(requestDto)))
        .andExpect(status().isBadRequest())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
        .andReturn()
        .getResponse()
        .getContentAsString();

    JSONAssert.assertEquals(expectedJson, responseJson, new CustomComparator(
        JSONCompareMode.LENIENT, new Customization("timestamp", (o1, o2) -> true)));
  }

  @Test
  @DisplayName("Should return no content update customer with valid data")
  void shouldReturnNoContentWhenUpdateCustomerWithValidData() throws Exception {
    UpdateCustomerRequestDto requestDto = UpdateCustomerRequestDto.builder()
        .firstName("Carlos Alberto")
        .lastName("Arroyo Martínez")
        .build();

    mockMvc.perform(put("/customers/{customerId}", 1L).contentType(MediaType.APPLICATION_JSON)
        .content(mapper.writeValueAsString(requestDto))).andExpect(status().isNoContent());
  }

  @Test
  @DisplayName("Should throw AppExceptionDto when update customer with non existing id")
  void shouldThrowWhenUpdateCustomerWithNonExistingId() throws Exception {
    String expectedJson = JsonUtils.readJson("/customers/update_with_non_existing_id.json");

    UpdateCustomerRequestDto requestDto = UpdateCustomerRequestDto.builder()
        .firstName("Carlos Alberto")
        .lastName("Arroyo Martínez")
        .build();

    String responseJson = mockMvc
        .perform(put("/customers/{customerId}", 1000L).contentType(MediaType.APPLICATION_JSON)
            .content(mapper.writeValueAsString(requestDto)))
        .andExpect(status().isNotFound())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
        .andReturn()
        .getResponse()
        .getContentAsString();

    JSONAssert.assertEquals(expectedJson, responseJson, new CustomComparator(
        JSONCompareMode.LENIENT, new Customization("timestamp", (o1, o2) -> true)));
  }

  @Test
  @DisplayName("Should return no content when delete customer with existing id")
  void shouldReturnNoContentWhenDeleteCustomerWithExistingId() throws Exception {
    mockMvc.perform(delete("/customers/{customerId}", 1L)).andExpect(status().isNoContent());
  }

  @Test
  @DisplayName("Should throw AppExceptionDto when delete customer with non existing id")
  void shouldThrowWhenDeleteCustomerWithNonExistingId() throws Exception {
    String expectedJson = JsonUtils.readJson("/customers/delete_with_non_existing_id.json");

    String responseJson = mockMvc.perform(delete("/customers/{customerId}", 1000L))
        .andExpect(status().isNotFound())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
        .andReturn()
        .getResponse()
        .getContentAsString();

    JSONAssert.assertEquals(expectedJson, responseJson, new CustomComparator(
        JSONCompareMode.LENIENT, new Customization("timestamp", (o1, o2) -> true)));
  }
}
