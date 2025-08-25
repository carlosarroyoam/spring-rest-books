package com.carlosarroyoam.rest.books.book;

import com.carlosarroyoam.rest.books.author.dto.AuthorDto;
import com.carlosarroyoam.rest.books.book.dto.BookDto;
import com.carlosarroyoam.rest.books.book.dto.BookFilterDto;
import com.carlosarroyoam.rest.books.book.dto.CreateBookRequestDto;
import com.carlosarroyoam.rest.books.book.dto.UpdateBookRequestDto;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

@RestController
@RequestMapping("/books")
public class BookController {
  private final BookService bookService;

  public BookController(final BookService bookService) {
    this.bookService = bookService;
  }

  @GetMapping(produces = "application/json")
  public ResponseEntity<List<BookDto>> findAll(
      @PageableDefault(page = 0, size = 25) Pageable pageable,
      @Valid @ModelAttribute BookFilterDto filters) {
    List<BookDto> books = bookService.findAll(pageable, filters);
    return ResponseEntity.ok(books);
  }

  @GetMapping(value = "/{bookId}", produces = "application/json")
  public ResponseEntity<BookDto> findById(@PathVariable Long bookId) {
    BookDto bookById = bookService.findById(bookId);
    return ResponseEntity.ok(bookById);
  }

  @PostMapping(consumes = "application/json")
  @PreAuthorize("hasRole('App/Admin')")
  public ResponseEntity<Void> create(@Valid @RequestBody CreateBookRequestDto requestDto,
      UriComponentsBuilder builder) {
    BookDto createdBook = bookService.create(requestDto);
    UriComponents uriComponents = builder.path("/books/{bookId}")
        .buildAndExpand(createdBook.getId());
    return ResponseEntity.created(uriComponents.toUri()).build();
  }

  @PutMapping(value = "/{bookId}", consumes = "application/json")
  @PreAuthorize("hasRole('App/Admin')")
  public ResponseEntity<Void> update(@PathVariable Long bookId,
      @Valid @RequestBody UpdateBookRequestDto requestDto) {
    bookService.update(bookId, requestDto);
    return ResponseEntity.noContent().build();
  }

  @DeleteMapping("/{bookId}")
  @PreAuthorize("hasRole('App/Admin')")
  public ResponseEntity<Void> deleteById(@PathVariable Long bookId) {
    bookService.deleteById(bookId);
    return ResponseEntity.noContent().build();
  }

  @GetMapping(path = "/{bookId}/authors", produces = "application/json")
  public ResponseEntity<List<AuthorDto>> findBookAuthors(@PathVariable Long bookId) {
    List<AuthorDto> authors = bookService.findAuthorsByBookId(bookId);
    return ResponseEntity.ok(authors);
  }
}
