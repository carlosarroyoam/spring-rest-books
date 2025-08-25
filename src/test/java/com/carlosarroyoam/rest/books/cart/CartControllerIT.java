package com.carlosarroyoam.rest.books.cart;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.carlosarroyoam.rest.books.cart.dto.UpdateCartItemRequestDto;
import com.carlosarroyoam.rest.books.common.JsonUtils;
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
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Transactional
class CartControllerIT {
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
        .defaultRequest(
            get("/").with(jwt().jwt(jwt -> jwt.claim("preferred_username", "carroyom"))))
        .build();
  }

  @Test
  @DisplayName("Should return CartDto when find cart by username")
  void shouldReturnCartDtoWhenFindCartByUsername() throws Exception {
    String expectedJson = JsonUtils.readJson("/carts/find-by-username.json");

    String responseJson = mockMvc.perform(get("/carts"))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andReturn()
        .getResponse()
        .getContentAsString();

    JSONAssert.assertEquals(expectedJson, responseJson, false);
  }

  @Test
  @DisplayName("Should return no content when update cart item with valid data")
  void shouldReturnNoContentWhenUpdateCartItemWithValidData() throws Exception {
    UpdateCartItemRequestDto requestDto = UpdateCartItemRequestDto.builder()
        .quantity(1)
        .bookId(1L)
        .build();

    mockMvc.perform(put("/carts/items").content(mapper.writeValueAsString(requestDto))
        .contentType(MediaType.APPLICATION_JSON)
        .accept(MediaType.APPLICATION_JSON)).andExpect(status().isNoContent());
  }

  @Test
  @DisplayName("Should throw AppExceptionDto when update cart item with non existing book id")
  void shouldThrowWhenUpdateCartItemWithNonExistingBookId() throws Exception {
    String expectedJson = JsonUtils.readJson("/carts/update_with_non_existing_book_id.json");

    UpdateCartItemRequestDto requestDto = UpdateCartItemRequestDto.builder()
        .quantity(1)
        .bookId(1000L)
        .build();

    String responseJson = mockMvc
        .perform(put("/carts/items").content(mapper.writeValueAsString(requestDto))
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isNotFound())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
        .andReturn()
        .getResponse()
        .getContentAsString();

    JSONAssert.assertEquals(expectedJson, responseJson, new CustomComparator(
        JSONCompareMode.LENIENT, new Customization("timestamp", (o1, o2) -> true)));
  }

  @Test
  @DisplayName("Should return no content when delete cart item with existing id")
  void shouldReturnNoContentWhenDeleteCartItemWithExistingId() throws Exception {
    mockMvc.perform(delete("/carts/items/{cartItemId}", 1L).accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isNoContent());
  }

  @Test
  @DisplayName("Should throw AppExceptionDto when delete cart item with non existing cart item id")
  void shouldThrowWhenDeleteCartItemWithNonExistingCartItemId() throws Exception {
    String expectedJson = JsonUtils.readJson("/carts/delete_with_non_existing_cart_item_id.json");

    String responseJson = mockMvc
        .perform(delete("/carts/items/{cartItemId}", 1000L).accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isNotFound())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
        .andReturn()
        .getResponse()
        .getContentAsString();

    JSONAssert.assertEquals(expectedJson, responseJson, new CustomComparator(
        JSONCompareMode.LENIENT, new Customization("timestamp", (o1, o2) -> true)));
  }
}
