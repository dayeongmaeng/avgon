package com.avgon.portfolio.domain;

import static com.avgon.common.domain.DecimalPolicy.nonNegativeCost;
import static com.avgon.common.domain.DecimalPolicy.nonNegativeQuantity;
import static com.avgon.common.domain.DecimalPolicy.positivePrice;

import java.math.BigDecimal;

public record PositionSummary(BigDecimal averagePrice, BigDecimal totalCost, BigDecimal quantity) {

    public PositionSummary {
        totalCost = nonNegativeCost(totalCost, "총원가");
        quantity = nonNegativeQuantity(quantity, "총수량");
        if (quantity.signum() == 0) {
            if (averagePrice != null || totalCost.signum() != 0) {
                throw new IllegalArgumentException("총수량이 0이면 평균가는 null이고 총원가는 0이어야 합니다.");
            }
        } else {
            averagePrice = positivePrice(averagePrice, "현재 평단가");
            if (totalCost.signum() <= 0) {
                throw new IllegalArgumentException("총수량이 양수이면 총원가도 양수여야 합니다.");
            }
        }
    }
}
