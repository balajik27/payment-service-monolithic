package com.balaji.payment.wallet.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.balaji.payment.common.api.ApiResponse;
import com.balaji.payment.wallet.dto.response.WalletResponse;
import com.balaji.payment.wallet.service.WalletService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/wallets")
@RequiredArgsConstructor
public class WalletController {

    private final WalletService walletService;

    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<List<WalletResponse>>> getUserWallets(
            @PathVariable UUID userId) {
        java.util.List<WalletResponse> wallets = walletService
                .getUserWallets(userId);
        return ResponseEntity
                .ok(ApiResponse.success(wallets, "User wallets retrieved successfully"));
    }
}