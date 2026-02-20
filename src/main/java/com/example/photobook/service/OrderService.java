package com.example.photobook.service;

import com.example.photobook.dto.OrderDto;
import com.example.photobook.entity.Order;
import com.example.photobook.mapper.OrderMapper;
import com.example.photobook.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderService {
    private final OrderRepository repository;
    private final OrderMapper mapper;
    private final ProductCategoryService productCategoryService;
    private final CustomerService customerService;
    private final EmployeeService employeeService;

    public OrderDto create(OrderDto dto) {
        Order Order = mapper.toEntity(dto);

        return mapper.toDto(repository.save(Order));
    }

    public OrderDto update(UUID id, OrderDto dto) {
        Order order = findByOrderId(id);
        order.setId(dto.getId());
        order.setKind(dto.getKind());
        order.setCategory(productCategoryService.findByProductCategoryId(dto.getCategoryId()));
        order.setOrderName(dto.getOrderName());
        order.setItemType(dto.getItemType());
        order.setCustomer(customerService.findByUserId(dto.getCustomerId()));
        order.setReceiverName(dto.getReceiverName());
        order.setEmployee(employeeService.findByUserId(dto.getEmployeeId()));
        order.setPageCount(dto.getPageCount());
        order.setAmount(dto.getAmount());
        order.setProcessedCount(dto.getProcessedCount());
        order.setAcceptedDate(dto.getAcceptedDate());
        order.setDeadline(dto.getDeadline());
        order.setStatus(dto.getStatus());

        return mapper.toDto(repository.save(order));
    }

    public OrderDto findById(UUID id) {
        Order Order = findByOrderId(id);
        return mapper.toDto(Order);
    }

    public List<OrderDto> findAll() {
        return mapper.toDto(repository.findAll());
    }

    public void delete(UUID id) {
        Order order = findByOrderId(id);
        repository.delete(order);
    }

    public Order findByOrderId(UUID id) {
        return repository.findById(id).orElseThrow(() -> new IllegalArgumentException("order not found"));
    }
}
