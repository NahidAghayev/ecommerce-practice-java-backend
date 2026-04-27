package com.aghayev.ecommerce.dto.response;

import com.aghayev.ecommerce.entity.User.Role;
import java.time.LocalDateTime;
import java.util.UUID;

public record UserResponseDto(
        UUID id,
        String email,
        Role role,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
