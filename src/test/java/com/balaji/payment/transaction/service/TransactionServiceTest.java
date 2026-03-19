package com.balaji.payment.transaction.service;

import com.balaji.payment.config.PaymentProperties;
// import com.balaji.payment.notification.service.NotificationService;
// import com.balaji.payment.reward.service.RewardService;
import com.balaji.payment.transaction.dto.request.TransactionRequest;
import com.balaji.payment.transaction.repository.TransactionRepository;
import com.balaji.payment.wallet.enums.Currency;
import com.balaji.payment.wallet.service.WalletService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private WalletService walletService;
    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private ExchangeFeeService exchangeFeeService;

    // @Mock
    // private NotificationService notificationService;
    // @Mock
    // private RewardService rewardService;
    @Mock
    private PaymentProperties paymentProperties;

    @InjectMocks
    private TransactionService transactionService;

    private PaymentProperties.Exchange exchange;

    @BeforeEach
    void setUp() {
        exchange = new PaymentProperties.Exchange();
        exchange.setFeeRate(new BigDecimal("0.01"));
        exchange.setDefaultRate(new BigDecimal("0.012"));

        lenient().when(paymentProperties.getExchange()).thenReturn(exchange);
        lenient().when(paymentProperties.getSupportedCurrencies()).thenReturn(Collections.singletonList(Currency.USD));
    }

    @Test
    void validateRequest_SelfTransfer_ThrowsException() {
        UUID userId = UUID.randomUUID();
        TransactionRequest request = mock(TransactionRequest.class);
        when(request.getSenderId()).thenReturn(userId);
        when(request.getReceiverId()).thenReturn(userId);

        assertThrows(RuntimeException.class, () -> transactionService.validateRequest(request));
    }

    @Test
    void validateRequest_NegativeAmount_ThrowsException() {
        TransactionRequest request = mock(TransactionRequest.class);
        when(request.getSenderId()).thenReturn(UUID.randomUUID());
        when(request.getReceiverId()).thenReturn(UUID.randomUUID());
        when(request.getAmount()).thenReturn(new BigDecimal("-10"));

        assertThrows(RuntimeException.class, () -> transactionService.validateRequest(request));
    }

    @Test
    void calculateExchangeFee_SameCurrency_ReturnsZero() {
        BigDecimal fee = exchangeFeeService.calculateExchangeFee(Currency.USD, Currency.USD, new BigDecimal("100"));
        assertEquals(0, fee.compareTo(BigDecimal.ZERO));
    }

    @Test
    void calculateExchangeFee_DifferentCurrency_CalculatesCorrectly() {
        BigDecimal amount = new BigDecimal("100");
        BigDecimal expectedFee = amount.multiply(exchange.getFeeRate());

        BigDecimal fee = exchangeFeeService.calculateExchangeFee(Currency.USD, Currency.EUR, amount);
        assertEquals(0, expectedFee.compareTo(fee));
    }
}
