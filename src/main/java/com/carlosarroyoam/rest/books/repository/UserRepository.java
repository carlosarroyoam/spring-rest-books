package com.carlosarroyoam.rest.books.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.carlosarroyoam.rest.books.entity.User;

public interface UserRepository extends JpaRepository<User, Long> {

	Optional<User> findByEmail(String email);

}
