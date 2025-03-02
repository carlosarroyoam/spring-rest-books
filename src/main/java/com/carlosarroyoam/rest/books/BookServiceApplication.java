package com.carlosarroyoam.rest.books;

import com.carlosarroyoam.rest.books.entity.Author;
import com.carlosarroyoam.rest.books.entity.Book;
import com.carlosarroyoam.rest.books.entity.Role;
import com.carlosarroyoam.rest.books.entity.User;
import com.carlosarroyoam.rest.books.repository.AuthorRepository;
import com.carlosarroyoam.rest.books.repository.BookRepository;
import com.carlosarroyoam.rest.books.repository.RoleRepository;
import com.carlosarroyoam.rest.books.repository.UserRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class BookServiceApplication implements CommandLineRunner {
  private final AuthorRepository authorRepository;
  private final BookRepository bookRepository;
  private final RoleRepository roleRepository;
  private final UserRepository userRepository;

  public BookServiceApplication(AuthorRepository authorRepository, BookRepository bookRepository,
      RoleRepository roleRepository, UserRepository userRepository) {
    this.authorRepository = authorRepository;
    this.bookRepository = bookRepository;
    this.roleRepository = roleRepository;
    this.userRepository = userRepository;
  }

  public static void main(String[] args) {
    SpringApplication.run(BookServiceApplication.class, args);
  }

  @Override
  public void run(String... args) throws Exception {
    Author author1 = Author.builder()
        .name("Yuval Noah Harari")
        .createdAt(LocalDateTime.now())
        .updatedAt(LocalDateTime.now())
        .build();

    Author author2 = Author.builder()
        .name("Itzik Yahav")
        .createdAt(LocalDateTime.now())
        .updatedAt(LocalDateTime.now())
        .build();

    authorRepository.save(author1);
    authorRepository.save(author2);

    Book book1 = Book.builder()
        .isbn("978-1-3035-0529-4")
        .title("Homo Deus: A Brief History of Tomorrow")
        .coverUrl("https://images.isbndb.com/covers/39/36/9781784703936.jpg")
        .price(new BigDecimal("22.99"))
        .isAvailableOnline(false)
        .publishedAt(LocalDate.parse("2017-01-01"))
        .createdAt(LocalDateTime.now())
        .updatedAt(LocalDateTime.now())
        .build();
    book1.addAuthor(author1);
    book1.addAuthor(author2);

    Book book2 = Book.builder()
        .isbn("978-9-7389-4434-3")
        .title("Sapiens: A Brief History of Humankind")
        .coverUrl("https://images.isbndb.com/covers/60/97/9780062316097.jpg")
        .price(new BigDecimal("20.79"))
        .isAvailableOnline(false)
        .publishedAt(LocalDate.parse("2022-12-01"))
        .createdAt(LocalDateTime.now())
        .updatedAt(LocalDateTime.now())
        .build();
    book2.addAuthor(author1);

    bookRepository.save(book1);
    bookRepository.save(book2);

    Role adminRole = Role.builder()
        .title("App//Admin")
        .description("Role for admins users")
        .build();

    Role customerRole = Role.builder()
        .title("App//Customer")
        .description("Role for customer users")
        .build();

    roleRepository.save(adminRole);
    roleRepository.save(customerRole);

    User user1 = User.builder()
        .name("Carlos Alberto Arroyo Martínez")
        .age(Byte.valueOf("28"))
        .email("carroyom@mail.com")
        .username("carroyom")
        .roleId(adminRole.getId())
        .isActive(Boolean.TRUE)
        .createdAt(LocalDateTime.now())
        .updatedAt(LocalDateTime.now())
        .build();

    User user2 = User.builder()
        .name("Cathy Stefania Guido Rojas")
        .age(Byte.valueOf("28"))
        .email("cguidor@mail.com")
        .username("cguidor")
        .roleId(customerRole.getId())
        .isActive(Boolean.TRUE)
        .createdAt(LocalDateTime.now())
        .updatedAt(LocalDateTime.now())
        .build();

    userRepository.save(user1);
    userRepository.save(user2);
  }
}
