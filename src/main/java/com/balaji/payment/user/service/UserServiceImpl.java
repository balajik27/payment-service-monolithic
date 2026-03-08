package com.balaji.payment.user.service;

import java.util.UUID;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.context.annotation.Lazy;

import com.balaji.payment.common.security.JwtUtil;
import com.balaji.payment.user.dto.request.UserLoginRequest;
import com.balaji.payment.user.dto.request.UserRegisterRequest;
import com.balaji.payment.user.dto.response.UserLoginResponse;
import com.balaji.payment.user.dto.response.UserRegisterResponse;
import com.balaji.payment.user.entity.UserEntity;

import com.balaji.payment.user.repository.UserRepository;
import com.balaji.payment.user.security.CustomUserDetailsService;
import com.balaji.payment.wallet.service.WalletService;
import com.balaji.payment.wallet.enums.Currency;



@Service
public class UserServiceImpl implements UserService {

        private final UserRepository userRepository;
        private final PasswordEncoder passwordEncoder;
        private final AuthenticationManager authenticationManager;
        private final JwtUtil jwtUtil;
        private final CustomUserDetailsService userDetailsService;
        private final WalletService walletService;

        public UserServiceImpl(UserRepository userRepository,
                        PasswordEncoder passwordEncoder,
                        AuthenticationManager authenticationManager,
                        JwtUtil jwtUtil,
                        CustomUserDetailsService userDetailsService,
                        @Lazy WalletService walletService) {
                this.userRepository = userRepository;
                this.passwordEncoder = passwordEncoder;
                this.authenticationManager = authenticationManager;
                this.jwtUtil = jwtUtil;
                this.userDetailsService = userDetailsService;
                this.walletService = walletService;
        }

        @Override
        @Transactional // user registration and wallet creation should be atomic
        public UserRegisterResponse registerUser(UserRegisterRequest request) {

                if (userRepository.existsByEmail(request.getEmail())) {
                        throw new RuntimeException("User already exists");
                }

                UserEntity user = new UserEntity.Builder(request.getName(), request.getEmail(),
                                passwordEncoder.encode(request.getPassword()))
                                .withVerified(false)
                                .build();

                user = userRepository.save(user); // Make sure we capture returning user

                // Create a default wallet for the user
                walletService.initializeWallet(user, true,
                                request.getCurrency() != null ? request.getCurrency() : Currency.INR);

                UserRegisterResponse userResponse = UserRegisterResponse.builder()
                                .id(user.getId())
                                .name(user.getName())
                                .email(user.getEmail())
                                .status(user.getStatus())
                                .build();

                return userResponse;
        }

        @Override
        public UserLoginResponse loginUser(UserLoginRequest request) {

                authenticationManager.authenticate(
                                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));

                UserEntity user = userRepository.findByEmail(request.getEmail())
                                .orElseThrow(() -> new RuntimeException("User not found"));

                UserDetails userDetails = userDetailsService.loadUserByUsername(user.getEmail());
                String jwtToken = jwtUtil.generateToken(userDetails);

                UserLoginResponse userResponse = UserLoginResponse.builder()
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

        public UserEntity fetchUserById(UUID userId) {
                return userRepository.findById(userId)
                                .orElseThrow(() -> new RuntimeException("User not found or userid is null"));
        }

}
