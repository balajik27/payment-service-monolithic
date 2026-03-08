package com.balaji.payment.user.controller;

import java.time.LocalDateTime;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.balaji.payment.common.api.ApiResponse;
import com.balaji.payment.user.dto.request.UserLoginRequest;
import com.balaji.payment.user.dto.request.UserRegisterRequest;
import com.balaji.payment.user.dto.response.UserLoginResponse;
import com.balaji.payment.user.dto.response.UserRegisterResponse;
import com.balaji.payment.user.service.UserService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserRegisterResponse>> registerUser(
            @Valid @RequestBody UserRegisterRequest registerRequest) {
        UserRegisterResponse result = userService.registerUser(registerRequest);

        ApiResponse<UserRegisterResponse> response = ApiResponse.<UserRegisterResponse>builder()
                .success(true)
                .message("User registered successfully")
                .data(result)
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<UserLoginResponse>> loginUser(@Valid @RequestBody UserLoginRequest loginRequest) {
        UserLoginResponse result = userService.loginUser(loginRequest);

        ApiResponse<UserLoginResponse> response = ApiResponse.<UserLoginResponse>builder()
                .success(true)
                .message("User logged in successfully")
                .data(result)
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.ok(response);
    }

}