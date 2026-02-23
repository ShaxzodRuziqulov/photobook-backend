package com.example.photobook.service;

import com.example.photobook.dto.RoleDto;
import com.example.photobook.entity.Role;
import com.example.photobook.mapper.RoleMapper;
import com.example.photobook.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RoleService {
    private final RoleRepository repository;
    private final RoleMapper mapper;

    public RoleDto create(RoleDto dto) {
        Role order = mapper.toEntity(dto);

        return mapper.toDto(repository.save(order));
    }

    public RoleDto update(UUID id, RoleDto dto) {
        Role order = findByRoleId(id);
        order.setName(dto.getName());
        order.setDescription(dto.getDescription());
        return mapper.toDto(repository.save(order));
    }

    public RoleDto findById(UUID id) {
        Role role = findByRoleId(id);
        return mapper.toDto(role);
    }

    public List<RoleDto> findAll() {
        return mapper.toDto(repository.findAll());
    }

    public void delete(UUID id) {
        Role order = findByRoleId(id);
        repository.delete(order);
    }

    public Role findByRoleId(UUID id) {
        return repository.findById(id).orElseThrow(() -> new IllegalArgumentException("order not found"));
    }
}
