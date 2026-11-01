package com.carlosarroyoam.rest.books.order;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.anonymous;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.carlosarroyoam.rest.books.order.dto.CreateOrderItemRequest;
import com.carlosarroyoam.rest.books.order.dto.CreateOrderRequest;
import com.carlosarroyoam.rest.books.order.dto.UpdateOrderRequest;
import com.carlosarroyoam.rest.books.support.testutils.JsonUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
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

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class OrderControllerIT {
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
  @DisplayName("GET /orders - Given orders exist, when find all, then returns paged orders")
  void givenOrdersExist_whenFindAllOrders_thenReturnsPagedOrders() throws Exception {
    String expectedJson = JsonUtils.readJson("orders/find-all.json");

    String responseJson =
        mockMvc
            .perform(get("/orders").param("page", "0").param("size", "25"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andReturn()
            .getResponse()
            .getContentAsString();

    JSONAssert.assertEquals(expectedJson, responseJson, false);
  }

  @Test
  @DisplayName("GET /orders - Given anonymous user, when find all, then returns unauthorized")
  void givenAnonymousUser_whenFindAllOrders_thenReturnsUnauthorized() throws Exception {
    mockMvc
        .perform(get("/orders").with(anonymous()))
        .andExpect(status().isUnauthorized())
        .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
        .andExpect(jsonPath("$.status").value(401))
        .andExpect(jsonPath("$.title").value("Unauthorized"))
        .andExpect(jsonPath("$.instance").value("/orders"));
  }

  @Test
  @DisplayName("GET /orders - Given customer role, when find all, then returns forbidden")
  void givenCustomerRole_whenFindAllOrders_thenReturnsForbidden() throws Exception {
    mockMvc
        .perform(get("/orders").with(customerJwt()))
        .andExpect(status().isForbidden())
        .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
        .andExpect(jsonPath("$.status").value(403))
        .andExpect(jsonPath("$.title").value("Forbidden"))
        .andExpect(jsonPath("$.instance").value("/orders"));
  }

  @Test
  @DisplayName("GET /orders/{id} - Given order exists, when find by id, then returns order")
  void givenOrderExists_whenFindOrderById_thenReturnsOrder() throws Exception {
    String expectedJson = JsonUtils.readJson("orders/find-by-id.json");

    String responseJson =
        mockMvc
            .perform(get("/orders/{orderId}", 1L))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andReturn()
            .getResponse()
            .getContentAsString();

    JSONAssert.assertEquals(expectedJson, responseJson, false);
  }

  @Test
  @DisplayName("GET /orders/{id} - Given customer role, when find by id, then returns forbidden")
  void givenCustomerRole_whenFindOrderById_thenReturnsForbidden() throws Exception {
    mockMvc
        .perform(get("/orders/{orderId}", 1L).with(customerJwt()))
        .andExpect(status().isForbidden())
        .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
        .andExpect(jsonPath("$.status").value(403))
        .andExpect(jsonPath("$.title").value("Forbidden"))
        .andExpect(jsonPath("$.instance").value("/orders/1"));
  }

  @Test
  @DisplayName(
      "GET /orders/{id} - Given order does not exist, when find by id, then returns not found")
  void givenOrderDoesNotExist_whenFindOrderById_thenReturnsNotFound() throws Exception {
    mockMvc
        .perform(get("/orders/{orderId}", 9999L))
        .andExpect(status().isNotFound())
        .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
        .andExpect(jsonPath("$.status").value(404))
        .andExpect(jsonPath("$.title").value("Not Found"))
        .andExpect(jsonPath("$.detail").value("Order not found"))
        .andExpect(jsonPath("$.instance").value("/orders/9999"));
  }

  @Test
  @DisplayName("POST /orders - Given valid order data, when create, then returns created")
  void givenValidOrderData_whenCreateOrder_thenReturnsCreated() throws Exception {
    CreateOrderItemRequest item = CreateOrderItemRequest.builder().bookId(1L).quantity(1).build();

    CreateOrderRequest request =
        CreateOrderRequest.builder()
            .customerId(1L)
            .shippingAddress("789 New Street, Metropolis")
            .billingAddress("789 New Street, Metropolis")
            .notes("Please handle with care")
            .items(List.of(item))
            .build();

    mockMvc
        .perform(
            post("/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(request)))
        .andExpect(status().isCreated())
        .andExpect(header().string("Location", "http://localhost/orders/3"));
  }

  @Test
  @DisplayName("POST /orders - Given customer role, when create, then returns forbidden")
  void givenCustomerRole_whenCreateOrder_thenReturnsForbidden() throws Exception {
    mockMvc
        .perform(
            post("/orders")
                .with(customerJwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(validCreateRequest(1L))))
        .andExpect(status().isForbidden())
        .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
        .andExpect(jsonPath("$.status").value(403))
        .andExpect(jsonPath("$.title").value("Forbidden"))
        .andExpect(jsonPath("$.instance").value("/orders"));
  }

  @Test
  @DisplayName("POST /orders - Given customer does not exist, when create, then returns not found")
  void givenCustomerDoesNotExist_whenCreateOrder_thenReturnsNotFound() throws Exception {
    mockMvc
        .perform(
            post("/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(validCreateRequest(9999L))))
        .andExpect(status().isNotFound())
        .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
        .andExpect(jsonPath("$.status").value(404))
        .andExpect(jsonPath("$.title").value("Not Found"))
        .andExpect(jsonPath("$.detail").value("Customer not found"))
        .andExpect(jsonPath("$.instance").value("/orders"));
  }

  @Test
  @DisplayName("POST /orders - Given invalid order data, when create, then returns problem detail")
  void givenInvalidOrderData_whenCreateOrder_thenReturnsUnprocessableEntity() throws Exception {
    mockMvc
        .perform(post("/orders").contentType(MediaType.APPLICATION_JSON).content("{}"))
        .andExpect(status().isUnprocessableEntity())
        .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
        .andExpect(jsonPath("$.status").value(422))
        .andExpect(jsonPath("$.title").value("Unprocessable Entity"))
        .andExpect(jsonPath("$.detail").value("Invalid request data"))
        .andExpect(jsonPath("$.instance").value("/orders"))
        .andExpect(jsonPath("$.errors").isMap())
        .andExpect(jsonPath("$.errors.customerId").isNotEmpty())
        .andExpect(jsonPath("$.errors.shippingAddress").isNotEmpty())
        .andExpect(jsonPath("$.errors.items").isNotEmpty());
  }

  @Test
  @DisplayName("PUT /orders/{id} - Given valid order data, when update, then returns no content")
  void givenValidOrderData_whenUpdateOrder_thenReturnsNoContent() throws Exception {
    UpdateOrderRequest request =
        UpdateOrderRequest.builder()
            .shippingAddress("999 Updated Street, Gotham")
            .billingAddress("999 Updated Street, Gotham")
            .notes("Updated notes")
            .build();

    mockMvc
        .perform(
            put("/orders/{orderId}", 1L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(request)))
        .andExpect(status().isNoContent());
  }

  @Test
  @DisplayName("PUT /orders/{id} - Given customer role, when update, then returns forbidden")
  void givenCustomerRole_whenUpdateOrder_thenReturnsForbidden() throws Exception {
    mockMvc
        .perform(
            put("/orders/{orderId}", 1L)
                .with(customerJwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(validUpdateRequest())))
        .andExpect(status().isForbidden())
        .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
        .andExpect(jsonPath("$.status").value(403))
        .andExpect(jsonPath("$.title").value("Forbidden"))
        .andExpect(jsonPath("$.instance").value("/orders/1"));
  }

  @Test
  @DisplayName("PUT /orders/{id} - Given order does not exist, when update, then returns not found")
  void givenOrderDoesNotExist_whenUpdateOrder_thenReturnsNotFound() throws Exception {
    mockMvc
        .perform(
            put("/orders/{orderId}", 9999L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(validUpdateRequest())))
        .andExpect(status().isNotFound())
        .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
        .andExpect(jsonPath("$.status").value(404))
        .andExpect(jsonPath("$.title").value("Not Found"))
        .andExpect(jsonPath("$.detail").value("Order not found"))
        .andExpect(jsonPath("$.instance").value("/orders/9999"));
  }

  @Test
  @DisplayName(
      "PUT /orders/{id} - Given invalid order data, when update, then returns problem detail")
  void givenInvalidOrderData_whenUpdateOrder_thenReturnsUnprocessableEntity() throws Exception {
    mockMvc
        .perform(put("/orders/{orderId}", 1L).contentType(MediaType.APPLICATION_JSON).content("{}"))
        .andExpect(status().isUnprocessableEntity())
        .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
        .andExpect(jsonPath("$.status").value(422))
        .andExpect(jsonPath("$.title").value("Unprocessable Entity"))
        .andExpect(jsonPath("$.detail").value("Invalid request data"))
        .andExpect(jsonPath("$.instance").value("/orders/1"))
        .andExpect(jsonPath("$.errors").isMap())
        .andExpect(jsonPath("$.errors.shippingAddress").isNotEmpty())
        .andExpect(jsonPath("$.errors.billingAddress").isNotEmpty());
  }

  private static RequestPostProcessor customerJwt() {
    return jwt()
        .jwt(jwt -> jwt.claim("preferred_username", "customer"))
        .authorities(new SimpleGrantedAuthority("ROLE_App/Customer"));
  }

  private static CreateOrderRequest validCreateRequest(Long customerId) {
    CreateOrderItemRequest item = CreateOrderItemRequest.builder().bookId(1L).quantity(1).build();

    return CreateOrderRequest.builder()
        .customerId(customerId)
        .shippingAddress("789 New Street, Metropolis")
        .billingAddress("789 New Street, Metropolis")
        .notes("Please handle with care")
        .items(List.of(item))
        .build();
  }

  private static UpdateOrderRequest validUpdateRequest() {
    return UpdateOrderRequest.builder()
        .shippingAddress("999 Updated Street, Gotham")
        .billingAddress("999 Updated Street, Gotham")
        .notes("Updated notes")
        .build();
  }
}
