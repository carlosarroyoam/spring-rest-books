package com.carlosarroyoam.rest.books.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.carlosarroyoam.rest.books.entities.User;

public interface UserRepository extends JpaRepository<User, Long> {

	Optional<User> findByEmail(String email);

}
