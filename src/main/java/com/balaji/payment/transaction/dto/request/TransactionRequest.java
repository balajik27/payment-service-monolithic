package com.balaji.payment.transaction.dto.request;

import java.math.BigDecimal;
import java.util.UUID;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class TransactionRequest {

    @NotNull(message = "Sender User ID (fromUserId) is required")
    private UUID senderId;

    @NotNull(message = "Recipient User ID (toUserId) is required")
    private UUID receiverId;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than zero")
    private BigDecimal amount;

    @NotNull(message = "Idempotency key is required")
    private String idempotencyKey;

}