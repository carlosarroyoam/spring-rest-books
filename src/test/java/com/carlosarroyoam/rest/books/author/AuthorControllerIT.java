package com.carlosarroyoam.rest.books.author;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.carlosarroyoam.rest.books.author.dto.CreateAuthorRequestDto;
import com.carlosarroyoam.rest.books.author.dto.UpdateAuthorRequestDto;
import com.fasterxml.jackson.databind.ObjectMapper;
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
    mockMvc.perform(get("/authors").param("page", "0").param("size", "25"))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
        .andExpect(jsonPath("$[0].id").value(1L))
        .andExpect(jsonPath("$[0].name").value("Yuval Noah Harari"));
  }

  @Test
  @DisplayName("Should return AuthorDto when find author by id with existing id")
  void shouldReturnAuthorDtoWhenFindAuthorByIdWithExistingId() throws Exception {
    mockMvc.perform(get("/authors/{authorId}", 1L))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
        .andExpect(jsonPath("$.id").value(1L))
        .andExpect(jsonPath("$.name").value("Yuval Noah Harari"));
  }

  @Test
  @DisplayName("Should throw AppExceptionDto when find author by id with non existing id")
  void shouldThrowWhenFindAuthorByIdWithNonExistingId() throws Exception {
    mockMvc.perform(get("/authors/{authorId}", 1000L))
        .andExpect(status().isNotFound())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
        .andExpect(jsonPath("$.error").value("Not Found"))
        .andExpect(jsonPath("$.message").value("Author not found"))
        .andExpect(jsonPath("$.status").value(404));
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
    UpdateAuthorRequestDto requestDto = UpdateAuthorRequestDto.builder().name("Yuval Noah").build();

    mockMvc
        .perform(put("/authors/{authorId}", 1000L).contentType(MediaType.APPLICATION_JSON)
            .content(mapper.writeValueAsString(requestDto)))
        .andExpect(status().isNotFound())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
        .andExpect(jsonPath("$.error").value("Not Found"))
        .andExpect(jsonPath("$.message").value("Author not found"))
        .andExpect(jsonPath("$.status").value(404));
  }

  @Test
  @DisplayName("Should return no content when delete author with existing id")
  void shouldReturnNoContentWhenDeleteAuthorWithExistingId() throws Exception {
    mockMvc.perform(delete("/authors/{authorId}", 1L)).andExpect(status().isNoContent());
  }

  @Test
  @DisplayName("Should throw AppExceptionDto when delete author with non existing id")
  void shouldThrowWhenDeleteAuthorWithNonExistingId() throws Exception {
    mockMvc.perform(delete("/authors/{authorId}", 1000L))
        .andExpect(status().isNotFound())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
        .andExpect(jsonPath("$.error").value("Not Found"))
        .andExpect(jsonPath("$.message").value("Author not found"))
        .andExpect(jsonPath("$.status").value(404));
  }

  @Test
  @DisplayName("Should return List<BookDto> when find books by author id with existing id")
  void shouldReturnListOfBooksWhenFindBooksByAuthorIdWithExistingId() throws Exception {
    mockMvc.perform(get("/authors/{authorId}/books", 1L))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
        .andExpect(jsonPath("$[0].id").value(1L))
        .andExpect(jsonPath("$[0].isbn").value("978-1-3035-0529-4"))
        .andExpect(jsonPath("$[0].title").value("Homo Deus: A Brief History of Tomorrow"))
        .andExpect(jsonPath("$[0].cover_url")
            .value("https://images.isbndb.com/covers/39/36/9781784703936.jpg"))
        .andExpect(jsonPath("$[0].price").value(22.99))
        .andExpect(jsonPath("$[0].is_available_online").value(false))
        .andExpect(jsonPath("$[0].published_at").value("2017-01-01"));
  }
}
