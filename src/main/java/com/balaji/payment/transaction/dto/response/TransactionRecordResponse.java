package com.balaji.payment.transaction.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import com.balaji.payment.transaction.enums.TransactionStatus;
import com.balaji.payment.user.dto.response.UserSimpleResponse;

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
public class TransactionRecordResponse {

    private UUID transactionId;
    private TransactionStatus transactionStatus;
    private UserSimpleResponse sender;
    private UserSimpleResponse receiver;
    private BigDecimal srcAmount;
    private BigDecimal tgtAmount;
    private BigDecimal feeAmount;
    private BigDecimal exchangeRate;
    private LocalDateTime createdAt;

}
