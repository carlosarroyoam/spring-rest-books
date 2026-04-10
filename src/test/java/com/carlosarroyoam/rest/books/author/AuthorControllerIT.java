package com.carlosarroyoam.rest.books.author;

import com.carlosarroyoam.rest.books.author.dto.CreateAuthorRequest;
import com.carlosarroyoam.rest.books.author.dto.UpdateAuthorRequest;
import com.carlosarroyoam.rest.books.common.JsonUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
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
        .defaultRequest(get("/").with(jwt().jwt(jwt -> jwt.claim("preferred_username", "carroyom"))
            .authorities(new SimpleGrantedAuthority("ROLE_App/Admin"))))
        .build();
  }

  @Test
  @DisplayName("GET /authors - Should return paged authors when find all authors")
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
  @DisplayName("GET /authors/{authorId} - Should return AuthorResponse when find author by id with existing id")
  void shouldReturnAuthorResponseWhenFindAuthorByIdWithExistingId() throws Exception {
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
  @DisplayName("POST /authors - Should return created when create an author with valid data")
  void shouldReturnCreatedWhenCreateAuthorWithValidData() throws Exception {
    CreateAuthorRequest request = CreateAuthorRequest.builder().name("Yuval Noah Harari").build();

    mockMvc
        .perform(post("/authors").contentType(MediaType.APPLICATION_JSON)
            .content(mapper.writeValueAsString(request)))
        .andExpect(status().isCreated())
        .andExpect(header().string("Location", "http://localhost/authors/3"));
  }

  @Test
  @DisplayName("PUT /authors/{authorId} - Should return no content when update author with valid data")
  void shouldReturnNoContentWhenUpdateAuthorWithValidData() throws Exception {
    UpdateAuthorRequest request = UpdateAuthorRequest.builder().name("Yuval Noah").build();

    mockMvc.perform(put("/authors/{authorId}", 1L).contentType(MediaType.APPLICATION_JSON)
        .content(mapper.writeValueAsString(request))).andExpect(status().isNoContent());
  }

  @Test
  @DisplayName("DELETE /authors/{authorId} - Should return no content when delete author with existing id")
  void shouldReturnNoContentWhenDeleteAuthorWithExistingId() throws Exception {
    mockMvc.perform(delete("/authors/{authorId}", 1L)).andExpect(status().isNoContent());
  }

  @Test
  @DisplayName("GET /authors/{authorId}/books - Should return List<BookResponse> when find books by author id with existing id")
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
