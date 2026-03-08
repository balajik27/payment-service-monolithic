package com.balaji.payment.wallet.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.balaji.payment.common.api.ApiResponse;
import com.balaji.payment.wallet.dto.request.WalletCreationRequest;
import com.balaji.payment.wallet.dto.request.WalletDepositRequest;
import com.balaji.payment.wallet.dto.response.WalletResponse;
import com.balaji.payment.wallet.service.WalletService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/wallets")
@RequiredArgsConstructor
public class WalletController {

        private final WalletService walletService;

        @GetMapping("/user/{userId}")
        public ResponseEntity<ApiResponse<List<WalletResponse>>> getUserWallets(
                        @PathVariable("userId") UUID userId) {
                List<WalletResponse> wallets = walletService.getUserWallets(userId);
                return ResponseEntity.ok(ApiResponse.success(wallets, "User wallets retrieved successfully"));
        }

        @PostMapping("/deposit")
        public ResponseEntity<ApiResponse<WalletResponse>> deposit(
                        @Valid @RequestBody WalletDepositRequest request) {
                WalletResponse wallet = walletService.deposit(
                                request.getUserId(),
                                request.getAmount(),
                                request.getCurrency());
                return ResponseEntity.ok(ApiResponse.success(wallet, "Deposit successful"));
        }

        @PostMapping("/add-wallet")
        public ResponseEntity<ApiResponse<WalletResponse>> addWallet(
                        @Valid @RequestBody WalletCreationRequest request) {
                WalletResponse wallet = walletService.addWallet(request.getUserId(), request.getCurrency());
                return ResponseEntity.ok(ApiResponse.success(wallet, "Wallet created successfully"));
        }
}