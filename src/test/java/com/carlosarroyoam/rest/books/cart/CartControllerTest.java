package com.carlosarroyoam.rest.books.cart;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.carlosarroyoam.rest.books.cart.dto.CartResponse;
import com.carlosarroyoam.rest.books.cart.dto.UpdateCartItemRequest;
import com.carlosarroyoam.rest.books.core.constant.MessageKeys;
import com.carlosarroyoam.rest.books.core.exception.ExceptionLogger;
import com.carlosarroyoam.rest.books.core.exception.GlobalExceptionHandler;
import com.carlosarroyoam.rest.books.core.exception.ProblemDetailFactory;
import com.carlosarroyoam.rest.books.core.exception.ResourceNotFoundException;
import com.carlosarroyoam.rest.books.support.security.JwtArgumentResolver;
import com.carlosarroyoam.rest.books.support.testutils.TestMessageSources;
import com.carlosarroyoam.rest.books.support.testutils.TestObjectMappers;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
class CartControllerTest {
  @Mock private CartService cartService;
  @InjectMocks private CartController cartController;

  private ObjectMapper mapper;
  private MockMvc mockMvc;

  @BeforeEach
  void setup() {
    mapper = TestObjectMappers.snakeCase();

    mockMvc =
        MockMvcBuilders.standaloneSetup(cartController)
            .setControllerAdvice(
                new GlobalExceptionHandler(
                    new ProblemDetailFactory(),
                    new ExceptionLogger(),
                    TestMessageSources.messages()))
            .setMessageConverters(
                new MappingJackson2HttpMessageConverter(TestObjectMappers.snakeCase()))
            .setCustomArgumentResolvers(new JwtArgumentResolver())
            .build();
  }

  @Test
  @DisplayName("GET /carts - Given cart exists, when find by customer id, then returns cart")
  void givenCartExists_whenFindCartByCustomerId_thenReturnsCart() throws Exception {
    CartResponse cartResponse = CartResponse.builder().id(1L).build();

    when(cartService.findByCustomerId(anyLong())).thenReturn(cartResponse);

    mockMvc
        .perform(get("/carts").accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.id").value(1));
  }

  @Test
  @DisplayName(
      "GET /carts - Given cart does not exist, when find by customer id, then returns not found")
  void givenCartDoesNotExist_whenFindCartByCustomerId_thenReturnsNotFound() throws Exception {
    when(cartService.findByCustomerId(anyLong()))
        .thenThrow(new ResourceNotFoundException(MessageKeys.CART_NOT_FOUND_EXCEPTION));

    mockMvc
        .perform(get("/carts").accept(MediaType.APPLICATION_JSON))
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
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isNoContent());
  }

  @Test
  @DisplayName("PUT /carts/items - Given book does not exist, when update, then returns not found")
  void givenBookDoesNotExist_whenUpdateCartItem_thenReturnsNotFound() throws Exception {
    UpdateCartItemRequest request =
        UpdateCartItemRequest.builder().quantity(1).bookId(9999L).build();

    doThrow(new ResourceNotFoundException(MessageKeys.BOOK_NOT_FOUND_EXCEPTION))
        .when(cartService)
        .updateCartItem(anyLong(), any(UpdateCartItemRequest.class));

    mockMvc
        .perform(
            put("/carts/items")
                .content(mapper.writeValueAsString(request))
                .contentType(MediaType.APPLICATION_JSON))
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
        .perform(put("/carts/items").content("{}").contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isUnprocessableEntity())
        .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
        .andExpect(jsonPath("$.status").value(422))
        .andExpect(jsonPath("$.title").value("Unprocessable Entity"))
        .andExpect(jsonPath("$.detail").value("Invalid request data"))
        .andExpect(jsonPath("$.instance").value("/carts/items"))
        .andExpect(jsonPath("$.errors").isMap())
        .andExpect(jsonPath("$.errors.bookId").value("BookId should not be null"))
        .andExpect(jsonPath("$.errors.quantity").value("Quantity should not be null"));

    verifyNoInteractions(cartService);
  }

  @Test
  @DisplayName(
      "DELETE /carts/items/{id} - Given cart item exists, when delete, then returns no content")
  void givenCartItemExists_whenDeleteCartItem_thenReturnsNoContent() throws Exception {
    mockMvc.perform(delete("/carts/items/{cartItemId}", 1L)).andExpect(status().isNoContent());
  }

  @Test
  @DisplayName(
      "DELETE /carts/items/{id} - Given cart does not exist, when delete, then returns not found")
  void givenCartDoesNotExist_whenDeleteCartItem_thenReturnsNotFound() throws Exception {
    doThrow(new ResourceNotFoundException(MessageKeys.CART_NOT_FOUND_EXCEPTION))
        .when(cartService)
        .deleteCartItem(anyLong(), anyLong());

    mockMvc
        .perform(delete("/carts/items/{cartItemId}", 1L))
        .andExpect(status().isNotFound())
        .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
        .andExpect(jsonPath("$.status").value(404))
        .andExpect(jsonPath("$.title").value("Not Found"))
        .andExpect(jsonPath("$.detail").value("Cart not found"))
        .andExpect(jsonPath("$.instance").value("/carts/items/1"));
  }
}
