package com.balaji.payment.transaction.service;

import java.math.BigDecimal;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.balaji.payment.config.PaymentProperties;
import com.balaji.payment.wallet.enums.Currency;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ExchangeFeeService {
    private final PaymentProperties paymentProperties;

    private static final Map<String, BigDecimal> RATES = Map.of(
            "INR-USD", new BigDecimal("0.012"),
            "USD-INR", new BigDecimal("83.30"),
            "EUR-INR", new BigDecimal("90.50"),
            "INR-EUR", new BigDecimal("0.011"));

    public BigDecimal getExchangeRate(Currency src, Currency tgt) {

        if (src == tgt)
            return BigDecimal.ONE;

        String key = src + "-" + tgt;

        BigDecimal rate = RATES.get(key);

        if (rate == null) {
            throw new RuntimeException("Exchange rate not supported for: " + key);
        }

        return rate;

    }

    public BigDecimal calculateExchangeFee(Currency sendCurrency, Currency receiverCurrency, BigDecimal amount) {
        if (sendCurrency == receiverCurrency) {
            return BigDecimal.ZERO;
        }
        return amount.multiply(paymentProperties.getExchange().getFeeRate());
    }
    
}
