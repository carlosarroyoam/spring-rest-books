package com.carlosarroyoam.rest.books;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.carlosarroyoam.rest.books.entities.Author;
import com.carlosarroyoam.rest.books.entities.Book;
import com.carlosarroyoam.rest.books.entities.Role;
import com.carlosarroyoam.rest.books.entities.User;
import com.carlosarroyoam.rest.books.repositories.AuthorRepository;
import com.carlosarroyoam.rest.books.repositories.BookRepository;
import com.carlosarroyoam.rest.books.repositories.RoleRepository;
import com.carlosarroyoam.rest.books.repositories.UserRepository;

@SpringBootApplication
public class BookServiceApplication implements CommandLineRunner {

	private final AuthorRepository authorRepository;
	private final BookRepository bookRepository;
	private final RoleRepository roleRepository;
	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;

	public BookServiceApplication(AuthorRepository authorRepository, BookRepository bookRepository,
			RoleRepository roleRepository, UserRepository userRepository, PasswordEncoder passwordEncoder) {
		this.authorRepository = authorRepository;
		this.bookRepository = bookRepository;
		this.roleRepository = roleRepository;
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
	}

	public static void main(String[] args) {
		SpringApplication.run(BookServiceApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		Author author1 = new Author("Yuval", "Noah", LocalDateTime.now(), LocalDateTime.now());
		Author author2 = new Author("Yuval", "Noah2", LocalDateTime.now(), LocalDateTime.now());

		authorRepository.save(author1);
		authorRepository.save(author2);

		Book book1 = new Book("Homo Deus", BigDecimal.valueOf(12.99d), true, LocalDate.of(2018, 12, 1),
				LocalDateTime.now(), LocalDateTime.now());
		book1.addAuthor(author1);
		book1.addAuthor(author2);

		Book book2 = new Book("Homo Sapiens", BigDecimal.valueOf(12.99d), true, LocalDate.of(2018, 12, 1),
				LocalDateTime.now(), LocalDateTime.now());
		book2.addAuthor(author1);

		bookRepository.save(book1);
		bookRepository.save(book2);

		Role role1 = new Role("App//Admin", "Role for admins users");
		Role role2 = new Role("App//Customer", "Role for customer users");

		roleRepository.save(role1);
		roleRepository.save(role2);

		User user1 = new User("Carlos Alberto", "Arroyo Martínez", "carlosarroyoam@gmail.com",
				passwordEncoder.encode("secret"), role1, LocalDateTime.now(), LocalDateTime.now());
		User user2 = new User("Cathy Stefania", "Guido Rojas", "fanipato1995@gmail.com",
				passwordEncoder.encode("secret"), role1, LocalDateTime.now(), LocalDateTime.now());

		userRepository.save(user1);
		userRepository.save(user2);
	}

}
