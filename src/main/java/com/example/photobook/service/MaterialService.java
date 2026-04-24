package com.example.photobook.service;

import com.example.photobook.dto.MaterialDto;
import com.example.photobook.dto.MaterialAdjustDto;
import com.example.photobook.dto.request.MaterialPagingRequest;
import com.example.photobook.entity.Material;
import com.example.photobook.mapper.MaterialMapper;
import com.example.photobook.repository.MaterialRepository;
import com.example.photobook.util.StringUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MaterialService {

    private final MaterialRepository repository;
    private final MaterialMapper mapper;

    public MaterialDto create(MaterialDto dto) {
        validateMaterial(dto);
        Material material = mapper.toEntity(dto);

        return mapper.toDto(repository.save(material));
    }

    public MaterialDto update(UUID id, MaterialDto dto) {
        validateMaterial(dto);
        Material material = findByMaterialId(id);
        material.setItemName(dto.getItemName());
        material.setItemType(dto.getItemType());
        material.setUnitName(dto.getUnitName());
        material.setQuantity(dto.getQuantity());
        return mapper.toDto(repository.save(material));
    }

    public MaterialDto adjust(UUID id, MaterialAdjustDto dto) {
        if (dto.getDelta() == null) {
            throw new IllegalArgumentException("delta is required");
        }
        Material material = findByMaterialId(id);
        if (material.getQuantity().add(dto.getDelta()).signum() < 0) {
            throw new IllegalArgumentException("quantity cannot be negative");
        }
        material.setQuantity(material.getQuantity().add(dto.getDelta()));
        return mapper.toDto(repository.save(material));
    }

    public MaterialDto findById(UUID id) {
        Material material = findByMaterialId(id);
        return mapper.toDto(material);
    }

    public List<MaterialDto> findAll() {
        return mapper.toDto(repository.findAll());
    }

    public Page<MaterialDto> findPage(MaterialPagingRequest request, Pageable pageable) {
        String search = StringUtils.normalize(request.getSearch());
        Page<Material> page = search == null
                ? repository.findPageWithoutTextSearch(request.getItemType(), pageable)
                : repository.findPageWithTextSearch(search, request.getItemType(), pageable);
        return page.map(mapper::toDto);
    }

    public void delete(UUID id) {
        Material expenseCategory = findByMaterialId(id);
        repository.delete(expenseCategory);
    }

    public Material findByMaterialId(UUID id) {
        return repository.findById(id).orElseThrow(() -> new IllegalArgumentException("material not found"));
    }

    private void validateMaterial(MaterialDto dto) {
        if (dto.getItemName() == null || dto.getItemName().isBlank()) {
            throw new IllegalArgumentException("item_name is required");
        }
        if (dto.getUnitName() == null || dto.getUnitName().isBlank()) {
            throw new IllegalArgumentException("unit_name is required");
        }
        if (dto.getQuantity() == null || dto.getQuantity().signum() < 0) {
            throw new IllegalArgumentException("quantity must be greater than or equal to 0");
        }
    }
}
