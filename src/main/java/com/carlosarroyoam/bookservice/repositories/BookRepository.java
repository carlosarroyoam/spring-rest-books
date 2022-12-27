package com.carlosarroyoam.bookservice.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.carlosarroyoam.bookservice.entities.Book;

public interface BookRepository extends JpaRepository<Book, Long> {
}
