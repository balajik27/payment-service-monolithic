package com.balaji.payment.wallet.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import com.balaji.payment.wallet.enums.Currency;
import com.balaji.payment.wallet.enums.WalletStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class WalletResponse {

    private UUID id;
    private UUID userId;
    private BigDecimal balance;
    private Currency currency;
    private WalletStatus status;
    private boolean isPrimary;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
