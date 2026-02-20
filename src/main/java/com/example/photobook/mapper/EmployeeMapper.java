package com.example.photobook.mapper;

import com.example.photobook.dto.EmployeeDto;
import com.example.photobook.entity.Employee;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface EmployeeMapper extends EntityMapper<EmployeeDto, Employee> {

}