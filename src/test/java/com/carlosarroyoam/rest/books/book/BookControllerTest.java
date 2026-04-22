package com.carlosarroyoam.rest.books.book;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.carlosarroyoam.rest.books.author.dto.AuthorResponse;
import com.carlosarroyoam.rest.books.book.dto.BookResponse;
import com.carlosarroyoam.rest.books.book.dto.BookSpecs;
import com.carlosarroyoam.rest.books.book.dto.CreateBookRequest;
import com.carlosarroyoam.rest.books.book.dto.UpdateBookRequest;
import com.carlosarroyoam.rest.books.core.dto.PagedResponse;
import com.carlosarroyoam.rest.books.core.dto.PaginationResponse;
import com.carlosarroyoam.rest.books.core.exception.GlobalExceptionHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
class BookControllerTest {
  private ObjectMapper mapper;
  private MockMvc mockMvc;

  @Mock private BookService bookService;

  @InjectMocks private BookController bookController;

  @BeforeEach
  void setup() {
    mapper = new ObjectMapper();
    mapper.findAndRegisterModules();

    mockMvc =
        MockMvcBuilders.standaloneSetup(bookController)
            .setControllerAdvice(GlobalExceptionHandler.class)
            .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
            .build();
  }

  @Test
  @DisplayName("GET /books - Given books exist, when find all, then returns paged books")
  void givenBooksExist_whenFindAllBooks_thenReturnsPagedBooks() throws Exception {
    PagedResponse<BookResponse> pagedResponse =
        PagedResponse.<BookResponse>builder()
            .items(List.of(BookResponse.builder().id(1L).build()))
            .pagination(
                PaginationResponse.builder().page(0).size(25).totalItems(1).totalPages(1).build())
            .build();

    when(bookService.findAll(any(BookSpecs.class), any(Pageable.class))).thenReturn(pagedResponse);

    mockMvc
        .perform(
            get("/books")
                .queryParam("page", "0")
                .queryParam("size", "25")
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.items.length()").value(1))
        .andExpect(jsonPath("$.pagination.page").value(0))
        .andExpect(jsonPath("$.pagination.size").value(25))
        .andExpect(jsonPath("$.pagination.totalItems").value(1))
        .andExpect(jsonPath("$.pagination.totalPages").value(1));
  }

  @Test
  @DisplayName("GET /books/{id} - Given book exists, when find by id, then returns book")
  void givenBookExists_whenFindBookById_thenReturnsBook() throws Exception {
    BookResponse book = BookResponse.builder().id(1L).title("Sapiens").build();

    when(bookService.findById(anyLong())).thenReturn(book);

    mockMvc
        .perform(get("/books/{bookId}", 1L).accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.id").value(1))
        .andExpect(jsonPath("$.title").value("Sapiens"));
  }

  @Test
  @DisplayName("POST /books - Given valid book data, when create, then returns created")
  void givenValidBookData_whenCreateBook_thenReturnsCreated() throws Exception {
    CreateBookRequest request =
        CreateBookRequest.builder()
            .isbn("978-9-7389-4434-3")
            .title("Sapiens: A Brief History of Humankind")
            .coverUrl("https://images.isbndb.com/covers/60/97/9780062316097.jpg")
            .price(new BigDecimal("20.99"))
            .publishedAt(LocalDate.parse("2021-12-01"))
            .isAvailableOnline(Boolean.TRUE)
            .build();

    BookResponse book = BookResponse.builder().id(1L).build();

    when(bookService.create(any(CreateBookRequest.class))).thenReturn(book);

    mockMvc
        .perform(
            post("/books")
                .content(mapper.writeValueAsString(request))
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isCreated())
        .andExpect(header().string("location", "http://localhost/books/1"));
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
            .isAvailableOnline(Boolean.TRUE)
            .build();

    mockMvc
        .perform(
            put("/books/{bookId}", 1L)
                .content(mapper.writeValueAsString(request))
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isNoContent());
  }

  @Test
  @DisplayName("DELETE /books/{id} - Given book exists, when delete, then returns no content")
  void givenBookExists_whenDeleteBook_thenReturnsNoContent() throws Exception {
    mockMvc
        .perform(delete("/books/{bookId}", 1L).accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isNoContent());
  }

  @Test
  @DisplayName(
      "GET /books/{id}/authors - Given book exists, when find authors, then returns authors")
  void givenBookExists_whenFindAuthorsByBookId_thenReturnsAuthors() throws Exception {
    List<AuthorResponse> authors = List.of(AuthorResponse.builder().id(1L).build());

    when(bookService.findAuthorsByBookId(anyLong())).thenReturn(authors);

    mockMvc
        .perform(get("/books/{bookId}/authors", 1L).accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.length()").value(1))
        .andExpect(jsonPath("$[0].id").value(1));
  }
}
