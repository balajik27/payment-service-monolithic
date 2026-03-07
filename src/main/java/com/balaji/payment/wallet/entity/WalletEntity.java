package com.balaji.payment.wallet.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.balaji.payment.user.entity.UserEntity;
import com.balaji.payment.wallet.enums.Currency;
import com.balaji.payment.wallet.enums.WalletStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "wallets", uniqueConstraints = {
        @UniqueConstraint(columnNames = { "user_id", "currency" })
})
@Getter
public class WalletEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    // Establishing a 1-to-1 relationship with User
    // FetchType.LAZY means user details are fetched only when accessed
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Currency currency; // E.g., USD, INR, EUR

    // Balance stored exactly. Using BigDecimal is crucial for finances!
    @Column(nullable = false, precision = 19, scale = 4)
    @Setter
    private BigDecimal balance = BigDecimal.ZERO;

    @Column(nullable = false)
    @Setter
    private boolean isPrimary = false;

    @Column(nullable = false)
    @Setter
    @Enumerated(EnumType.STRING) // Without it, Hibernate will store the status as an integer (0, 1, 2) in the
                                 // database
    private WalletStatus status = WalletStatus.ACTIVE; // E.g., ACTIVE, LOCKED, SUSPENDED

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    protected WalletEntity() {
    }

    private WalletEntity(Builder builder) {
        this.user = builder.user;
        this.currency = builder.currency;
        this.balance = builder.balance;
        this.isPrimary = builder.isPrimary;
        this.status = builder.status != null ? builder.status : WalletStatus.ACTIVE;
    }

    public static class Builder {

        private final UserEntity user;
        private final Currency currency;
        private BigDecimal balance = BigDecimal.ZERO;
        private boolean isPrimary;
        private WalletStatus status = WalletStatus.ACTIVE;

        public Builder(UserEntity user, Currency currency) {
            this.user = user;
            this.currency = currency;
        }

        public Builder withBalance(BigDecimal balance) {
            this.balance = (balance != null) ? balance : BigDecimal.ZERO;
            return this;
        }

        public Builder withPrimary(boolean isPrimary) {
            this.isPrimary = isPrimary;
            return this;
        }

        public Builder withStatus(WalletStatus status) {
            this.status = status;
            return this;
        }

        public WalletEntity build() {
            return new WalletEntity(this);
        }

    }

}