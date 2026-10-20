package com.avgon.portfolio.domain;

import static com.avgon.common.domain.DecimalPolicy.nonNegativePrice;
import static com.avgon.common.domain.DecimalPolicy.nonNegativeQuantity;

import java.math.BigDecimal;

public record PositionBaselineAmount(BigDecimal averagePrice, BigDecimal quantity) {

    public PositionBaselineAmount {
        averagePrice = nonNegativePrice(averagePrice, "기준 평단가");
        quantity = nonNegativeQuantity(quantity, "기준 수량");
        if ((quantity.signum() == 0) != (averagePrice.signum() == 0)) {
            throw new IllegalArgumentException("기준 평단가와 기준 수량은 함께 0이거나 함께 양수여야 합니다.");
        }
    }
}
