package com.example.photobook.service;

import com.example.photobook.dto.MaterialDto;
import com.example.photobook.entity.Material;
import com.example.photobook.mapper.MaterialMapper;
import com.example.photobook.repository.MaterialRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MaterialService {

    private final MaterialRepository repository;
    private final MaterialMapper mapper;

    public MaterialDto create(MaterialDto dto) {
        Material material = mapper.toEntity(dto);

        return mapper.toDto(repository.save(material));
    }

    public MaterialDto update(UUID id, MaterialDto dto) {
        Material material = findByMaterialId(id);
        material.setId(dto.getId());
        material.setItemName(dto.getItemName());
        material.setItemType(dto.getItemType());
        material.setUnitName(dto.getUnitName());
        material.setQuantity(dto.getQuantity());
        return mapper.toDto(repository.save(material));
    }

    public MaterialDto findById(UUID id) {
        Material material = findByMaterialId(id);
        return mapper.toDto(material);
    }

    public List<MaterialDto> findAll() {
        return mapper.toDto(repository.findAll());
    }

    public void delete(UUID id) {
        Material expenseCategory = findByMaterialId(id);
        repository.delete(expenseCategory);
    }

    public Material findByMaterialId(UUID id) {
        return repository.findById(id).orElseThrow(() -> new IllegalArgumentException("material not found"));
    }
}
