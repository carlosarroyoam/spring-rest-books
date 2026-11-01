package com.carlosarroyoam.rest.books.cart;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.anonymous;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.carlosarroyoam.rest.books.cart.dto.UpdateCartItemRequest;
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

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class CartControllerIT {
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
                            .jwt(jwt ->
                                jwt.claim("preferred_username", "carroyom")
                                    .claim("customer_id", 1L))
                            .authorities(new SimpleGrantedAuthority("ROLE_App/Customer"))))
            .build();
  }

  @Test
  @DisplayName("GET /carts - Given cart exists, when find by customer id, then returns cart")
  void givenCartExists_whenFindCartByCustomerId_thenReturnsCart() throws Exception {
    String expectedJson = JsonUtils.readJson("carts/find-by-customer-id.json");

    String responseJson =
        mockMvc
            .perform(get("/carts"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andReturn()
            .getResponse()
            .getContentAsString();

    JSONAssert.assertEquals(expectedJson, responseJson, false);
  }

  @Test
  @DisplayName(
      "GET /carts - Given anonymous user, when find by customer id, then returns unauthorized")
  void givenAnonymousUser_whenFindCartByCustomerId_thenReturnsUnauthorized() throws Exception {
    mockMvc
        .perform(get("/carts").with(anonymous()))
        .andExpect(status().isUnauthorized())
        .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
        .andExpect(jsonPath("$.status").value(401))
        .andExpect(jsonPath("$.title").value("Unauthorized"))
        .andExpect(jsonPath("$.instance").value("/carts"));
  }

  @Test
  @DisplayName("GET /carts - Given admin role, when find by customer id, then returns forbidden")
  void givenAdminRole_whenFindCartByCustomerId_thenReturnsForbidden() throws Exception {
    mockMvc
        .perform(get("/carts").with(adminJwt()))
        .andExpect(status().isForbidden())
        .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
        .andExpect(jsonPath("$.status").value(403))
        .andExpect(jsonPath("$.title").value("Forbidden"))
        .andExpect(jsonPath("$.instance").value("/carts"));
  }

  @Test
  @DisplayName(
      "GET /carts - Given cart does not exist, when find by customer id, then returns not found")
  void givenCartDoesNotExist_whenFindCartByCustomerId_thenReturnsNotFound() throws Exception {
    mockMvc
        .perform(get("/carts").with(customerWithoutCartJwt()))
        .andExpect(status().isNotFound())
        .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
        .andExpect(jsonPath("$.status").value(404))
        .andExpect(jsonPath("$.title").value("Not Found"))
        .andExpect(jsonPath("$.detail").value("Cart not found"))
        .andExpect(jsonPath("$.instance").value("/carts"));
  }

  @Test
  @DisplayName(
      "PUT /carts/items - Given valid cart item data, when update, then returns no content")
  void givenValidCartItemData_whenUpdateCartItem_thenReturnsNoContent() throws Exception {
    UpdateCartItemRequest request = UpdateCartItemRequest.builder().quantity(1).bookId(1L).build();

    mockMvc
        .perform(
            put("/carts/items")
                .content(mapper.writeValueAsString(request))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isNoContent());
  }

  @Test
  @DisplayName("PUT /carts/items - Given admin role, when update, then returns forbidden")
  void givenAdminRole_whenUpdateCartItem_thenReturnsForbidden() throws Exception {
    mockMvc
        .perform(
            put("/carts/items")
                .with(adminJwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(updateRequest(1L))))
        .andExpect(status().isForbidden())
        .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
        .andExpect(jsonPath("$.status").value(403))
        .andExpect(jsonPath("$.title").value("Forbidden"))
        .andExpect(jsonPath("$.instance").value("/carts/items"));
  }

  @Test
  @DisplayName("PUT /carts/items - Given book does not exist, when update, then returns not found")
  void givenBookDoesNotExist_whenUpdateCartItem_thenReturnsNotFound() throws Exception {
    mockMvc
        .perform(
            put("/carts/items")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(updateRequest(9999L))))
        .andExpect(status().isNotFound())
        .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
        .andExpect(jsonPath("$.status").value(404))
        .andExpect(jsonPath("$.title").value("Not Found"))
        .andExpect(jsonPath("$.detail").value("Book not found"))
        .andExpect(jsonPath("$.instance").value("/carts/items"));
  }

  @Test
  @DisplayName(
      "PUT /carts/items - Given invalid cart item data, when update, then returns problem detail")
  void givenInvalidCartItemData_whenUpdateCartItem_thenReturnsUnprocessableEntity()
      throws Exception {
    mockMvc
        .perform(put("/carts/items").contentType(MediaType.APPLICATION_JSON).content("{}"))
        .andExpect(status().isUnprocessableEntity())
        .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
        .andExpect(jsonPath("$.status").value(422))
        .andExpect(jsonPath("$.title").value("Unprocessable Entity"))
        .andExpect(jsonPath("$.detail").value("Invalid request data"))
        .andExpect(jsonPath("$.instance").value("/carts/items"))
        .andExpect(jsonPath("$.errors").isMap())
        .andExpect(jsonPath("$.errors.bookId").isNotEmpty())
        .andExpect(jsonPath("$.errors.quantity").isNotEmpty());
  }

  @Test
  @DisplayName(
      "DELETE /carts/items/{id} - Given cart item exists, when delete, then returns no content")
  void givenCartItemExists_whenDeleteCartItem_thenReturnsNoContent() throws Exception {
    mockMvc
        .perform(delete("/carts/items/{cartItemId}", 1L).accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isNoContent());
  }

  @Test
  @DisplayName("DELETE /carts/items/{id} - Given admin role, when delete, then returns forbidden")
  void givenAdminRole_whenDeleteCartItem_thenReturnsForbidden() throws Exception {
    mockMvc
        .perform(delete("/carts/items/{cartItemId}", 1L).with(adminJwt()))
        .andExpect(status().isForbidden())
        .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
        .andExpect(jsonPath("$.status").value(403))
        .andExpect(jsonPath("$.title").value("Forbidden"))
        .andExpect(jsonPath("$.instance").value("/carts/items/1"));
  }

  @Test
  @DisplayName(
      "DELETE /carts/items/{id} - Given cart does not exist, when delete, then returns not found")
  void givenCartDoesNotExist_whenDeleteCartItem_thenReturnsNotFound() throws Exception {
    mockMvc
        .perform(delete("/carts/items/{cartItemId}", 1L).with(customerWithoutCartJwt()))
        .andExpect(status().isNotFound())
        .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
        .andExpect(jsonPath("$.status").value(404))
        .andExpect(jsonPath("$.title").value("Not Found"))
        .andExpect(jsonPath("$.detail").value("Cart not found"))
        .andExpect(jsonPath("$.instance").value("/carts/items/1"));
  }

  private static RequestPostProcessor adminJwt() {
    return jwt()
        .jwt(jwt -> jwt.claim("preferred_username", "admin").claim("customer_id", 1L))
        .authorities(new SimpleGrantedAuthority("ROLE_App/Admin"));
  }

  private static RequestPostProcessor customerWithoutCartJwt() {
    return jwt()
        .jwt(jwt -> jwt.claim("preferred_username", "nocart").claim("customer_id", 9999L))
        .authorities(new SimpleGrantedAuthority("ROLE_App/Customer"));
  }

  private static UpdateCartItemRequest updateRequest(Long bookId) {
    return UpdateCartItemRequest.builder().quantity(1).bookId(bookId).build();
  }
}
