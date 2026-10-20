package com.avgon.portfolio.domain;

import com.avgon.common.persistence.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "purchase_record")
public class PurchaseRecord extends BaseTimeEntity {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "position_id", nullable = false, foreignKey = @ForeignKey(name = "fk_purchase_record_position"))
    private UserStockPosition position;

    @Column(nullable = false, precision = 24, scale = 8)
    private BigDecimal price;

    @Column(nullable = false, precision = 24, scale = 12)
    private BigDecimal quantity;

    @Column(name = "purchased_on", nullable = false)
    private LocalDate purchasedOn;

    protected PurchaseRecord() {
    }

    PurchaseRecord(
            UUID id,
            UserStockPosition position,
            BigDecimal price,
            BigDecimal quantity,
            LocalDate purchasedOn
    ) {
        this.id = Objects.requireNonNull(id, "매수 이력 ID는 필수입니다.");
        this.position = Objects.requireNonNull(position, "보유 종목은 필수입니다.");
        change(price, quantity, purchasedOn);
    }

    void change(BigDecimal price, BigDecimal quantity, LocalDate purchasedOn) {
        PurchaseAmount validated = new PurchaseAmount(price, quantity);
        this.price = validated.price();
        this.quantity = validated.quantity();
        this.purchasedOn = Objects.requireNonNull(purchasedOn, "매수일은 필수입니다.");
    }

    PurchaseAmount toAmount() {
        return new PurchaseAmount(price, quantity);
    }

    public UUID getId() {
        return id;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public LocalDate getPurchasedOn() {
        return purchasedOn;
    }
}
