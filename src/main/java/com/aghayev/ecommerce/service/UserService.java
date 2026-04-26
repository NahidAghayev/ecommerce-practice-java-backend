package com.aghayev.ecommerce.service;

import com.aghayev.ecommerce.dto.UserCreateRequestDto;
import com.aghayev.ecommerce.dto.UserResponseDto;
import com.aghayev.ecommerce.dto.UserUpdateRequestDto;
import com.aghayev.ecommerce.entity.User;
import com.aghayev.ecommerce.exception.DuplicateEmailException;
import com.aghayev.ecommerce.exception.ResourceNotFoundException;
import com.aghayev.ecommerce.repository.UserRepository;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public UserResponseDto createUser(UserCreateRequestDto requestDto) {
        validateEmailUniqueness(requestDto.email());

        User user = User.builder()
                .email(requestDto.email())
                .password(passwordEncoder.encode(requestDto.password()))
                .role(requestDto.role())
                .build();

        return toResponseDto(userRepository.save(user));
    }

    public List<UserResponseDto> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(this::toResponseDto)
                .toList();
    }

    public UserResponseDto getUserById(UUID id) {
        return toResponseDto(findUserById(id));
    }

    public UserResponseDto updateUser(UUID id, UserUpdateRequestDto requestDto) {
        User user = findUserById(id);

        if (!user.getEmail().equals(requestDto.email())) {
            validateEmailUniqueness(requestDto.email());
        }

        user.setEmail(requestDto.email());
        user.setRole(requestDto.role());
        if (requestDto.password() != null && !requestDto.password().isBlank()) {
            user.setPassword(passwordEncoder.encode(requestDto.password()));
        }

        return toResponseDto(userRepository.save(user));
    }

    public void deleteUser(UUID id) {
        User user = findUserById(id);
        userRepository.delete(user);
    }

    private User findUserById(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
    }

    private void validateEmailUniqueness(String email) {
        if (userRepository.existsByEmail(email)) {
            throw new DuplicateEmailException("Email already exists");
        }
    }

    private UserResponseDto toResponseDto(User user) {
        return new UserResponseDto(
                user.getId(),
                user.getEmail(),
                user.getRole(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }
}
