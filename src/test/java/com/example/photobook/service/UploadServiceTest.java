package com.example.photobook.service;

import com.example.photobook.dto.UploadDto;
import com.example.photobook.entity.Order;
import com.example.photobook.entity.Upload;
import com.example.photobook.entity.enumirated.OwnerType;
import com.example.photobook.repository.ExpenseRepository;
import com.example.photobook.repository.OrderRepository;
import com.example.photobook.repository.UploadRepository;
import com.example.photobook.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.util.unit.DataSize;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UploadServiceTest {

    private final UploadRepository uploadRepository = mock(UploadRepository.class);
    private final UserRepository userRepository = mock(UserRepository.class);
    private final OrderRepository orderRepository = mock(OrderRepository.class);
    private final ExpenseRepository expenseRepository = mock(ExpenseRepository.class);

    @TempDir
    Path tempDir;

    @Test
    void uploadRejectsFileLargerThanConfiguredLimit() {
        UploadService service = new UploadService(
                uploadRepository,
                userRepository,
                orderRepository,
                expenseRepository,
                tempDir.toString(),
                DataSize.ofBytes(4)
        );

        MockMultipartFile file = new MockMultipartFile("file", "a.png", "image/png", new byte[]{1, 2, 3, 4, 5});
        UploadDto dto = new UploadDto();

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> service.upload(file, dto));

        assertEquals("File too large", exception.getMessage());
        verify(uploadRepository, never()).save(any());
    }

    @Test
    void attachToOwnerReplacesExistingOwnedUpload() throws Exception {
        UploadService service = new UploadService(
                uploadRepository,
                userRepository,
                orderRepository,
                expenseRepository,
                tempDir.toString(),
                DataSize.ofMegabytes(5)
        );

        UUID orderId = UUID.randomUUID();
        UUID newUploadId = UUID.randomUUID();
        UUID oldUploadId = UUID.randomUUID();

        Upload newUpload = new Upload();
        newUpload.setId(newUploadId);
        newUpload.setKey("new.png");

        Upload oldUpload = new Upload();
        oldUpload.setId(oldUploadId);
        oldUpload.setKey("old.png");
        oldUpload.setOwnerType(OwnerType.ORDER);
        oldUpload.setOwnerId(orderId);

        Order order = new Order();
        order.setUpload(oldUpload);
        order.setImageUrl("/uploads-storage/old.png");

        Files.writeString(tempDir.resolve("old.png"), "old");

        when(orderRepository.existsById(orderId)).thenReturn(true);
        when(uploadRepository.findById(newUploadId)).thenReturn(Optional.of(newUpload));
        when(uploadRepository.findByOwnerTypeAndOwnerId(OwnerType.ORDER, orderId)).thenReturn(Optional.of(oldUpload));
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(uploadRepository.save(any(Upload.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Upload attached = service.attachToOwner(newUploadId, OwnerType.ORDER, orderId);

        assertSame(newUpload, attached);
        assertEquals(OwnerType.ORDER, attached.getOwnerType());
        assertEquals(orderId, attached.getOwnerId());
        assertFalse(Files.exists(tempDir.resolve("old.png")));
        assertNull(order.getUpload());
        assertNull(order.getImageUrl());
        verify(orderRepository, atLeastOnce()).save(order);
        verify(uploadRepository).delete(oldUpload);
    }

    @Test
    void deleteClearsOwnerReferenceAndRemovesPhysicalFile() throws Exception {
        UploadService service = new UploadService(
                uploadRepository,
                userRepository,
                orderRepository,
                expenseRepository,
                tempDir.toString(),
                DataSize.ofMegabytes(5)
        );

        UUID uploadId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();

        Upload upload = new Upload();
        upload.setId(uploadId);
        upload.setKey("receipt.png");
        upload.setOwnerType(OwnerType.ORDER);
        upload.setOwnerId(orderId);

        Order order = new Order();
        order.setUpload(upload);
        order.setImageUrl("/uploads-storage/receipt.png");

        Files.writeString(tempDir.resolve("receipt.png"), "file");

        when(uploadRepository.findByKey("receipt.png")).thenReturn(Optional.of(upload));
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        service.delete("receipt.png");

        assertFalse(Files.exists(tempDir.resolve("receipt.png")));
        assertNull(order.getUpload());
        assertNull(order.getImageUrl());
        verify(orderRepository).save(order);
        verify(uploadRepository).delete(upload);
    }
}
