package com.carlosarroyoam.rest.books.order;

import com.carlosarroyoam.rest.books.common.JsonUtils;
import com.carlosarroyoam.rest.books.order.dto.CreateOrderItemRequest;
import com.carlosarroyoam.rest.books.order.dto.CreateOrderRequest;
import com.carlosarroyoam.rest.books.order.dto.UpdateOrderRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
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
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

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
@Transactional
class OrderControllerIT {
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
  @DisplayName("GET /orders - Given orders exist, when find all, then returns paged orders")
  void givenOrdersExist_whenFindAllOrders_thenReturnsPagedOrders() throws Exception {
    String expectedJson = JsonUtils.readJson("/orders/find-all.json");

    String responseJson = mockMvc.perform(get("/orders").param("page", "0").param("size", "25"))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
        .andReturn()
        .getResponse()
        .getContentAsString();

    JSONAssert.assertEquals(expectedJson, responseJson, false);
  }

  @Test
  @DisplayName("GET /orders/{id} - Given order exists, when find by id, then returns order")
  void givenOrderExists_whenFindOrderById_thenReturnsOrder() throws Exception {
    String expectedJson = JsonUtils.readJson("/orders/find-by-id.json");

    String responseJson = mockMvc.perform(get("/orders/{orderId}", 1L))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
        .andReturn()
        .getResponse()
        .getContentAsString();

    JSONAssert.assertEquals(expectedJson, responseJson, false);
  }

  @Test
  @DisplayName("POST /orders - Given valid order data, when create, then returns created")
  void givenValidOrderData_whenCreateOrder_thenReturnsCreated() throws Exception {
    CreateOrderItemRequest item = CreateOrderItemRequest.builder().bookId(1L).quantity(1).build();

    CreateOrderRequest request = CreateOrderRequest.builder()
        .customerId(1L)
        .shippingAddress("789 New Street, Metropolis")
        .billingAddress("789 New Street, Metropolis")
        .notes("Please handle with care")
        .items(List.of(item))
        .build();

    mockMvc
        .perform(post("/orders").contentType(MediaType.APPLICATION_JSON)
            .content(mapper.writeValueAsString(request)))
        .andExpect(status().isCreated())
        .andExpect(header().string("Location", "http://localhost/orders/3"));
  }

  @Test
  @DisplayName("PUT /orders/{id} - Given valid order data, when update, then returns no content")
  void givenValidOrderData_whenUpdateOrder_thenReturnsNoContent() throws Exception {
    UpdateOrderRequest request = UpdateOrderRequest.builder()
        .shippingAddress("999 Updated Street, Gotham")
        .billingAddress("999 Updated Street, Gotham")
        .notes("Updated notes")
        .build();

    mockMvc.perform(put("/orders/{orderId}", 1L).contentType(MediaType.APPLICATION_JSON)
        .content(mapper.writeValueAsString(request))).andExpect(status().isNoContent());
  }

  @Test
  @DisplayName("DELETE /orders/{id} - Given order exists, when delete, then returns no content")
  void givenOrderExists_whenDeleteOrder_thenReturnsNoContent() throws Exception {
    mockMvc.perform(delete("/orders/{orderId}", 1L)).andExpect(status().isNoContent());
  }
}