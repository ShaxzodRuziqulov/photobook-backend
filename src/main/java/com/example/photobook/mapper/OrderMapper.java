package com.example.photobook.mapper;

import com.example.photobook.dto.OrderDto;
import com.example.photobook.entity.Customer;
import com.example.photobook.entity.Employee;
import com.example.photobook.entity.Order;
import com.example.photobook.entity.ProductCategory;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.UUID;

@Mapper(componentModel = "spring")
public interface OrderMapper extends EntityMapper<OrderDto, Order> {

    @Override
    @Mapping(target = "category", source = "categoryId")
    @Mapping(target = "customer", source = "customerId")
    @Mapping(target = "employee", source = "employeeId")
    Order toEntity(OrderDto dto);

    @Override
    @Mapping(target = "categoryId", source = "category.id")
    @Mapping(target = "customerId", source = "customer.id")
    @Mapping(target = "employeeId", source = "employee.id")
    OrderDto toDto(Order entity);

    default ProductCategory mapProductCategory(UUID id) {
        if (id == null) {
            return null;
        }
        ProductCategory category = new ProductCategory();
        category.setId(id);
        return category;
    }

    default UUID mapProductCategory(ProductCategory category) {
        return category == null ? null : category.getId();
    }

    default Customer mapCustomer(UUID id) {
        if (id == null) {
            return null;
        }
        Customer customer = new Customer();
        customer.setId(id);
        return customer;
    }

    default UUID mapCustomer(Customer customer) {
        return customer == null ? null : customer.getId();
    }

    default Employee mapEmployee(UUID id) {
        if (id == null) {
            return null;
        }
        Employee employee = new Employee();
        employee.setId(id);
        return employee;
    }

    default UUID mapEmployee(Employee employee) {
        return employee == null ? null : employee.getId();
    }
}
