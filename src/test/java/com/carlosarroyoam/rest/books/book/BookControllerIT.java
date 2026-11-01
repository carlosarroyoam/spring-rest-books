package com.carlosarroyoam.rest.books.book;

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

import com.carlosarroyoam.rest.books.book.dto.CreateBookRequest;
import com.carlosarroyoam.rest.books.book.dto.UpdateBookRequest;
import com.carlosarroyoam.rest.books.support.testutils.JsonUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.time.LocalDate;
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
class BookControllerIT {
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
  @DisplayName("GET /books - Given books exist, when find all, then returns paged books")
  void givenBooksExist_whenFindAllBooks_thenReturnsPagedBooks() throws Exception {
    String expectedJson = JsonUtils.readJson("books/find-all.json");

    String responseJson =
        mockMvc
            .perform(get("/books").param("page", "0").param("size", "25"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andReturn()
            .getResponse()
            .getContentAsString();

    JSONAssert.assertEquals(expectedJson, responseJson, false);
  }

  @Test
  @DisplayName("GET /books/{id} - Given book exists, when find by id, then returns book")
  void givenBookExists_whenFindBookById_thenReturnsBook() throws Exception {
    String expectedJson = JsonUtils.readJson("books/find-by-id.json");

    String responseJson =
        mockMvc
            .perform(get("/books/{bookId}", 1L))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andReturn()
            .getResponse()
            .getContentAsString();

    JSONAssert.assertEquals(expectedJson, responseJson, false);
  }

  @Test
  @DisplayName(
      "GET /books/{id} - Given book does not exist, when find by id, then returns not found")
  void givenBookDoesNotExist_whenFindBookById_thenReturnsNotFound() throws Exception {
    mockMvc
        .perform(get("/books/{bookId}", 9999L))
        .andExpect(status().isNotFound())
        .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
        .andExpect(jsonPath("$.status").value(404))
        .andExpect(jsonPath("$.title").value("Not Found"))
        .andExpect(jsonPath("$.detail").value("Book not found"))
        .andExpect(jsonPath("$.instance").value("/books/9999"));
  }

  @Test
  @DisplayName("POST /books - Given valid book data, when create, then returns created")
  void givenValidBookData_whenCreateBook_thenReturnsCreated() throws Exception {
    CreateBookRequest request =
        CreateBookRequest.builder()
            .isbn("978-1-7873-3067-2")
            .title("21 Lessons for the 21st Century")
            .coverUrl("https://images.isbndb.com/covers/9835763482824.jpg")
            .price(new BigDecimal("47.20"))
            .publishedAt(LocalDate.parse("2018-08-30"))
            .isAvailableOnline(true)
            .build();

    mockMvc
        .perform(
            post("/books")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(request)))
        .andExpect(status().isCreated())
        .andExpect(header().string("Location", "http://localhost/books/3"));
  }

  @Test
  @DisplayName("POST /books - Given anonymous user, when create, then returns unauthorized")
  void givenAnonymousUser_whenCreateBook_thenReturnsUnauthorized() throws Exception {
    mockMvc
        .perform(
            post("/books")
                .with(anonymous())
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(validCreateRequest())))
        .andExpect(status().isUnauthorized())
        .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
        .andExpect(jsonPath("$.status").value(401))
        .andExpect(jsonPath("$.title").value("Unauthorized"))
        .andExpect(jsonPath("$.instance").value("/books"));
  }

  @Test
  @DisplayName("POST /books - Given customer role, when create, then returns forbidden")
  void givenCustomerRole_whenCreateBook_thenReturnsForbidden() throws Exception {
    mockMvc
        .perform(
            post("/books")
                .with(customerJwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(validCreateRequest())))
        .andExpect(status().isForbidden())
        .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
        .andExpect(jsonPath("$.status").value(403))
        .andExpect(jsonPath("$.title").value("Forbidden"))
        .andExpect(jsonPath("$.instance").value("/books"));
  }

  @Test
  @DisplayName("POST /books - Given invalid book data, when create, then returns problem detail")
  void givenInvalidBookData_whenCreateBook_thenReturnsUnprocessableEntity() throws Exception {
    mockMvc
        .perform(post("/books").contentType(MediaType.APPLICATION_JSON).content("{}"))
        .andExpect(status().isUnprocessableEntity())
        .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
        .andExpect(jsonPath("$.status").value(422))
        .andExpect(jsonPath("$.title").value("Unprocessable Entity"))
        .andExpect(jsonPath("$.detail").value("Invalid request data"))
        .andExpect(jsonPath("$.instance").value("/books"))
        .andExpect(jsonPath("$.errors").isMap())
        .andExpect(jsonPath("$.errors.isbn").isNotEmpty())
        .andExpect(jsonPath("$.errors.title").isNotEmpty());
  }

  @Test
  @DisplayName("PUT /books/{id} - Given valid book data, when update, then returns no content")
  void givenValidBookData_whenUpdateBook_thenReturnsNoContent() throws Exception {
    UpdateBookRequest request =
        UpdateBookRequest.builder()
            .isbn("978-9-7389-4434-3")
            .title("Sapiens: A Brief History of Humankind")
            .coverUrl("https://images.isbndb.com/covers/60/97/9780062316097.jpg")
            .price(new BigDecimal("20.99"))
            .publishedAt(LocalDate.parse("2021-12-01"))
            .isAvailableOnline(true)
            .build();

    mockMvc
        .perform(
            put("/books/{bookId}", 1L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(request)))
        .andExpect(status().isNoContent());
  }

  @Test
  @DisplayName("PUT /books/{id} - Given customer role, when update, then returns forbidden")
  void givenCustomerRole_whenUpdateBook_thenReturnsForbidden() throws Exception {
    mockMvc
        .perform(
            put("/books/{bookId}", 1L)
                .with(customerJwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(validUpdateRequest())))
        .andExpect(status().isForbidden())
        .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
        .andExpect(jsonPath("$.status").value(403))
        .andExpect(jsonPath("$.instance").value("/books/1"));
  }

  @Test
  @DisplayName("PUT /books/{id} - Given book does not exist, when update, then returns not found")
  void givenBookDoesNotExist_whenUpdateBook_thenReturnsNotFound() throws Exception {
    mockMvc
        .perform(
            put("/books/{bookId}", 9999L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(validUpdateRequest())))
        .andExpect(status().isNotFound())
        .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
        .andExpect(jsonPath("$.status").value(404))
        .andExpect(jsonPath("$.detail").value("Book not found"));
  }

  @Test
  @DisplayName(
      "PUT /books/{id} - Given invalid book data, when update, then returns problem detail")
  void givenInvalidBookData_whenUpdateBook_thenReturnsUnprocessableEntity() throws Exception {
    mockMvc
        .perform(put("/books/{bookId}", 1L).contentType(MediaType.APPLICATION_JSON).content("{}"))
        .andExpect(status().isUnprocessableEntity())
        .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
        .andExpect(jsonPath("$.status").value(422))
        .andExpect(jsonPath("$.instance").value("/books/1"))
        .andExpect(jsonPath("$.errors").isMap())
        .andExpect(jsonPath("$.errors.isbn").isNotEmpty());
  }

  @Test
  @DisplayName("DELETE /books/{id} - Given book exists, when delete, then returns no content")
  void givenBookExists_whenDeleteBook_thenReturnsNoContent() throws Exception {
    mockMvc.perform(delete("/books/{bookId}", 1L)).andExpect(status().isNoContent());
  }

  @Test
  @DisplayName("DELETE /books/{id} - Given customer role, when delete, then returns forbidden")
  void givenCustomerRole_whenDeleteBook_thenReturnsForbidden() throws Exception {
    mockMvc
        .perform(delete("/books/{bookId}", 1L).with(customerJwt()))
        .andExpect(status().isForbidden())
        .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
        .andExpect(jsonPath("$.status").value(403))
        .andExpect(jsonPath("$.instance").value("/books/1"));
  }

  @Test
  @DisplayName(
      "DELETE /books/{id} - Given book does not exist, when delete, then returns not found")
  void givenBookDoesNotExist_whenDeleteBook_thenReturnsNotFound() throws Exception {
    mockMvc
        .perform(delete("/books/{bookId}", 9999L))
        .andExpect(status().isNotFound())
        .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
        .andExpect(jsonPath("$.status").value(404))
        .andExpect(jsonPath("$.detail").value("Book not found"));
  }

  @Test
  @DisplayName(
      "GET /books/{id}/authors - Given book exists, when find authors, then returns authors")
  void givenBookExists_whenFindAuthorsByBookId_thenReturnsAuthors() throws Exception {
    String expectedJson = JsonUtils.readJson("books/find-authors-by-book.json");

    String responseJson =
        mockMvc
            .perform(get("/books/{bookId}/authors", 1L))
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

  private static CreateBookRequest validCreateRequest() {
    return CreateBookRequest.builder()
        .isbn("978-1-7873-3067-2")
        .title("21 Lessons for the 21st Century")
        .coverUrl("https://images.isbndb.com/covers/9835763482824.jpg")
        .price(new BigDecimal("47.20"))
        .publishedAt(LocalDate.parse("2018-08-30"))
        .isAvailableOnline(true)
        .build();
  }

  private static UpdateBookRequest validUpdateRequest() {
    return UpdateBookRequest.builder()
        .isbn("978-9-7389-4434-3")
        .title("Sapiens: A Brief History of Humankind")
        .coverUrl("https://images.isbndb.com/covers/60/97/9780062316097.jpg")
        .price(new BigDecimal("20.99"))
        .publishedAt(LocalDate.parse("2021-12-01"))
        .isAvailableOnline(true)
        .build();
  }
}
