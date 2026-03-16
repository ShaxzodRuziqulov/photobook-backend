package com.example.photobook.mapper;

import com.example.photobook.dto.EmployeeDto;
import com.example.photobook.dto.OrderDto;
import com.example.photobook.entity.Customer;
import com.example.photobook.entity.Order;
import com.example.photobook.entity.ProductCategory;
import com.example.photobook.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Mapper(componentModel = "spring")
public interface OrderMapper extends EntityMapper<OrderDto, Order> {

    @Override
    @Mapping(target = "category", source = "categoryId")
    @Mapping(target = "customer", source = "customerId")
    @Mapping(target = "employees", ignore = true)
    @Mapping(target = "upload", ignore = true)
    @Mapping(target = "statusHistory", ignore = true)
    Order toEntity(OrderDto dto);

    @Override
    @Mapping(target = "categoryId", source = "category.id")
    @Mapping(target = "categoryName", source = "category.name")
    @Mapping(target = "customerId", source = "customer.id")
    @Mapping(target = "customerName", source = "customer.fullName")
    @Mapping(target = "employees", expression = "java(mapEmployees(entity.getEmployees()))")
    @Mapping(target = "uploadId", source = "upload.id")
    @Mapping(target = "statusHistory", ignore = true)
    OrderDto toDto(Order entity);

    default ProductCategory mapProductCategory(UUID id) {
        if (id == null) {
            return null;
        }
        ProductCategory category = new ProductCategory();
        category.setId(id);
        return category;
    }

    default Customer mapCustomer(UUID id) {
        if (id == null) {
            return null;
        }
        Customer customer = new Customer();
        customer.setId(id);
        return customer;
    }

    default List<EmployeeDto> mapEmployees(java.util.Set<User> employees) {
        if (employees == null || employees.isEmpty()) {
            return List.of();
        }
        return employees.stream()
                .sorted(Comparator.comparing(User::getId))
                .map(this::toEmployeeDto)
                .toList();
    }

    default EmployeeDto toEmployeeDto(User employee) {
        if (employee == null) {
            return null;
        }
        EmployeeDto dto = new EmployeeDto();
        dto.setEmployeeIds(employee.getId());
        dto.setEmployeeNames(buildEmployeeName(employee));
        return dto;
    }

    default String buildEmployeeName(User employee) {
        if (employee == null) {
            return null;
        }
        StringBuilder result = new StringBuilder();
        appendPart(result, employee.getLastName());
        appendPart(result, employee.getFirstName());
        String fullName = result.toString().trim();
        return fullName.isEmpty() ? employee.getUsername() : fullName;
    }

    private static void appendPart(StringBuilder result, String part) {
        if (part == null || part.isBlank()) {
            return;
        }
        if (!result.isEmpty()) {
            result.append(' ');
        }
        result.append(part.trim());
    }
}
