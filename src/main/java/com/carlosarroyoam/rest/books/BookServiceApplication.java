package com.carlosarroyoam.rest.books;

import java.time.LocalDate;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.carlosarroyoam.rest.books.entities.Book;
import com.carlosarroyoam.rest.books.entities.Role;
import com.carlosarroyoam.rest.books.entities.User;
import com.carlosarroyoam.rest.books.repositories.BookRepository;
import com.carlosarroyoam.rest.books.repositories.RoleRepository;
import com.carlosarroyoam.rest.books.repositories.UserRepository;

@SpringBootApplication
public class BookServiceApplication implements CommandLineRunner {

	private final BookRepository bookRepository;
	private final RoleRepository roleRepository;
	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;

	public BookServiceApplication(BookRepository bookRepository, RoleRepository roleRepository,
			UserRepository userRepository, PasswordEncoder passwordEncoder) {
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
		Book book1 = new Book("Homo Deus", "Yuval Noah", 12.99d, LocalDate.of(2018, 12, 1), true);
		Book book2 = new Book("Homo Sapiens", "Yuval Noah", 17.99d, LocalDate.of(2013, 12, 1), true);

		bookRepository.save(book1);
		bookRepository.save(book2);

		Role role1 = new Role("App//Admin", "Role for admins users");
		Role role2 = new Role("App//Customer", "Role for customer users");

		roleRepository.save(role1);
		roleRepository.save(role2);

		User user1 = new User("Carlos Alberto", "Arroyo Martínez", "carlosarroyoam@gmail.com",
				passwordEncoder.encode("secret"), role1);
		User user2 = new User("Cathy Stefania", "Guido Rojas", "fanipato1995@gmail.com",
				passwordEncoder.encode("secret"), role2);

		userRepository.save(user1);
		userRepository.save(user2);
	}

}
