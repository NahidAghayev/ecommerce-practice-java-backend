package com.aghayev.ecommerce.service;

import com.aghayev.ecommerce.config.LogExecutionTime;
import com.aghayev.ecommerce.dto.UserCreateRequestDto;
import com.aghayev.ecommerce.dto.UserResponseDto;
import com.aghayev.ecommerce.dto.UserUpdateRequestDto;
import com.aghayev.ecommerce.entity.User;
import com.aghayev.ecommerce.exception.BadRequestException;
import com.aghayev.ecommerce.exception.ResourceNotFoundException;
import com.aghayev.ecommerce.repository.UserRepository;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @LogExecutionTime
    public UserResponseDto createUser(UserCreateRequestDto requestDto) {
        log.info("action=createUser email={} role={}", requestDto.email(), requestDto.role());
        validateEmailUniqueness(requestDto.email());

        User user = User.builder()
                .email(requestDto.email())
                .password(passwordEncoder.encode(requestDto.password()))
                .role(requestDto.role())
                .build();

        User savedUser = userRepository.save(user);
        log.info("action=createUser status=SUCCESS userId={}", savedUser.getId());
        return toResponseDto(savedUser);
    }

    @LogExecutionTime
    public List<UserResponseDto> getAllUsers() {
        log.debug("action=getAllUsers");
        return userRepository.findAll()
                .stream()
                .map(this::toResponseDto)
                .toList();
    }

    @LogExecutionTime
    public UserResponseDto getUserById(UUID id) {
        log.debug("action=getUserById userId={}", id);
        return toResponseDto(findUserById(id));
    }

    @LogExecutionTime
    public UserResponseDto updateUser(UUID id, UserUpdateRequestDto requestDto) {
        log.info("action=updateUser userId={} newEmail={} role={}", id, requestDto.email(), requestDto.role());
        User user = findUserById(id);

        if (!user.getEmail().equals(requestDto.email())) {
            validateEmailUniqueness(requestDto.email());
        }

        user.setEmail(requestDto.email());
        user.setRole(requestDto.role());
        if (requestDto.password() != null && !requestDto.password().isBlank()) {
            user.setPassword(passwordEncoder.encode(requestDto.password()));
            log.debug("action=updateUser passwordChanged=true userId={}", id);
        }

        User updatedUser = userRepository.save(user);
        log.info("action=updateUser status=SUCCESS userId={}", updatedUser.getId());
        return toResponseDto(updatedUser);
    }

    @LogExecutionTime
    public void deleteUser(UUID id) {
        log.info("action=deleteUser userId={}", id);
        User user = findUserById(id);
        userRepository.delete(user);
        log.info("action=deleteUser status=SUCCESS userId={}", id);
    }

    private User findUserById(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
    }

    private void validateEmailUniqueness(String email) {
        if (userRepository.existsByEmail(email)) {
            log.warn("action=validateEmailUniqueness status=DUPLICATE email={}", email);
            throw new BadRequestException("Email already exists", "email");
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
