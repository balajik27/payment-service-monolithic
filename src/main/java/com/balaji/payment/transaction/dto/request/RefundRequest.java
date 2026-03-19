package com.balaji.payment.transaction.dto.request;

import java.util.UUID;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class RefundRequest {

    @NotNull(message = "Transaction Id is required")
    private UUID transactionId;

    @NotNull(message = "Idempotency key is required")
    private String idempotencyKey;

}