package com.example.photobook.service;

import com.example.photobook.dto.UserDto;
import com.example.photobook.dto.UserProfileUpdateDto;
import com.example.photobook.entity.User;
import com.example.photobook.mapper.UserMapper;
import com.example.photobook.repository.UserRepository;
import com.example.photobook.service.security.CurrentUserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    private final UserRepository userRepository = mock(UserRepository.class);
    private final UserMapper userMapper = mock(UserMapper.class);
    private final PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
    private final RoleService roleService = mock(RoleService.class);
    private final CurrentUserService currentUserService = mock(CurrentUserService.class);
    private final UploadService uploadService = mock(UploadService.class);

    @Test
    void updateCurrentUserProfileAttachesUploadedAvatarWhenUploadIdProvided() {
        UserService service = new UserService(
                userRepository,
                userMapper,
                passwordEncoder,
                roleService,
                currentUserService,
                uploadService
        );

        User currentUser = new User();
        currentUser.setAvatarUrl("/uploads-storage/old.png");

        UserDto mappedDto = new UserDto();
        UUID uploadId = UUID.randomUUID();

        UserProfileUpdateDto dto = new UserProfileUpdateDto();
        dto.setFirstName("Ali");
        dto.setLastName("Valiyev");
        dto.setAvatarUrl("https://example.com/should-not-win.png");
        dto.setUploadId(uploadId);

        when(currentUserService.getCurrentUser()).thenReturn(currentUser);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(userMapper.toDto(currentUser)).thenReturn(mappedDto);

        UserDto response = service.updateCurrentUserProfile(dto);

        assertSame(mappedDto, response);
        verify(uploadService).attachToOwner(uploadId, com.example.photobook.entity.enumirated.OwnerType.USER, currentUser.getId());
        verify(userRepository, atLeastOnce()).save(currentUser);
    }
}
