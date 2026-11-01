package com.carlosarroyoam.rest.books.customer;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.anonymous;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.carlosarroyoam.rest.books.customer.dto.CreateCustomerRequest;
import com.carlosarroyoam.rest.books.customer.dto.UpdateCustomerRequest;
import com.carlosarroyoam.rest.books.support.testutils.JsonUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;
import org.wiremock.spring.ConfigureWireMock;
import org.wiremock.spring.EnableWireMock;

@SpringBootTest
@ActiveProfiles("test")
@EnableWireMock({@ConfigureWireMock(port = 8089)})
@Transactional
class CustomerControllerIT {
  @Autowired private WebApplicationContext webApplicationContext;
  @Autowired private ObjectMapper mapper;
  private MockMvc mockMvc;

  @BeforeEach
  void setup() {
    mockMvc =
        MockMvcBuilders.webAppContextSetup(webApplicationContext)
            .apply(SecurityMockMvcConfigurers.springSecurity())
            .defaultRequest(
                get("/")
                    .with(
                        jwt()
                            .jwt(jwt -> jwt.claim("preferred_username", "carroyom"))
                            .authorities(new SimpleGrantedAuthority("ROLE_App/Admin"))))
            .build();
  }

  @Test
  @DisplayName(
      "GET /customers - Given customers exist, when find all, then returns paged customers")
  void givenCustomersExist_whenFindAllCustomers_thenReturnsPagedCustomers() throws Exception {
    String expectedJson = JsonUtils.readJson("customers/find-all.json");

    String responseJson =
        mockMvc
            .perform(get("/customers").param("page", "0").param("size", "25"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andReturn()
            .getResponse()
            .getContentAsString();

    JSONAssert.assertEquals(expectedJson, responseJson, false);
  }

  @Test
  @DisplayName("GET /customers - Given anonymous user, when find all, then returns unauthorized")
  void givenAnonymousUser_whenFindAllCustomers_thenReturnsUnauthorized() throws Exception {
    mockMvc
        .perform(get("/customers").with(anonymous()))
        .andExpect(status().isUnauthorized())
        .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
        .andExpect(jsonPath("$.status").value(401))
        .andExpect(jsonPath("$.title").value("Unauthorized"))
        .andExpect(jsonPath("$.instance").value("/customers"));
  }

  @Test
  @DisplayName("GET /customers - Given customer role, when find all, then returns forbidden")
  void givenCustomerRole_whenFindAllCustomers_thenReturnsForbidden() throws Exception {
    mockMvc
        .perform(get("/customers").with(customerJwt()))
        .andExpect(status().isForbidden())
        .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
        .andExpect(jsonPath("$.status").value(403))
        .andExpect(jsonPath("$.title").value("Forbidden"))
        .andExpect(jsonPath("$.instance").value("/customers"));
  }

  @Test
  @DisplayName(
      "GET /customers/{id} - Given customer exists, when find by id, then returns customer")
  void givenCustomerExists_whenFindCustomerById_thenReturnsCustomer() throws Exception {
    String expectedJson = JsonUtils.readJson("customers/find-by-id.json");

    String responseJson =
        mockMvc
            .perform(get("/customers/{customerId}", 1L))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andReturn()
            .getResponse()
            .getContentAsString();

    JSONAssert.assertEquals(expectedJson, responseJson, false);
  }

  @Test
  @DisplayName(
      "GET /customers/{id} - Given customer role, when find by id, then returns forbidden")
  void givenCustomerRole_whenFindCustomerById_thenReturnsForbidden() throws Exception {
    mockMvc
        .perform(get("/customers/{customerId}", 1L).with(customerJwt()))
        .andExpect(status().isForbidden())
        .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
        .andExpect(jsonPath("$.status").value(403))
        .andExpect(jsonPath("$.instance").value("/customers/1"));
  }

  @Test
  @DisplayName(
      "GET /customers/{id} - Given customer does not exist, when find by id, then returns not"
          + " found")
  void givenCustomerDoesNotExist_whenFindCustomerById_thenReturnsNotFound() throws Exception {
    mockMvc
        .perform(get("/customers/{customerId}", 9999L))
        .andExpect(status().isNotFound())
        .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
        .andExpect(jsonPath("$.status").value(404))
        .andExpect(jsonPath("$.title").value("Not Found"))
        .andExpect(jsonPath("$.detail").value("Customer not found"))
        .andExpect(jsonPath("$.instance").value("/customers/9999"));
  }

  @Test
  @DisplayName("POST /customers - Given valid customer data, when create, then returns created")
  void givenValidCustomerData_whenCreateCustomer_thenReturnsCreated() throws Exception {
    CreateCustomerRequest request =
        CreateCustomerRequest.builder()
            .firstName("Carlos Alberto")
            .lastName("Arroyo Martínez")
            .password("secret123#")
            .email("carroyom2@mail.com")
            .username("carroyom2")
            .build();

    mockMvc
        .perform(
            post("/customers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(request)))
        .andExpect(status().isCreated())
        .andExpect(header().string("Location", "http://localhost/customers/3"));
  }

  @Test
  @DisplayName(
      "POST /customers - Given invalid customer data, when create, then returns problem detail")
  void givenInvalidCustomerData_whenCreateCustomer_thenReturnsProblemDetail() throws Exception {
    mockMvc
        .perform(post("/customers").contentType(MediaType.APPLICATION_JSON).content("{}"))
        .andExpect(status().isUnprocessableEntity())
        .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
        .andExpect(jsonPath("$.type").value("about:blank"))
        .andExpect(jsonPath("$.title").value("Unprocessable Entity"))
        .andExpect(jsonPath("$.status").value(422))
        .andExpect(jsonPath("$.detail").value("Invalid request data"))
        .andExpect(jsonPath("$.instance").value("/customers"))
        .andExpect(jsonPath("$.errors").isMap())
        .andExpect(jsonPath("$.errors.username").isNotEmpty());
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
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(request)))
        .andExpect(status().isNoContent());
  }

  @Test
  @DisplayName(
      "PUT /customers/{id} - Given customer does not exist, when update, then returns not found")
  void givenCustomerDoesNotExist_whenUpdateCustomer_thenReturnsNotFound() throws Exception {
    UpdateCustomerRequest request =
        UpdateCustomerRequest.builder()
            .firstName("Carlos Alberto")
            .lastName("Arroyo Martínez")
            .build();

    mockMvc
        .perform(
            put("/customers/{customerId}", 9999L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(request)))
        .andExpect(status().isNotFound())
        .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
        .andExpect(jsonPath("$.status").value(404))
        .andExpect(jsonPath("$.detail").value("Customer not found"));
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
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
        .andExpect(status().isUnprocessableEntity())
        .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
        .andExpect(jsonPath("$.status").value(422))
        .andExpect(jsonPath("$.detail").value("Invalid request data"))
        .andExpect(jsonPath("$.instance").value("/customers/1"))
        .andExpect(jsonPath("$.errors").isMap())
        .andExpect(jsonPath("$.errors.firstName").isNotEmpty())
        .andExpect(jsonPath("$.errors.lastName").isNotEmpty());
  }

  @Test
  @DisplayName(
      "DELETE /customers/{id} - Given customer exists, when delete, then returns no content")
  void givenCustomerExists_whenDeleteCustomer_thenReturnsNoContent() throws Exception {
    mockMvc.perform(delete("/customers/{customerId}", 1L)).andExpect(status().isNoContent());
  }

  @Test
  @DisplayName(
      "DELETE /customers/{id} - Given customer role, when delete, then returns forbidden")
  void givenCustomerRole_whenDeleteCustomer_thenReturnsForbidden() throws Exception {
    mockMvc
        .perform(delete("/customers/{customerId}", 1L).with(customerJwt()))
        .andExpect(status().isForbidden())
        .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
        .andExpect(jsonPath("$.status").value(403))
        .andExpect(jsonPath("$.instance").value("/customers/1"));
  }

  @Test
  @DisplayName(
      "DELETE /customers/{id} - Given customer does not exist, when delete, then returns not"
          + " found")
  void givenCustomerDoesNotExist_whenDeleteCustomer_thenReturnsNotFound() throws Exception {
    mockMvc
        .perform(delete("/customers/{customerId}", 9999L))
        .andExpect(status().isNotFound())
        .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
        .andExpect(jsonPath("$.status").value(404))
        .andExpect(jsonPath("$.detail").value("Customer not found"));
  }

  private static RequestPostProcessor customerJwt() {
    return jwt()
        .jwt(jwt -> jwt.claim("preferred_username", "customer"))
        .authorities(new SimpleGrantedAuthority("ROLE_App/Customer"));
  }
}
