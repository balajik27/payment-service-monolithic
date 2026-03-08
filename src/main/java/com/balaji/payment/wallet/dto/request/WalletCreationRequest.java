package com.balaji.payment.wallet.dto.request;

import com.balaji.payment.wallet.enums.Currency;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WalletCreationRequest {

    @NotNull(message = "User ID is required")
    private UUID userId;

    @NotNull(message = "Currency Type mandatory")
    @Enumerated(EnumType.STRING)
    private Currency currency;

}
