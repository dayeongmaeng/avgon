package com.avgon.portfolio.domain;

import com.avgon.common.persistence.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "position_baseline")
public class PositionBaseline extends BaseTimeEntity {

    @Id
    @Column(name = "position_id")
    private UUID positionId;

    @MapsId
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "position_id", foreignKey = @ForeignKey(name = "fk_position_baseline_position"))
    private UserStockPosition position;

    @Column(name = "average_price", nullable = false, precision = 24, scale = 8)
    private BigDecimal averagePrice;

    @Column(nullable = false, precision = 24, scale = 12)
    private BigDecimal quantity;

    protected PositionBaseline() {
    }

    PositionBaseline(UserStockPosition position, BigDecimal averagePrice, BigDecimal quantity) {
        this.position = position;
        change(averagePrice, quantity);
    }

    public void change(BigDecimal averagePrice, BigDecimal quantity) {
        PositionBaselineAmount validated = new PositionBaselineAmount(averagePrice, quantity);
        this.averagePrice = validated.averagePrice();
        this.quantity = validated.quantity();
    }

    public PositionBaselineAmount toAmount() {
        return new PositionBaselineAmount(averagePrice, quantity);
    }

    public BigDecimal getAveragePrice() {
        return averagePrice;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }
}
