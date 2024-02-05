package com.carlosarroyoam.rest.books.mappers;

import java.util.List;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

import com.carlosarroyoam.rest.books.dtos.CreateUserRequest;
import com.carlosarroyoam.rest.books.dtos.UpdateUserRequest;
import com.carlosarroyoam.rest.books.dtos.UserResponse;
import com.carlosarroyoam.rest.books.entities.User;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface UserMapper {

	UserResponse toDto(User user);

	List<UserResponse> toDtos(List<User> users);

	@BeanMapping(unmappedTargetPolicy = ReportingPolicy.IGNORE)
	User createRequestToEntity(CreateUserRequest createUserRequest);

	@BeanMapping(unmappedTargetPolicy = ReportingPolicy.IGNORE)
	User updateRequestToEntity(UpdateUserRequest updateUserRequest);

}
