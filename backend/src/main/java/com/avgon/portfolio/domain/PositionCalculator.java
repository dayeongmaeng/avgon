package com.avgon.portfolio.domain;

import static com.avgon.common.domain.DecimalPolicy.AVERAGE_PRICE_ROUNDING;
import static com.avgon.common.domain.DecimalPolicy.AVERAGE_PRICE_SCALE;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Objects;

public final class PositionCalculator {

    private PositionCalculator() {
    }

    public static PositionSummary calculate(
            PositionBaselineAmount baseline,
            Collection<PurchaseAmount> purchases
    ) {
        Objects.requireNonNull(baseline, "기준 보유분은 필수입니다.");
        Objects.requireNonNull(purchases, "매수 이력은 필수입니다.");

        BigDecimal totalQuantity = baseline.quantity();
        BigDecimal totalCost = baseline.averagePrice().multiply(baseline.quantity());

        for (PurchaseAmount purchase : purchases) {
            Objects.requireNonNull(purchase, "매수 이력에는 null을 포함할 수 없습니다.");
            totalQuantity = totalQuantity.add(purchase.quantity());
            totalCost = totalCost.add(purchase.price().multiply(purchase.quantity()));
        }

        if (totalQuantity.signum() == 0) {
            return new PositionSummary(null, BigDecimal.ZERO, BigDecimal.ZERO);
        }

        BigDecimal averagePrice = totalCost.divide(
                totalQuantity,
                AVERAGE_PRICE_SCALE,
                AVERAGE_PRICE_ROUNDING
        );
        return new PositionSummary(averagePrice, totalCost, totalQuantity);
    }
}
