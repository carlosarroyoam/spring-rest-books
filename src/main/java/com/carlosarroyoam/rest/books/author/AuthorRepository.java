package com.carlosarroyoam.rest.books.author;

import com.carlosarroyoam.rest.books.author.entity.Author;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuthorRepository extends JpaRepository<Author, Long> {
}
