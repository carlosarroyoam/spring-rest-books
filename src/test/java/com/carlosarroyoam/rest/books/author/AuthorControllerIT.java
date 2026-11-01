package com.carlosarroyoam.rest.books.author;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.anonymous;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.carlosarroyoam.rest.books.author.dto.CreateAuthorRequest;
import com.carlosarroyoam.rest.books.author.dto.UpdateAuthorRequest;
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
class AuthorControllerIT {
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
                            .jwt(jwt -> jwt.claim("preferred_username", "carroyom"))
                            .authorities(new SimpleGrantedAuthority("ROLE_App/Admin"))))
            .build();
  }

  @Test
  @DisplayName("GET /authors - Given authors exist, when find all, then returns paged authors")
  void givenAuthorsExist_whenFindAllAuthors_thenReturnsPagedAuthors() throws Exception {
    String expectedJson = JsonUtils.readJson("authors/find-all.json");

    String responseJson =
        mockMvc
            .perform(get("/authors").param("page", "0").param("size", "25"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andReturn()
            .getResponse()
            .getContentAsString();

    JSONAssert.assertEquals(expectedJson, responseJson, false);
  }

  @Test
  @DisplayName("GET /authors/{id} - Given author exists, when find by id, then returns author")
  void givenAuthorExists_whenFindAuthorById_thenReturnsAuthor() throws Exception {
    String expectedJson = JsonUtils.readJson("authors/find-by-id.json");

    String responseJson =
        mockMvc
            .perform(get("/authors/{authorId}", 1L))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andReturn()
            .getResponse()
            .getContentAsString();

    JSONAssert.assertEquals(expectedJson, responseJson, false);
  }

  @Test
  @DisplayName(
      "GET /authors/{id} - Given author does not exist, when find by id, then returns not found")
  void givenAuthorDoesNotExist_whenFindAuthorById_thenReturnsNotFound() throws Exception {
    mockMvc
        .perform(get("/authors/{authorId}", 9999L))
        .andExpect(status().isNotFound())
        .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
        .andExpect(jsonPath("$.status").value(404))
        .andExpect(jsonPath("$.title").value("Not Found"))
        .andExpect(jsonPath("$.detail").value("Author not found"))
        .andExpect(jsonPath("$.instance").value("/authors/9999"));
  }

  @Test
  @DisplayName("POST /authors - Given valid author data, when create, then returns created")
  void givenValidAuthorData_whenCreateAuthor_thenReturnsCreated() throws Exception {
    CreateAuthorRequest request = CreateAuthorRequest.builder().name("Yuval Noah Harari").build();

    mockMvc
        .perform(
            post("/authors")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(request)))
        .andExpect(status().isCreated())
        .andExpect(header().string("Location", "http://localhost/authors/3"));
  }

  @Test
  @DisplayName("POST /authors - Given anonymous user, when create, then returns unauthorized")
  void givenAnonymousUser_whenCreateAuthor_thenReturnsUnauthorized() throws Exception {
    mockMvc
        .perform(
            post("/authors")
                .with(anonymous())
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(validCreateRequest())))
        .andExpect(status().isUnauthorized())
        .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
        .andExpect(jsonPath("$.status").value(401))
        .andExpect(jsonPath("$.title").value("Unauthorized"))
        .andExpect(jsonPath("$.instance").value("/authors"));
  }

  @Test
  @DisplayName("POST /authors - Given customer role, when create, then returns forbidden")
  void givenCustomerRole_whenCreateAuthor_thenReturnsForbidden() throws Exception {
    mockMvc
        .perform(
            post("/authors")
                .with(customerJwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(validCreateRequest())))
        .andExpect(status().isForbidden())
        .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
        .andExpect(jsonPath("$.status").value(403))
        .andExpect(jsonPath("$.title").value("Forbidden"))
        .andExpect(jsonPath("$.instance").value("/authors"));
  }

  @Test
  @DisplayName(
      "POST /authors - Given invalid author data, when create, then returns problem detail")
  void givenInvalidAuthorData_whenCreateAuthor_thenReturnsUnprocessableEntity() throws Exception {
    mockMvc
        .perform(post("/authors").contentType(MediaType.APPLICATION_JSON).content("{}"))
        .andExpect(status().isUnprocessableEntity())
        .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
        .andExpect(jsonPath("$.status").value(422))
        .andExpect(jsonPath("$.title").value("Unprocessable Entity"))
        .andExpect(jsonPath("$.detail").value("Invalid request data"))
        .andExpect(jsonPath("$.instance").value("/authors"))
        .andExpect(jsonPath("$.errors").isMap())
        .andExpect(jsonPath("$.errors.name").isNotEmpty());
  }

  @Test
  @DisplayName("PUT /authors/{id} - Given valid author data, when update, then returns no content")
  void givenValidAuthorData_whenUpdateAuthor_thenReturnsNoContent() throws Exception {
    UpdateAuthorRequest request = UpdateAuthorRequest.builder().name("Yuval Noah").build();

    mockMvc
        .perform(
            put("/authors/{authorId}", 1L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(request)))
        .andExpect(status().isNoContent());
  }

  @Test
  @DisplayName("PUT /authors/{id} - Given customer role, when update, then returns forbidden")
  void givenCustomerRole_whenUpdateAuthor_thenReturnsForbidden() throws Exception {
    mockMvc
        .perform(
            put("/authors/{authorId}", 1L)
                .with(customerJwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(validUpdateRequest())))
        .andExpect(status().isForbidden())
        .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
        .andExpect(jsonPath("$.status").value(403))
        .andExpect(jsonPath("$.title").value("Forbidden"))
        .andExpect(jsonPath("$.instance").value("/authors/1"));
  }

  @Test
  @DisplayName(
      "PUT /authors/{id} - Given author does not exist, when update, then returns not found")
  void givenAuthorDoesNotExist_whenUpdateAuthor_thenReturnsNotFound() throws Exception {
    mockMvc
        .perform(
            put("/authors/{authorId}", 9999L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(validUpdateRequest())))
        .andExpect(status().isNotFound())
        .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
        .andExpect(jsonPath("$.status").value(404))
        .andExpect(jsonPath("$.title").value("Not Found"))
        .andExpect(jsonPath("$.detail").value("Author not found"));
  }

  @Test
  @DisplayName(
      "PUT /authors/{id} - Given invalid author data, when update, then returns problem detail")
  void givenInvalidAuthorData_whenUpdateAuthor_thenReturnsUnprocessableEntity() throws Exception {
    mockMvc
        .perform(
            put("/authors/{authorId}", 1L)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
        .andExpect(status().isUnprocessableEntity())
        .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
        .andExpect(jsonPath("$.status").value(422))
        .andExpect(jsonPath("$.title").value("Unprocessable Entity"))
        .andExpect(jsonPath("$.detail").value("Invalid request data"))
        .andExpect(jsonPath("$.instance").value("/authors/1"))
        .andExpect(jsonPath("$.errors").isMap())
        .andExpect(jsonPath("$.errors.name").isNotEmpty());
  }

  @Test
  @DisplayName("DELETE /authors/{id} - Given author exists, when delete, then returns no content")
  void givenAuthorExists_whenDeleteAuthor_thenReturnsNoContent() throws Exception {
    mockMvc.perform(delete("/authors/{authorId}", 1L)).andExpect(status().isNoContent());
  }

  @Test
  @DisplayName("DELETE /authors/{id} - Given customer role, when delete, then returns forbidden")
  void givenCustomerRole_whenDeleteAuthor_thenReturnsForbidden() throws Exception {
    mockMvc
        .perform(delete("/authors/{authorId}", 1L).with(customerJwt()))
        .andExpect(status().isForbidden())
        .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
        .andExpect(jsonPath("$.status").value(403))
        .andExpect(jsonPath("$.title").value("Forbidden"))
        .andExpect(jsonPath("$.instance").value("/authors/1"));
  }

  @Test
  @DisplayName(
      "DELETE /authors/{id} - Given author does not exist, when delete, then returns not found")
  void givenAuthorDoesNotExist_whenDeleteAuthor_thenReturnsNotFound() throws Exception {
    mockMvc
        .perform(delete("/authors/{authorId}", 9999L))
        .andExpect(status().isNotFound())
        .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
        .andExpect(jsonPath("$.status").value(404))
        .andExpect(jsonPath("$.title").value("Not Found"))
        .andExpect(jsonPath("$.detail").value("Author not found"));
  }

  @Test
  @DisplayName("GET /authors/{id}/books - Given author exists, when find books, then returns books")
  void givenAuthorExists_whenFindBooksByAuthorId_thenReturnsBooks() throws Exception {
    String expectedJson = JsonUtils.readJson("authors/find-books-by-author.json");

    String responseJson =
        mockMvc
            .perform(get("/authors/{authorId}/books", 1L))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andReturn()
            .getResponse()
            .getContentAsString();

    JSONAssert.assertEquals(expectedJson, responseJson, false);
  }

  private static RequestPostProcessor customerJwt() {
    return jwt()
        .jwt(jwt -> jwt.claim("preferred_username", "customer"))
        .authorities(new SimpleGrantedAuthority("ROLE_App/Customer"));
  }

  private static CreateAuthorRequest validCreateRequest() {
    return CreateAuthorRequest.builder().name("Yuval Noah Harari").build();
  }

  private static UpdateAuthorRequest validUpdateRequest() {
    return UpdateAuthorRequest.builder().name("Yuval Noah").build();
  }
}
