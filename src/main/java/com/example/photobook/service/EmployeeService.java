package com.example.photobook.service;

import com.example.photobook.dto.EmployeeDto;
import com.example.photobook.entity.Employee;
import com.example.photobook.mapper.EmployeeMapper;
import com.example.photobook.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EmployeeService {
    private final EmployeeRepository repository;
    private final EmployeeMapper mapper;

    public EmployeeDto create(EmployeeDto dto) {
        Employee employee = mapper.toEntity(dto);
        return mapper.toDto(repository.save(employee));
    }

    public EmployeeDto update(UUID id, EmployeeDto dto) {
        Employee employee = findByUserId(id);
        employee.setId(dto.getId());
        employee.setFullName(dto.getFullName());
        employee.setProfession(dto.getProfession());
        employee.setPhoneNumber(dto.getPhoneNumber());
        if (dto.getIsActive() != null) {
            employee.setIsActive(dto.getIsActive());
        }
        return mapper.toDto(repository.save(employee));
    }

    public EmployeeDto findById(UUID id) {
        Employee employee = findByUserId(id);
        return mapper.toDto(employee);
    }

    public List<EmployeeDto> findAll() {
        return mapper.toDto(repository.findAll());
    }

    public EmployeeDto delete(UUID id) {
        Employee employee = findByUserId(id);
        employee.setIsActive(false);
        return mapper.toDto(repository.save(employee));
    }

    public Employee findByUserId(UUID id) {
        return repository.findById(id).orElseThrow(() -> new IllegalArgumentException("employee not found"));
    }
}
