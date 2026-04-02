package com.carlosarroyoam.rest.books.book;

import com.carlosarroyoam.rest.books.author.dto.AuthorDto;
import com.carlosarroyoam.rest.books.book.dto.BookDto;
import com.carlosarroyoam.rest.books.book.dto.BookSpecsDto;
import com.carlosarroyoam.rest.books.book.dto.CreateBookRequestDto;
import com.carlosarroyoam.rest.books.book.dto.UpdateBookRequestDto;
import com.carlosarroyoam.rest.books.core.dto.PagedResponseDto;
import com.carlosarroyoam.rest.books.core.dto.PaginationDto;
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

@ExtendWith(MockitoExtension.class)
class BookControllerTest {
  private ObjectMapper mapper;
  private MockMvc mockMvc;

  @Mock
  private BookService bookService;

  @InjectMocks
  private BookController bookController;

  @BeforeEach
  void setup() {
    mapper = new ObjectMapper();
    mapper.findAndRegisterModules();

    mockMvc = MockMvcBuilders.standaloneSetup(bookController)
        .setControllerAdvice(GlobalExceptionHandler.class)
        .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
        .build();
  }

  @Test
  @DisplayName("Should return PagedResponseDto<BookDto> when find all books")
  void shouldReturnPagedBooksWhenFindAllBooks() throws Exception {
    PagedResponseDto<BookDto> pagedResponse = PagedResponseDto.<BookDto>builder()
        .items(List.of(BookDto.builder().id(1L).build()))
        .pagination(PaginationDto.builder().page(0).size(25).totalItems(1).totalPages(1).build())
        .build();

    when(bookService.findAll(any(Pageable.class), any(BookSpecsDto.class)))
        .thenReturn(pagedResponse);

    mockMvc
        .perform(get("/books").queryParam("page", "0")
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
  @DisplayName("Should return BookDto when find book by id")
  void shouldReturnBookDtoWhenFindBookById() throws Exception {
    BookDto book = BookDto.builder().id(1L).title("Sapiens").build();

    when(bookService.findById(anyLong())).thenReturn(book);

    mockMvc.perform(get("/books/{bookId}", 1L).accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.id").value(1))
        .andExpect(jsonPath("$.title").value("Sapiens"));
  }

  @Test
  @DisplayName("Should return created when create a book")
  void shouldReturnCreatedWhenCreateBook() throws Exception {
    CreateBookRequestDto requestDto = CreateBookRequestDto.builder()
        .isbn("978-9-7389-4434-3")
        .title("Sapiens: A Brief History of Humankind")
        .coverUrl("https://images.isbndb.com/covers/60/97/9780062316097.jpg")
        .price(new BigDecimal("20.99"))
        .publishedAt(LocalDate.parse("2021-12-01"))
        .isAvailableOnline(Boolean.TRUE)
        .build();

    BookDto book = BookDto.builder().id(1L).build();

    when(bookService.create(any(CreateBookRequestDto.class))).thenReturn(book);

    mockMvc
        .perform(post("/books").content(mapper.writeValueAsString(requestDto))
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isCreated())
        .andExpect(header().string("location", "http://localhost/books/1"));
  }

  @Test
  @DisplayName("Should return no content when update book")
  void shouldReturnNoContentWhenUpdateBook() throws Exception {
    UpdateBookRequestDto requestDto = UpdateBookRequestDto.builder()
        .isbn("978-9-7389-4434-3")
        .title("Sapiens: A Brief History of Humankind")
        .coverUrl("https://images.isbndb.com/covers/60/97/9780062316097.jpg")
        .price(new BigDecimal("20.99"))
        .publishedAt(LocalDate.parse("2021-12-01"))
        .isAvailableOnline(Boolean.TRUE)
        .build();

    mockMvc.perform(put("/books/{bookId}", 1L).content(mapper.writeValueAsString(requestDto))
        .contentType(MediaType.APPLICATION_JSON)).andExpect(status().isNoContent());
  }

  @Test
  @DisplayName("Should return no content when delete book")
  void shouldReturnNoContentWhenDeleteBook() throws Exception {
    mockMvc.perform(delete("/books/{bookId}", 1L).accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isNoContent());
  }

  @Test
  @DisplayName("Should return authors when find authors by book id")
  void shouldReturnListOfAuthorsWhenFindAuthorsByBookId() throws Exception {
    List<AuthorDto> authors = List.of(AuthorDto.builder().id(1L).build());

    when(bookService.findAuthorsByBookId(anyLong())).thenReturn(authors);

    mockMvc.perform(get("/books/{bookId}/authors", 1L).accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.length()").value(1))
        .andExpect(jsonPath("$[0].id").value(1));
  }
}