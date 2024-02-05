package com.carlosarroyoam.rest.books.entities;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
public class User {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(length = 64, nullable = false)
	private String firstName;

	@Column(length = 64, nullable = false)
	private String lastName;

	@Column(length = 128, nullable = false)
	private String email;

	@Column(length = 128, nullable = false)
	private String password;

	@OneToOne(cascade = CascadeType.MERGE)
	@JoinColumn(name = "role_id", referencedColumnName = "id")
	private Role role;

	public User(String firstName, String lastName, String email, String password, Role role) {
		this.firstName = firstName;
		this.lastName = lastName;
		this.email = email;
		this.password = password;
		this.role = role;
	}

}
