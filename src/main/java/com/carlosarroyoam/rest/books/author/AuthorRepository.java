package com.carlosarroyoam.rest.books.author;

import com.carlosarroyoam.rest.books.author.entity.Author;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface AuthorRepository extends JpaRepository<Author, Long>, JpaSpecificationExecutor<Author> {
}
