package com.balaji.payment.config;

import com.balaji.payment.wallet.enums.Currency;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;
import java.util.List;

@Configuration
@ConfigurationProperties(prefix = "payment")
@Getter
@Setter
public class PaymentProperties {
    private Exchange exchange = new Exchange();
    private List<Currency> supportedCurrencies;

    @Getter
    @Setter
    public static class Exchange {
        private BigDecimal feeRate;
        private BigDecimal defaultRate;
    }
}
