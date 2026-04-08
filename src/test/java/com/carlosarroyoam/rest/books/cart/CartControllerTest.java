package com.carlosarroyoam.rest.books.cart;

import com.carlosarroyoam.rest.books.cart.dto.CartResponse;
import com.carlosarroyoam.rest.books.cart.dto.UpdateCartItemRequest;
import com.carlosarroyoam.rest.books.common.JwtArgumentResolver;
import com.carlosarroyoam.rest.books.core.exception.GlobalExceptionHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class CartControllerTest {
  private ObjectMapper mapper;
  private MockMvc mockMvc;

  @Mock
  private CartService cartService;

  @InjectMocks
  private CartController cartController;

  @BeforeEach
  void setup() {
    mapper = new ObjectMapper();
    mapper.findAndRegisterModules();

    mockMvc = MockMvcBuilders.standaloneSetup(cartController)
        .setControllerAdvice(GlobalExceptionHandler.class)
        .setCustomArgumentResolvers(new JwtArgumentResolver())
        .build();
  }

  @Test
  @DisplayName("Should return CartResponse when find cart by customer id")
  void shouldReturnCartResponseWhenFindCartByCustomerId() throws Exception {
    CartResponse cartResponse = CartResponse.builder().id(1L).build();

    when(cartService.findByCustomerId(anyLong())).thenReturn(cartResponse);

    mockMvc.perform(get("/carts").accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.id").value(1));
  }

  @Test
  @DisplayName("Should return no content when update cart item")
  void shouldReturnNoContentWhenUpdateCartItem() throws Exception {
    UpdateCartItemRequest request = UpdateCartItemRequest.builder().quantity(1).bookId(1L).build();

    mockMvc.perform(put("/carts/items").content(mapper.writeValueAsString(request))
        .contentType(MediaType.APPLICATION_JSON)).andExpect(status().isNoContent());
  }

  @Test
  @DisplayName("Should return no content when delete cart item")
  void shouldReturnNoContentWhenDeleteCartItem() throws Exception {
    mockMvc.perform(delete("/carts/items/{cartItemId}", 1L)).andExpect(status().isNoContent());
  }
}
