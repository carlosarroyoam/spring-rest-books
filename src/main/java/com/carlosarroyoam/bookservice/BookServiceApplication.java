package com.carlosarroyoam.bookservice;

import java.time.LocalDate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.carlosarroyoam.bookservice.configurations.RsaKeyProperties;
import com.carlosarroyoam.bookservice.entities.Book;
import com.carlosarroyoam.bookservice.entities.Role;
import com.carlosarroyoam.bookservice.entities.User;
import com.carlosarroyoam.bookservice.repositories.BookRepository;
import com.carlosarroyoam.bookservice.repositories.RoleRepository;
import com.carlosarroyoam.bookservice.repositories.UserRepository;

@SpringBootApplication
@EnableConfigurationProperties(RsaKeyProperties.class)
public class BookServiceApplication implements CommandLineRunner {
    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public static void main(String[] args) {
        SpringApplication.run(BookServiceApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        Book book1 = new Book(null, "Homo Deus", "Yuval Noah", 12.99d, LocalDate.of(2018, 12, 1), true);
        Book book2 = new Book(null, "Homo Sapiens", "Yuval Noah", 17.99d, LocalDate.of(2013, 12, 1), true);

        bookRepository.save(book1);
        bookRepository.save(book2);

        Role role1 = new Role("App//Admin", "Role for admins users");
        Role role2 = new Role("App//Customer", "Role for customer users");

        roleRepository.save(role1);
        roleRepository.save(role2);

        User user1 = new User(null, "Carlos Alberto", "Arroyo Martínez", "carlosarroyoam@gmail.com",
                passwordEncoder.encode("secret"), role1);
        User user2 = new User(null, "Cathy Stefania", "Guido Rojas", "fanipato1995@gmail.com",
                passwordEncoder.encode("secret"), role2);

        userRepository.save(user1);
        userRepository.save(user2);
    }
}
