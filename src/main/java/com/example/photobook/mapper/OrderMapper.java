package com.example.photobook.mapper;

import com.example.photobook.dto.OrderDto;
import com.example.photobook.entity.Customer;
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
    @Mapping(target = "employees", ignore = true)
    @Mapping(target = "upload", ignore = true)
    @Mapping(target = "statusHistory", ignore = true)
    Order toEntity(OrderDto dto);

    @Override
    @Mapping(target = "categoryId", source = "category.id")
    @Mapping(target = "categoryName", source = "category.name")
    @Mapping(target = "customerId", source = "customer.id")
    @Mapping(target = "customerName", source = "customer.fullName")
    @Mapping(target = "employees", ignore = true)
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
}
