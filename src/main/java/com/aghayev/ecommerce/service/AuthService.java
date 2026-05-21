package com.aghayev.ecommerce.service;

import com.aghayev.ecommerce.dto.request.AuthLoginRequestDto;
import com.aghayev.ecommerce.dto.request.AuthRegisterRequestDto;
import com.aghayev.ecommerce.dto.response.AuthResponseDto;
import com.aghayev.ecommerce.entity.User;
import com.aghayev.ecommerce.exception.BadRequestException;
import com.aghayev.ecommerce.exception.UnauthorizedException;
import com.aghayev.ecommerce.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final CustomUserDetailsService customUserDetailsService;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    public AuthResponseDto register(AuthRegisterRequestDto authRegisterRequestDto) {
        if (userRepository.existsByEmail(authRegisterRequestDto.email())) {
            throw new BadRequestException("User already exits");
        }

        User user = new User();
        user.setEmail(authRegisterRequestDto.email());
        user.setPassword(passwordEncoder.encode(authRegisterRequestDto.password()));
        user.setRole(User.Role.USER);

        User savedUser = userRepository.save(user);

        UserDetails userDetails = customUserDetailsService.loadUserByUsername(savedUser.getEmail());

        String token = jwtService.generateToken(userDetails);

        return new AuthResponseDto(token);
    }

    public AuthResponseDto login(AuthLoginRequestDto authLoginRequestDto) {
        User user = userRepository.findByEmail(authLoginRequestDto.email())
                .orElseThrow(() -> new UnauthorizedException("Invalid email or password", "credentials"));

        if (!passwordEncoder.matches(authLoginRequestDto.password(), user.getPassword())) {
            throw new UnauthorizedException("Invalid email or password", "credentials");
        }

        UserDetails userDetails = customUserDetailsService.loadUserByUsername(user.getEmail());

        String token = jwtService.generateToken(userDetails);

        return new AuthResponseDto(token);
    }
}
