package com.aghayev.ecommerce.controller;


import com.aghayev.ecommerce.dto.ApiResponse;
import com.aghayev.ecommerce.dto.request.AuthLoginRequestDto;
import com.aghayev.ecommerce.dto.request.AuthRegisterRequestDto;
import com.aghayev.ecommerce.dto.response.AuthResponseDto;
import com.aghayev.ecommerce.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponseDto>> register(
            @Valid @RequestBody AuthRegisterRequestDto authRegisterRequestDto
        ) {
        AuthResponseDto authResponseDto = authService.register(authRegisterRequestDto);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(authResponseDto, "User created successfully"));

    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponseDto>> login(
            @Valid @RequestBody AuthLoginRequestDto authLoginRequestDto
        ) {
        AuthResponseDto authResponseDto = authService.login(authLoginRequestDto);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success(authResponseDto, "User logged in successfully"));
    }
}
