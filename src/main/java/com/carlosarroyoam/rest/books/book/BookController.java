package com.carlosarroyoam.rest.books.book;

import com.carlosarroyoam.rest.books.author.dto.AuthorResponse;
import com.carlosarroyoam.rest.books.book.dto.BookResponse;
import com.carlosarroyoam.rest.books.book.dto.BookSpecs;
import com.carlosarroyoam.rest.books.book.dto.CreateBookRequest;
import com.carlosarroyoam.rest.books.book.dto.UpdateBookRequest;
import com.carlosarroyoam.rest.books.core.dto.PagedResponse;
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

  public BookController(BookService bookService) {
    this.bookService = bookService;
  }

  @GetMapping(produces = "application/json")
  public ResponseEntity<PagedResponse<BookResponse>> findAll(
      @Valid @ModelAttribute BookSpecs bookSpecs,
      @PageableDefault(page = 0, size = 25, sort = "id") Pageable pageable) {
    PagedResponse<BookResponse> books = bookService.findAll(bookSpecs, pageable);
    return ResponseEntity.ok(books);
  }

  @GetMapping(value = "/{bookId}", produces = "application/json")
  public ResponseEntity<BookResponse> findById(@PathVariable Long bookId) {
    BookResponse bookById = bookService.findById(bookId);
    return ResponseEntity.ok(bookById);
  }

  @PostMapping(consumes = "application/json")
  @PreAuthorize("hasRole('App/Admin')")
  public ResponseEntity<Void> create(@Valid @RequestBody CreateBookRequest request,
      UriComponentsBuilder builder) {
    BookResponse createdBook = bookService.create(request);
    UriComponents uriComponents = builder.path("/books/{bookId}")
        .buildAndExpand(createdBook.getId());
    return ResponseEntity.created(uriComponents.toUri()).build();
  }

  @PutMapping(value = "/{bookId}", consumes = "application/json")
  @PreAuthorize("hasRole('App/Admin')")
  public ResponseEntity<Void> update(@PathVariable Long bookId,
      @Valid @RequestBody UpdateBookRequest request) {
    bookService.update(bookId, request);
    return ResponseEntity.noContent().build();
  }

  @DeleteMapping("/{bookId}")
  @PreAuthorize("hasRole('App/Admin')")
  public ResponseEntity<Void> deleteById(@PathVariable Long bookId) {
    bookService.deleteById(bookId);
    return ResponseEntity.noContent().build();
  }

  @GetMapping(path = "/{bookId}/authors", produces = "application/json")
  public ResponseEntity<List<AuthorResponse>> findBookAuthors(@PathVariable Long bookId) {
    List<AuthorResponse> authors = bookService.findAuthorsByBookId(bookId);
    return ResponseEntity.ok(authors);
  }
}
