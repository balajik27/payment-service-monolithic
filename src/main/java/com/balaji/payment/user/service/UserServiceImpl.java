package com.balaji.payment.user.service;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.balaji.payment.common.security.JwtUtil;
import com.balaji.payment.user.dto.request.UserLoginRequest;
import com.balaji.payment.user.dto.request.UserRegisterRequest;
import com.balaji.payment.user.dto.response.UserResponse;
import com.balaji.payment.user.entity.UserEntity;

import com.balaji.payment.user.repository.UserRepository;
import com.balaji.payment.user.security.CustomUserDetailsService;
import com.balaji.payment.wallet.service.WalletService;
import com.balaji.payment.wallet.enums.Currency;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

        private final UserRepository userRepository;
        private final PasswordEncoder passwordEncoder;
        private final AuthenticationManager authenticationManager;
        private final JwtUtil jwtUtil;
        private final CustomUserDetailsService userDetailsService;
        private final WalletService walletService;

        @Override
        @Transactional // user registration and wallet creation should be atomic
        public UserResponse registerUser(UserRegisterRequest request) {

                if (userRepository.existsByEmail(request.getEmail())) {
                        throw new RuntimeException("User already exists");
                }

                UserEntity user = new UserEntity.Builder(request.getName(), request.getEmail(),
                                passwordEncoder.encode(request.getPassword()))
                                .withVerified(false)
                                .build();

                user = userRepository.save(user); // Make sure we capture returning user

                // Create a default wallet for the user
                walletService.createWallet(user, request.getCurrency() != null ? request.getCurrency() : Currency.INR);

                UserResponse userResponse = UserResponse.builder()
                                .id(user.getId())
                                .name(user.getName())
                                .email(user.getEmail())
                                .status(user.getStatus())
                                .createdAt(user.getCreatedAt())
                                .updatedAt(user.getUpdatedAt())
                                .build();

                return userResponse;
        }

        @Override
        public UserResponse loginUser(UserLoginRequest request) {

                authenticationManager.authenticate(
                                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));

                UserEntity user = userRepository.findByEmail(request.getEmail())
                                .orElseThrow(() -> new RuntimeException("User not found"));

                UserDetails userDetails = userDetailsService.loadUserByUsername(user.getEmail());
                String jwtToken = jwtUtil.generateToken(userDetails);

                UserResponse userResponse = UserResponse.builder()
                                .id(user.getId())
                                .name(user.getName())
                                .email(user.getEmail())
                                .status(user.getStatus())
                                .createdAt(user.getCreatedAt())
                                .updatedAt(user.getUpdatedAt())
                                .token(jwtToken)
                                .build();

                return userResponse;
        }

}
