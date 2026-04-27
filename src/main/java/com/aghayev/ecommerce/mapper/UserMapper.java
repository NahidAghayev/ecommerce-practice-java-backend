package com.aghayev.ecommerce.mapper;

import com.aghayev.ecommerce.dto.request.UserCreateRequestDto;
import com.aghayev.ecommerce.dto.request.UserUpdateRequestDto;
import com.aghayev.ecommerce.dto.response.UserResponseDto;
import com.aghayev.ecommerce.entity.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public User toEntity(UserCreateRequestDto requestDto, String encodedPassword) {
        return User.builder()
                .email(requestDto.email())
                .password(encodedPassword)
                .role(requestDto.role())
                .build();
    }

    public void updateEntity(User user, UserUpdateRequestDto requestDto, String encodedPassword) {
        user.setEmail(requestDto.email());
        user.setRole(requestDto.role());

        if (encodedPassword != null) {
            user.setPassword(encodedPassword);
        }
    }

    public UserResponseDto toResponseDto(User user) {
        return new UserResponseDto(
                user.getId(),
                user.getEmail(),
                user.getRole(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }
}
