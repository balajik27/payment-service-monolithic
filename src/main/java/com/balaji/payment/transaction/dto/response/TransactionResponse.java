package com.balaji.payment.transaction.dto.response;

import java.util.UUID;

import com.balaji.payment.transaction.enums.TransactionStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionResponse {

    private UUID transactionId;
    private TransactionStatus transactionStatus;
    private String message;

}
