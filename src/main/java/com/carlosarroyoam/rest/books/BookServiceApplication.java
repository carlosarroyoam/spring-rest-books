package com.carlosarroyoam.rest.books;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.carlosarroyoam.rest.books.config.security.RsaKeysProperties;
import com.carlosarroyoam.rest.books.entity.Author;
import com.carlosarroyoam.rest.books.entity.Book;
import com.carlosarroyoam.rest.books.entity.Role;
import com.carlosarroyoam.rest.books.entity.User;
import com.carlosarroyoam.rest.books.repository.AuthorRepository;
import com.carlosarroyoam.rest.books.repository.BookRepository;
import com.carlosarroyoam.rest.books.repository.RoleRepository;
import com.carlosarroyoam.rest.books.repository.UserRepository;

@SpringBootApplication
@EnableConfigurationProperties(RsaKeysProperties.class)
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
		Author author1 = new Author("Yuval Noah Harari", LocalDateTime.now(), LocalDateTime.now());
		Author author2 = new Author("Itzik Yahav", LocalDateTime.now(), LocalDateTime.now());

		authorRepository.save(author1);
		authorRepository.save(author2);

		Book book1 = new Book("978-1-3035-0529-4", "Homo Deus", BigDecimal.valueOf(12.99d), false,
				LocalDate.of(2017, 1, 1), LocalDateTime.now(), LocalDateTime.now());
		book1.addAuthor(author1);
		book1.addAuthor(author2);

		Book book2 = new Book("978-9-7389-4434-3", "Sapiens", BigDecimal.valueOf(12.99d), true,
				LocalDate.of(2022, 12, 1), LocalDateTime.now(), LocalDateTime.now());
		book2.addAuthor(author1);

		bookRepository.save(book1);
		bookRepository.save(book2);

		Role adminRole = new Role("App//Admin", "Role for admins users");
		Role customerRole = new Role("App//Customer", "Role for customer users");

		roleRepository.save(adminRole);
		roleRepository.save(customerRole);

		String encodedPassword = passwordEncoder.encode("secret");
		User user1 = new User("Carlos Alberto Arroyo Martínez", "carroyom@mail.com", "carroyom", encodedPassword,
				adminRole.getId(), LocalDateTime.now(), LocalDateTime.now());
		user1.setAge(Byte.valueOf("28"));
		user1.setIsActive(Boolean.TRUE);

		User user2 = new User("Cathy Stefania Guido Rojas", "cguidor@mail.com", "cguidor", encodedPassword,
				customerRole.getId(), LocalDateTime.now(), LocalDateTime.now());
		user2.setAge(Byte.valueOf("28"));
		user2.setIsActive(Boolean.TRUE);

		userRepository.save(user1);
		userRepository.save(user2);
	}

}
