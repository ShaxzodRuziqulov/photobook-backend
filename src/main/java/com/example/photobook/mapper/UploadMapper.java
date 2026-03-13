package com.example.photobook.mapper;

import com.example.photobook.dto.UploadDto;
import com.example.photobook.entity.Upload;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UploadMapper extends EntityMapper<UploadDto, Upload>{
}
