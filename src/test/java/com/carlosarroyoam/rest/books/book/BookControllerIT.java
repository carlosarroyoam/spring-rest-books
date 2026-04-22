package com.carlosarroyoam.rest.books.book;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.carlosarroyoam.rest.books.book.dto.CreateBookRequest;
import com.carlosarroyoam.rest.books.book.dto.UpdateBookRequest;
import com.carlosarroyoam.rest.books.common.JsonUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Transactional
class BookControllerIT {
  @Autowired private WebApplicationContext webApplicationContext;

  @Autowired private ObjectMapper mapper;

  @Autowired private MockMvc mockMvc;

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
    String expectedJson = JsonUtils.readJson("/books/find-all.json");

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
    String expectedJson = JsonUtils.readJson("/books/find-by-id.json");

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
  @DisplayName("DELETE /books/{id} - Given book exists, when delete, then returns no content")
  void givenBookExists_whenDeleteBook_thenReturnsNoContent() throws Exception {
    mockMvc.perform(delete("/books/{bookId}", 1L)).andExpect(status().isNoContent());
  }

  @Test
  @DisplayName(
      "GET /books/{id}/authors - Given book exists, when find authors, then returns authors")
  void givenBookExists_whenFindAuthorsByBookId_thenReturnsAuthors() throws Exception {
    String expectedJson = JsonUtils.readJson("/books/find-authors-by-book.json");

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
}
