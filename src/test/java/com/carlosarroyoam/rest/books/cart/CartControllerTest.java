package com.carlosarroyoam.rest.books.cart;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

import com.carlosarroyoam.rest.books.cart.dto.CartDto;
import com.carlosarroyoam.rest.books.cart.dto.UpdateCartItemRequestDto;
import com.carlosarroyoam.rest.books.common.JwtArgumentResolver;
import com.carlosarroyoam.rest.books.core.exception.ControllerAdvisor;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

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
        .setControllerAdvice(ControllerAdvisor.class)
        .setCustomArgumentResolvers(new JwtArgumentResolver())
        .build();
  }

  @Test
  @DisplayName("Should return CartDto when find cart by customer id")
  void shouldReturnCartDtoWhenFindCartByCustomerId() throws Exception {
    CartDto cartDto = CartDto.builder().build();

    when(cartService.findByCustomerId(anyLong())).thenReturn(cartDto);

    MvcResult mvcResult = mockMvc.perform(get("/carts").accept(MediaType.APPLICATION_JSON))
        .andReturn();

    String responseJson = mvcResult.getResponse().getContentAsString();
    CartDto responseDto = mapper.readValue(responseJson, CartDto.class);

    assertThat(mvcResult.getResponse().getStatus()).isEqualTo(HttpStatus.OK.value());
    assertThat(mvcResult.getResponse().getContentType())
        .isEqualTo(MediaType.APPLICATION_JSON_VALUE);
    assertThat(responseDto).isNotNull();
  }

  @Test
  @DisplayName("Should return no content when update cart item")
  void shouldReturnNoContentWhenUpdateCartItem() throws Exception {
    UpdateCartItemRequestDto requestDto = UpdateCartItemRequestDto.builder()
        .quantity(1)
        .bookId(1L)
        .build();

    MvcResult mvcResult = mockMvc
        .perform(put("/carts/items").content(mapper.writeValueAsString(requestDto))
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))
        .andReturn();

    assertThat(mvcResult.getResponse().getStatus()).isEqualTo(HttpStatus.NO_CONTENT.value());
  }

  @Test
  @DisplayName("Should return no content when delete cart item")
  void shouldReturnNoContentWhenDeleteCartItem() throws Exception {
    MvcResult mvcResult = mockMvc
        .perform(delete("/carts/items/{cartItemId}", 1L).accept(MediaType.APPLICATION_JSON))
        .andReturn();

    assertThat(mvcResult.getResponse().getStatus()).isEqualTo(HttpStatus.NO_CONTENT.value());
  }
}
