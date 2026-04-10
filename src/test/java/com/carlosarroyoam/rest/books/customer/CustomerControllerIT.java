package com.carlosarroyoam.rest.books.customer;

import com.carlosarroyoam.rest.books.common.JsonUtils;
import com.carlosarroyoam.rest.books.customer.dto.CreateCustomerRequest;
import com.carlosarroyoam.rest.books.customer.dto.UpdateCustomerRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;
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

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
  @DisplayName("GET /customers - Should return paged customers when find all customers")
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
  @DisplayName("GET /customers/{customerId} - Should return CustomerResponse when find customer by id with existing id")
  void shouldReturnCustomerResponseWhenFindCustomerByIdWithExistingId() throws Exception {
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
  @DisplayName("POST /customers - Should return created when create a customer with valid data")
  void shouldReturnCreatedWhenCreateCustomerWithValidData() throws Exception {
    CreateCustomerRequest request = CreateCustomerRequest.builder()
        .firstName("Carlos Alberto")
        .lastName("Arroyo Martínez")
        .password("secret123#")
        .email("carroyom2@mail.com")
        .username("carroyom2")
        .build();

    mockMvc
        .perform(post("/customers").contentType(MediaType.APPLICATION_JSON)
            .content(mapper.writeValueAsString(request)))
        .andExpect(status().isCreated())
        .andExpect(header().string("Location", "http://localhost/customers/3"));
  }

  @Test
  @DisplayName("PUT /customers/{customerId} - Should return no content update customer with valid data")
  void shouldReturnNoContentWhenUpdateCustomerWithValidData() throws Exception {
    UpdateCustomerRequest request = UpdateCustomerRequest.builder()
        .firstName("Carlos Alberto")
        .lastName("Arroyo Martínez")
        .build();

    mockMvc.perform(put("/customers/{customerId}", 1L).contentType(MediaType.APPLICATION_JSON)
        .content(mapper.writeValueAsString(request))).andExpect(status().isNoContent());
  }

  @Test
  @DisplayName("DELETE /customers/{customerId} - Should return no content when delete customer with existing id")
  void shouldReturnNoContentWhenDeleteCustomerWithExistingId() throws Exception {
    mockMvc.perform(delete("/customers/{customerId}", 1L)).andExpect(status().isNoContent());
  }
}
