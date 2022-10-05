package com.example.demospringrest;

import java.time.LocalDate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.example.demospringrest.entities.Book;
import com.example.demospringrest.repositories.BookRepository;

@SpringBootApplication
public class DemoRestBookApplication implements CommandLineRunner {
	@Autowired
	private BookRepository bookRepository;

	public static void main(String[] args) {
		SpringApplication.run(DemoRestBookApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		Book book1 = new Book(null, "Homo Deus", "Yuval Noah", 12.99d, LocalDate.of(2018, 12, 1), true);
		Book book2 = new Book(null, "Homo Sapiens", "Yuval Noah", 17.99d, LocalDate.of(2013, 12, 1), true);

		bookRepository.save(book1);
		bookRepository.save(book2);
	}

	@Bean
	public OpenAPI customOpenAPI(@Value("${springdoc.version}") String appVersion) {
		String appTitle = "Books service";
		String appDescription = "Book service demo API";
		Contact contactInfo = new Contact().email("carlosarroyoam@gmail.com");
		License license = new License().name("Apache 2.0").url("https://www.apache.org/licenses/LICENSE-2.0.html");

		return new OpenAPI().info(new Info().title(appTitle).version(appVersion).description(appDescription)
				.contact(contactInfo).license(license));
	}
}
