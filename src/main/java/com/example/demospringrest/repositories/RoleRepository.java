package com.example.demospringrest.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.demospringrest.entities.Role;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
}
