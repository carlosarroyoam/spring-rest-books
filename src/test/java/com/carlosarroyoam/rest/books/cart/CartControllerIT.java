package com.carlosarroyoam.rest.books.cart;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.carlosarroyoam.rest.books.cart.dto.UpdateCartItemRequestDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
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
    mockMvc.perform(get("/carts"))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.id").value(1L))
        .andExpect(jsonPath("$.items[0].id").value(1L))
        .andExpect(jsonPath("$.items[0].quantity").value(1))
        .andExpect(jsonPath("$.items[0].book.id").value(1))
        .andExpect(jsonPath("$.items[0].book.isbn").value("978-1-3035-0529-4"))
        .andExpect(
            jsonPath("$.items[0].book.title").value("Homo Deus: A Brief History of Tomorrow"))
        .andExpect(jsonPath("$.items[0].book.cover_url")
            .value("https://images.isbndb.com/covers/39/36/9781784703936.jpg"))
        .andExpect(jsonPath("$.items[0].book.price").value(22.99))
        .andExpect(jsonPath("$.items[0].book.is_available_online").value(false))
        .andExpect(jsonPath("$.items[0].book.published_at").value("2017-01-01"))
        .andExpect(jsonPath("$.items[0].book.authors").isArray())
        .andExpect(jsonPath("$.items[0].book.authors[0].id").value(1))
        .andExpect(jsonPath("$.items[0].book.authors[0].name").value("Yuval Noah Harari"))
        .andExpect(jsonPath("$.items[0].added_at").isNotEmpty())
        .andExpect(jsonPath("$.user.id").value(1))
        .andExpect(jsonPath("$.user.name").value("Carlos Alberto Arroyo Martínez"))
        .andExpect(jsonPath("$.user.age").value(28))
        .andExpect(jsonPath("$.user.email").value("carroyom@mail.com"))
        .andExpect(jsonPath("$.user.username").value("carroyom"))
        .andExpect(jsonPath("$.user.is_active").value(Boolean.TRUE))
        .andExpect(jsonPath("$.user.role.id").value(1))
        .andExpect(jsonPath("$.user.role.title").value("App//Admin"))
        .andExpect(jsonPath("$.user.role.description").value("Role for admins users"));
  }

  @Test
  @DisplayName("Should return no content when update cart item with valid data")
  void shouldReturnNoContentWhenUpdateCartItemWithValidData() throws Exception {
    UpdateCartItemRequestDto requestDto = UpdateCartItemRequestDto.builder()
        .quantity(1)
        .bookId(1L)
        .build();

    mockMvc.perform(put("/carts/{cartId}/items", 1L).content(mapper.writeValueAsString(requestDto))
        .contentType(MediaType.APPLICATION_JSON)
        .accept(MediaType.APPLICATION_JSON)).andExpect(status().isNoContent());
  }

  @Test
  @DisplayName("Should throw AppExceptionDto when update cart item with non existing cart id")
  void shouldThrowWhenUpdateCartItemWithNonExistingCartId() throws Exception {
    UpdateCartItemRequestDto requestDto = UpdateCartItemRequestDto.builder()
        .quantity(1)
        .bookId(1L)
        .build();

    mockMvc
        .perform(put("/carts/{cartId}/items", 1000L).content(mapper.writeValueAsString(requestDto))
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isNotFound())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
        .andExpect(jsonPath("$.error").value("Not Found"))
        .andExpect(jsonPath("$.message").value("Cart not found"))
        .andExpect(jsonPath("$.status").value(404));
  }

  @Test
  @DisplayName("Should throw AppExceptionDto when update cart item with non existing book id")
  void shouldThrowWhenUpdateCartItemWithNonExistingBookId() throws Exception {
    UpdateCartItemRequestDto requestDto = UpdateCartItemRequestDto.builder()
        .quantity(1)
        .bookId(1000L)
        .build();

    mockMvc
        .perform(put("/carts/{cartId}/items", 1L).content(mapper.writeValueAsString(requestDto))
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isNotFound())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
        .andExpect(jsonPath("$.error").value("Not Found"))
        .andExpect(jsonPath("$.message").value("Book not found"))
        .andExpect(jsonPath("$.status").value(404));
  }

  @Test
  @DisplayName("Should return no content when delete cart item with existing id")
  void shouldReturnNoContentWhenDeleteCartItemWithExistingId() throws Exception {
    mockMvc
        .perform(
            delete("/carts/{cartId}/items/{cartItemId}", 1L, 1L).accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isNoContent());
  }

  @Test
  @DisplayName("Should throw AppExceptionDto when delete cart item with non existing cart id")
  void shouldThrowWhenDeleteCartItemWithNonExistingCartId() throws Exception {
    mockMvc
        .perform(delete("/carts/{cartId}/items/{cartItemId}", 1000L, 1L)
            .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isNotFound())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
        .andExpect(jsonPath("$.error").value("Not Found"))
        .andExpect(jsonPath("$.message").value("Cart not found"))
        .andExpect(jsonPath("$.status").value(404));
  }

  @Test
  @DisplayName("Should throw AppExceptionDto when delete cart item with non existing cart item id")
  void shouldThrowWhenDeleteCartItemWithNonExistingCartItemId() throws Exception {
    mockMvc
        .perform(delete("/carts/{cartId}/items/{cartItemId}", 1L, 1000L)
            .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isNotFound())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
        .andExpect(jsonPath("$.error").value("Not Found"))
        .andExpect(jsonPath("$.message").value("Cart item not found"))
        .andExpect(jsonPath("$.status").value(404));
  }
}
