package com.carlosarroyoam.rest.books.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.carlosarroyoam.rest.books.constant.AppMessages;
import com.carlosarroyoam.rest.books.dto.AuthorResponse;
import com.carlosarroyoam.rest.books.entity.Author;
import com.carlosarroyoam.rest.books.mapper.AuthorMapper;
import com.carlosarroyoam.rest.books.repository.AuthorRepository;

@Service
public class AuthorService {

	private static final Logger log = LoggerFactory.getLogger(AuthorService.class);
	private final AuthorRepository authorRepository;
	private final AuthorMapper authorMapper;

	public AuthorService(AuthorRepository authorRepository, AuthorMapper authorMapper) {
		this.authorRepository = authorRepository;
		this.authorMapper = authorMapper;
	}

	public List<AuthorResponse> findAll() {
		List<Author> authors = authorRepository.findAll();
		return authorMapper.toDtos(authors);
	}

	public AuthorResponse findById(Long authorId) {
		Author authorById = authorRepository.findById(authorId).orElseThrow(() -> {
			log.warn(AppMessages.AUTHOR_NOT_FOUND_EXCEPTION);
			return new ResponseStatusException(HttpStatus.NOT_FOUND, AppMessages.AUTHOR_NOT_FOUND_EXCEPTION);
		});

		return authorMapper.toDto(authorById);
	}

}
