package com.example.photobook.mapper;

import com.example.photobook.dto.CustomerDto;
import com.example.photobook.entity.Customer;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CustomerMapper extends EntityMapper<CustomerDto, Customer>{

}