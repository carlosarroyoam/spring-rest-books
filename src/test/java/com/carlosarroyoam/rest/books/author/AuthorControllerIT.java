package com.carlosarroyoam.rest.books.author;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.carlosarroyoam.rest.books.author.dto.CreateAuthorRequestDto;
import com.carlosarroyoam.rest.books.author.dto.UpdateAuthorRequestDto;
import com.carlosarroyoam.rest.books.common.JsonUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Transactional
class AuthorControllerIT {
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
  @DisplayName("Should return authors when find all authors")
  void shouldReturnListOfAuthorsWhenFindAllAuthors() throws Exception {
    String expectedJson = JsonUtils.readJson("/authors/find-all.json");

    String responseJson = mockMvc.perform(get("/authors").param("page", "0").param("size", "25"))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
        .andReturn()
        .getResponse()
        .getContentAsString();

    JSONAssert.assertEquals(expectedJson, responseJson, false);
  }

  @Test
  @DisplayName("Should return AuthorDto when find author by id with existing id")
  void shouldReturnAuthorDtoWhenFindAuthorByIdWithExistingId() throws Exception {
    String expectedJson = JsonUtils.readJson("/authors/find-by-id.json");

    String responseJson = mockMvc.perform(get("/authors/{authorId}", 1L))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
        .andReturn()
        .getResponse()
        .getContentAsString();

    JSONAssert.assertEquals(expectedJson, responseJson, false);
  }

  @Test
  @DisplayName("Should throw AppExceptionDto when find author by id with non existing id")
  void shouldThrowWhenFindAuthorByIdWithNonExistingId() throws Exception {
    String expectedJson = JsonUtils.readJson("/authors/find-by-id_with_non_existing_id.json");

    String responseJson = mockMvc.perform(get("/authors/{authorId}", 1000L))
        .andExpect(status().isNotFound())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
        .andReturn()
        .getResponse()
        .getContentAsString();

    JSONAssert.assertEquals(expectedJson, responseJson, new CustomComparator(
        JSONCompareMode.LENIENT, new Customization("timestamp", (o1, o2) -> true)));
  }

  @Test
  @DisplayName("Should return created when create an author with valid data")
  void shouldReturnCreatedWhenCreateAuthorWithValidData() throws Exception {
    CreateAuthorRequestDto requestDto = CreateAuthorRequestDto.builder()
        .name("Yuval Noah Harari")
        .build();

    mockMvc
        .perform(post("/authors").contentType(MediaType.APPLICATION_JSON)
            .content(mapper.writeValueAsString(requestDto)))
        .andExpect(status().isCreated())
        .andExpect(header().string("Location", "http://localhost/authors/3"));
  }

  @Test
  @DisplayName("Should return no content when update author with valid data")
  void shouldReturnNoContentWhenUpdateAuthorWithValidData() throws Exception {
    UpdateAuthorRequestDto requestDto = UpdateAuthorRequestDto.builder().name("Yuval Noah").build();

    mockMvc.perform(put("/authors/{authorId}", 1L).contentType(MediaType.APPLICATION_JSON)
        .content(mapper.writeValueAsString(requestDto))).andExpect(status().isNoContent());
  }

  @Test
  @DisplayName("Should throw AppExceptionDto when update author with non existing id")
  void shouldThrowWhenUpdateAuthorWithNonExistingId() throws Exception {
    String expectedJson = JsonUtils.readJson("/authors/update_with_non_existing_id.json");

    UpdateAuthorRequestDto requestDto = UpdateAuthorRequestDto.builder().name("Yuval Noah").build();

    String responseJson = mockMvc
        .perform(put("/authors/{authorId}", 1000L).contentType(MediaType.APPLICATION_JSON)
            .content(mapper.writeValueAsString(requestDto)))
        .andExpect(status().isNotFound())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
        .andReturn()
        .getResponse()
        .getContentAsString();

    JSONAssert.assertEquals(expectedJson, responseJson, new CustomComparator(
        JSONCompareMode.LENIENT, new Customization("timestamp", (o1, o2) -> true)));
  }

  @Test
  @DisplayName("Should return no content when delete author with existing id")
  void shouldReturnNoContentWhenDeleteAuthorWithExistingId() throws Exception {
    mockMvc.perform(delete("/authors/{authorId}", 1L)).andExpect(status().isNoContent());
  }

  @Test
  @DisplayName("Should throw AppExceptionDto when delete author with non existing id")
  void shouldThrowWhenDeleteAuthorWithNonExistingId() throws Exception {
    String expectedJson = JsonUtils.readJson("/authors/delete_with_non_existing_id.json");

    String responseJson = mockMvc.perform(delete("/authors/{authorId}", 1000L))
        .andExpect(status().isNotFound())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
        .andReturn()
        .getResponse()
        .getContentAsString();

    JSONAssert.assertEquals(expectedJson, responseJson, new CustomComparator(
        JSONCompareMode.LENIENT, new Customization("timestamp", (o1, o2) -> true)));
  }

  @Test
  @DisplayName("Should return List<BookDto> when find books by author id with existing id")
  void shouldReturnListOfBooksWhenFindBooksByAuthorIdWithExistingId() throws Exception {
    String expectedJson = JsonUtils.readJson("/authors/find-books-by-author.json");

    String responseJson = mockMvc.perform(get("/authors/{authorId}/books", 1L))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
        .andReturn()
        .getResponse()
        .getContentAsString();

    JSONAssert.assertEquals(expectedJson, responseJson, false);
  }
}
