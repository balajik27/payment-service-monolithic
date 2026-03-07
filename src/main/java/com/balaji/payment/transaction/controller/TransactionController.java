package com.balaji.payment.transaction.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.balaji.payment.common.api.ApiResponse;
import com.balaji.payment.transaction.dto.request.TransactionRequest;
import com.balaji.payment.transaction.dto.response.TransactionResponse;
import com.balaji.payment.transaction.service.TransactionService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/transaction")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    @PostMapping
    public ResponseEntity<ApiResponse<TransactionResponse>> createTransaction(
            @Valid @RequestBody TransactionRequest request) {
        if (!transactionService.validateRequest(request)) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        TransactionResponse response = transactionService.createTransaction(request);
        return ResponseEntity.ok(ApiResponse.success(response, "Transaction processed successfully"));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<List<TransactionResponse>>> getTransactionHistory(
            @PathVariable UUID userId) {
        List<TransactionResponse> history = transactionService.getTransactionHistory(userId);
        return ResponseEntity.ok(ApiResponse.success(history, "Transaction history retrieved successfully"));
    }

}