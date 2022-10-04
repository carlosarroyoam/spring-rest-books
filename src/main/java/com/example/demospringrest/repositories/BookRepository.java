package com.example.demospringrest.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.demospringrest.entities.Book;

@Repository
public interface BookRepository extends JpaRepository<Book, Long> {
}
