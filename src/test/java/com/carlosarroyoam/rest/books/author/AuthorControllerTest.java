package com.carlosarroyoam.rest.books.author;

import com.carlosarroyoam.rest.books.author.dto.AuthorDto;
import com.carlosarroyoam.rest.books.author.dto.AuthorFilterDto;
import com.carlosarroyoam.rest.books.author.dto.CreateAuthorRequestDto;
import com.carlosarroyoam.rest.books.author.dto.UpdateAuthorRequestDto;
import com.carlosarroyoam.rest.books.book.dto.BookDto;
import com.carlosarroyoam.rest.books.core.dto.PagedResponseDto;
import com.carlosarroyoam.rest.books.core.dto.PaginationDto;
import com.carlosarroyoam.rest.books.core.exception.GlobalExceptionHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
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
class AuthorControllerTest {
  private ObjectMapper mapper;
  private MockMvc mockMvc;

  @Mock
  private AuthorService authorService;

  @InjectMocks
  private AuthorController authorController;

  @BeforeEach
  void setup() {
    mapper = new ObjectMapper();
    mapper.findAndRegisterModules();

    mockMvc = MockMvcBuilders.standaloneSetup(authorController)
        .setControllerAdvice(GlobalExceptionHandler.class)
        .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
        .build();
  }

  @Test
  @DisplayName("Should return PagedResponseDto<AuthorDto> when find all authors")
  void shouldReturnPagedAuthorsWhenFindAllAuthors() throws Exception {
    PagedResponseDto<AuthorDto> pagedResponse = PagedResponseDto.<AuthorDto>builder()
        .items(List.of(AuthorDto.builder().build()))
        .pagination(PaginationDto.builder().page(0).size(25).totalItems(1).totalPages(1).build())
        .build();

    when(authorService.findAll(any(Pageable.class), any(AuthorFilterDto.class)))
        .thenReturn(pagedResponse);

    mockMvc
        .perform(get("/authors").queryParam("page", "0")
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
  @DisplayName("Should return AuthorDto when find author by id with existing id")
  void shouldReturnAuthorDtoWhenFindAuthorById() throws Exception {
    AuthorDto author = AuthorDto.builder().id(1L).name("Yuval Noah Harari").build();

    when(authorService.findById(anyLong())).thenReturn(author);

    mockMvc.perform(get("/authors/{authorId}", 1L).accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.id").value(1));
  }

  @Test
  @DisplayName("Should return created when create an author")
  void shouldReturnCreatedWhenCreateAuthor() throws Exception {
    CreateAuthorRequestDto requestDto = CreateAuthorRequestDto.builder()
        .name("Yuval Noah Harari")
        .build();

    AuthorDto author = AuthorDto.builder().id(1L).build();

    when(authorService.create(any(CreateAuthorRequestDto.class))).thenReturn(author);

    mockMvc
        .perform(post("/authors").content(mapper.writeValueAsString(requestDto))
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isCreated())
        .andExpect(header().string("location", "http://localhost/authors/1"));
  }

  @Test
  @DisplayName("Should return no content when update author")
  void shouldReturnNoContentWhenUpdateAuthor() throws Exception {
    UpdateAuthorRequestDto requestDto = UpdateAuthorRequestDto.builder()
        .name("Yuval Noah Harari")
        .build();

    mockMvc.perform(put("/authors/{authorId}", 1L).content(mapper.writeValueAsString(requestDto))
        .contentType(MediaType.APPLICATION_JSON)).andExpect(status().isNoContent());
  }

  @Test
  @DisplayName("Should return no content when delete author")
  void shouldReturnNoContentWhenDeleteAuthor() throws Exception {
    mockMvc.perform(delete("/authors/{authorId}", 1L).accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isNoContent());
  }

  @Test
  @DisplayName("Should return books when find books by author id")
  void shouldReturnListOfBooksWhenFindBooksByAuthorId() throws Exception {
    List<BookDto> books = List.of(BookDto.builder().id(1L).build());

    when(authorService.findBooksByAuthorId(anyLong())).thenReturn(books);

    mockMvc.perform(get("/authors/{authorId}/books", 1L).accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.length()").value(1))
        .andExpect(jsonPath("$[0].id").value(1));
  }
}