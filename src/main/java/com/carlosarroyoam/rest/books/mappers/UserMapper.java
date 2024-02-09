package com.carlosarroyoam.rest.books.mappers;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

import com.carlosarroyoam.rest.books.dtos.CreateUserRequest;
import com.carlosarroyoam.rest.books.dtos.UpdateUserRequest;
import com.carlosarroyoam.rest.books.dtos.UserResponse;
import com.carlosarroyoam.rest.books.entities.User;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserMapper {

	UserResponse toDto(User user);

	List<UserResponse> toDtos(List<User> users);

	User createRequestToEntity(CreateUserRequest createUserRequest);

	User updateRequestToEntity(UpdateUserRequest updateUserRequest);

}
