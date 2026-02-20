package com.example.photobook.service;

import com.example.photobook.dto.CustomerDto;
import com.example.photobook.entity.Customer;
import com.example.photobook.mapper.CustomerMapper;
import com.example.photobook.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CustomerService {
    private final CustomerRepository repository;
    private final CustomerMapper mapper;

    public CustomerDto create(CustomerDto dto) {
        Customer customer = mapper.toEntity(dto);
        return mapper.toDto(repository.save(customer));
    }

    public CustomerDto update(UUID id, CustomerDto dto) {
        Customer customer = findByUserId(id);
        customer.setId(dto.getId());
        customer.setFullName(dto.getFullName());
        customer.setPhone(dto.getPhone());
        customer.setNotes(dto.getNotes());
        if (dto.getIsActive() != null) {
            customer.setIsActive(dto.getIsActive());
        }
        return mapper.toDto(repository.save(customer));
    }

    public CustomerDto findById(UUID id) {
        Customer customer = findByUserId(id);
        return mapper.toDto(customer);
    }

    public List<CustomerDto> findAll() {
        return mapper.toDto(repository.findAll());
    }

    public CustomerDto delete(UUID id) {
        Customer customer = findByUserId(id);
        customer.setIsActive(false);
        return mapper.toDto(repository.save(customer));
    }

    public Customer findByUserId(UUID id) {
        return repository.findById(id).orElseThrow(() -> new IllegalArgumentException("customer not found"));
    }
}
