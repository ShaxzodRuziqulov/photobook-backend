package com.example.photobook.service;

import com.example.photobook.dto.EmployeeDto;
import com.example.photobook.dto.request.EmployeePagingRequest;
import com.example.photobook.entity.Employee;
import com.example.photobook.mapper.EmployeeMapper;
import com.example.photobook.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EmployeeService {
    private final EmployeeRepository repository;
    private final EmployeeMapper mapper;

    public EmployeeDto create(EmployeeDto dto) {
        validateEmployee(dto);
        Employee employee = mapper.toEntity(dto);
        return mapper.toDto(repository.save(employee));
    }

    public EmployeeDto update(UUID id, EmployeeDto dto) {
        validateEmployee(dto);
        Employee employee = findEntityById(id);
        employee.setFullName(dto.getFullName());
        employee.setProfession(dto.getProfession());
        employee.setPhoneNumber(dto.getPhoneNumber());
        if (dto.getIsActive() != null) {
            employee.setIsActive(dto.getIsActive());
        }
        return mapper.toDto(repository.save(employee));
    }

    public EmployeeDto findById(UUID id) {
        Employee employee = findEntityById(id);
        return mapper.toDto(employee);
    }

    public List<EmployeeDto> findAll() {
        return mapper.toDto(repository.findAll());
    }

    public Page<EmployeeDto> findPage(EmployeePagingRequest request, Pageable pageable) {
        return repository.findPage(
                request.getSearch(),
                request.getProfession(),
                request.getIsActive(),
                pageable).map(mapper::toDto);
    }

    public EmployeeDto delete(UUID id) {
        Employee employee = findEntityById(id);
        employee.setIsActive(false);
        return mapper.toDto(repository.save(employee));
    }

    public Employee findEntityById(UUID id) {
        return repository.findById(id).orElseThrow(() -> new IllegalArgumentException("employee not found"));
    }

    private void validateEmployee(EmployeeDto dto) {
        if (dto.getFullName() == null || dto.getFullName().isBlank()) {
            throw new IllegalArgumentException("full_name is required");
        }
    }
}
