package com.example.photobook.service;

import com.example.photobook.dto.CustomerDto;
import com.example.photobook.dto.request.CustomerPagingRequest;
import com.example.photobook.entity.Customer;
import com.example.photobook.mapper.CustomerMapper;
import com.example.photobook.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

import static com.example.photobook.util.StringUtils.normalize;

@Service
@RequiredArgsConstructor
public class CustomerService {
    private final CustomerRepository repository;
    private final CustomerMapper mapper;

    public CustomerDto create(CustomerDto dto) {
        validateCustomer(dto);
        Customer customer = mapper.toEntity(dto);
        return mapper.toDto(repository.save(customer));
    }

    public Customer createForOrder(String customerName) {
        Customer customer = new Customer();
        customer.setFullName(customerName);
        return repository.save(customer);
    }

    public Customer resolveForOrder(UUID customerId, String customerName) {
        if (customerId != null) {
            return findEntityById(customerId);
        }
        return createForOrder(normalize(customerName));
    }

    public CustomerDto update(UUID id, CustomerDto dto) {
        validateCustomer(dto);
        Customer customer = findEntityById(id);
        customer.setFullName(dto.getFullName());
        customer.setPhone(dto.getPhone());
        customer.setNotes(dto.getNotes());
        if (dto.getIsActive() != null) {
            customer.setIsActive(dto.getIsActive());
        }
        return mapper.toDto(repository.save(customer));
    }

    public CustomerDto findById(UUID id) {
        Customer customer = findEntityById(id);
        return mapper.toDto(customer);
    }

    public List<CustomerDto> findAll() {
        return mapper.toDto(repository.findAllIsActive());
    }

    public Page<CustomerDto> findPage(CustomerPagingRequest request, Pageable pageable) {
        return repository.findPage(
                request.getSearch(),
                request.getIsActive(),
                pageable).map(mapper::toDto);
    }

    public CustomerDto delete(UUID id) {
        Customer customer = findEntityById(id);
        customer.setIsActive(false);
        return mapper.toDto(repository.save(customer));
    }

    public Customer findEntityById(UUID id) {
        return repository.findById(id).orElseThrow(() -> new IllegalArgumentException("customer not found"));
    }

    private void validateCustomer(CustomerDto dto) {
        if (dto.getFullName() == null || dto.getFullName().isBlank()) {
            throw new IllegalArgumentException("full_name is required");
        }
    }
}
