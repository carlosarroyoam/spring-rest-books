package com.carlosarroyoam.rest.books.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import com.carlosarroyoam.rest.books.dto.AuthorDto;
import com.carlosarroyoam.rest.books.service.AuthorService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
class AuthorControllerTest {
  private MockMvc mockMvc;
  private JacksonTester<List<AuthorDto>> jsonAuthor;

  @Mock
  private AuthorService authorService;

  @InjectMocks
  private AuthorController authorController;

  @BeforeEach
  public void setup() {
    JacksonTester.initFields(this, new ObjectMapper());

    mockMvc = MockMvcBuilders.standaloneSetup(authorController)
        .build();
  }

  @Test
  @DisplayName("Should return List<AuthorDto> when find all authors")
  void shouldReturnListOfAuthors() throws Exception {
    List<AuthorDto> authors = List.of(AuthorDto.builder().build(), AuthorDto.builder().build());

    Mockito.when(authorService.findAll(any(), any())).thenReturn(authors);

    MvcResult mvcResult = mockMvc.perform(get("/authors")
        .queryParam("page", "0")
        .queryParam("size", "25")
        .accept(MediaType.APPLICATION_JSON))
        .andReturn();

    assertThat(mvcResult.getResponse().getStatus()).isEqualTo(HttpStatus.OK.value());
    assertThat(mvcResult.getResponse().getContentAsString()).isEqualTo(
        jsonAuthor.write(List.of(AuthorDto.builder().build(), AuthorDto.builder().build())).getJson()
    );
  }
}
