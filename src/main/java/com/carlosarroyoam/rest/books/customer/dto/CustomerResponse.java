package com.carlosarroyoam.rest.books.customer.dto;

import com.carlosarroyoam.rest.books.customer.entity.Customer;
import com.carlosarroyoam.rest.books.customer.entity.CustomerStatus;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

@Getter
@Setter
@Builder
public class CustomerResponse {
  private Long id;
  private String firstName;
  private String lastName;
  private String email;
  private String username;
  private CustomerStatus status;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
  private LocalDateTime deletedAt;

  @Mapper(
      nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
      unmappedTargetPolicy = ReportingPolicy.IGNORE)
  public interface CustomerResponseMapper {
    CustomerResponseMapper INSTANCE = Mappers.getMapper(CustomerResponseMapper.class);

    CustomerResponse toDto(Customer entity);

    List<CustomerResponse> toDtos(List<Customer> entities);
  }
}
