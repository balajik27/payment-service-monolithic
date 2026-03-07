package com.balaji.payment.wallet.dto.request;

import java.math.BigDecimal;
import java.util.UUID;

import com.balaji.payment.wallet.enums.Currency;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class WalletTransferRequest {

    @NotNull(message = "Sender User ID (fromUserId) is required")
    private UUID fromUserId;

    @NotNull(message = "Recipient User ID (toUserId) is required")
    private UUID toUserId;

    private Currency currency;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than zero")
    private BigDecimal amount;
}
