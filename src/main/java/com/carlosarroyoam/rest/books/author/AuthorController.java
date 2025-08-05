package com.carlosarroyoam.rest.books.author;

import com.carlosarroyoam.rest.books.author.dto.AuthorDto;
import com.carlosarroyoam.rest.books.author.dto.CreateAuthorRequestDto;
import com.carlosarroyoam.rest.books.author.dto.UpdateAuthorRequestDto;
import com.carlosarroyoam.rest.books.book.dto.BookDto;
import com.carlosarroyoam.rest.books.core.config.OpenApiConfig;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

@RestController
@RequestMapping("/authors")
@Tag(name = "Authors", description = "Operations about authors")
@SecurityRequirement(name = OpenApiConfig.SECURITY_SCHEME_NAME)
public class AuthorController {
  private final AuthorService authorService;

  public AuthorController(final AuthorService authorService) {
    this.authorService = authorService;
  }

  @GetMapping(produces = "application/json")
  @Operation(summary = "Gets the list of authors")
  public ResponseEntity<List<AuthorDto>> findAll(
      @RequestParam(required = false, defaultValue = "0") Integer page,
      @RequestParam(required = false, defaultValue = "25") Integer size) {
    List<AuthorDto> authors = authorService.findAll(page, size);
    return ResponseEntity.ok(authors);
  }

  @GetMapping(path = "/{authorId}", produces = "application/json")
  @Operation(summary = "Gets an author by its id")
  public ResponseEntity<AuthorDto> findById(@PathVariable Long authorId) {
    AuthorDto authorById = authorService.findById(authorId);
    return ResponseEntity.ok(authorById);
  }

  @PostMapping(consumes = "application/json")
  @Operation(summary = "Creates a new author")
  public ResponseEntity<Void> create(@Valid @RequestBody CreateAuthorRequestDto requestDto,
      UriComponentsBuilder builder) {
    AuthorDto createdAuthor = authorService.create(requestDto);
    UriComponents uriComponents = builder.path("/authors/{authorId}")
        .buildAndExpand(createdAuthor.getId());
    return ResponseEntity.created(uriComponents.toUri()).build();
  }

  @PutMapping(value = "/{authorId}", consumes = "application/json")
  @Operation(summary = "Updates a author by its id")
  public ResponseEntity<Void> update(@PathVariable Long authorId,
      @Valid @RequestBody UpdateAuthorRequestDto requestDto) {
    authorService.update(authorId, requestDto);
    return ResponseEntity.noContent().build();
  }

  @DeleteMapping("/{authorId}")
  @Operation(summary = "Deletes a author by its id")
  public ResponseEntity<Void> deleteById(@PathVariable Long authorId) {
    authorService.deleteById(authorId);
    return ResponseEntity.noContent().build();
  }

  @GetMapping(path = "/{authorId}/books", produces = "application/json")
  @Operation(summary = "Gets the list of books by authorId")
  public ResponseEntity<List<BookDto>> findBookAuthors(@PathVariable Long authorId) {
    List<BookDto> books = authorService.findBooksByAuthorId(authorId);
    return ResponseEntity.ok(books);
  }
}
