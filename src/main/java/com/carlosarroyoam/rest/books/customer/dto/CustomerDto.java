package com.carlosarroyoam.rest.books.customer.dto;

import com.carlosarroyoam.rest.books.customer.entity.Customer;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerDto {
  private Long id;
  private String firstName;
  private String lastName;
  private String email;
  private String username;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;

  @Mapper(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE, unmappedTargetPolicy = ReportingPolicy.IGNORE)
  public interface CustomerDtoMapper {
    CustomerDtoMapper INSTANCE = Mappers.getMapper(CustomerDtoMapper.class);

    CustomerDto toDto(Customer entity);

    List<CustomerDto> toDtos(List<Customer> entities);

    Customer createRequestToEntity(CreateCustomerRequestDto requestDto);
  }
}
