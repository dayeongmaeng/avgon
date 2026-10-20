package com.avgon.portfolio.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;

class PositionCalculatorTest {

    @Test
    void calculatesPurchaseFromEmptyBaseline() {
        PositionSummary result = PositionCalculator.calculate(
                baseline("0", "0"),
                List.of(purchase("15000", "3"))
        );

        assertThat(result.averagePrice()).isEqualByComparingTo("15000");
        assertThat(result.totalCost()).isEqualByComparingTo("45000");
        assertThat(result.quantity()).isEqualByComparingTo("3");
    }

    @Test
    void combinesBaselineAndManagedPurchasesExactlyOnce() {
        PositionSummary result = PositionCalculator.calculate(
                baseline("10000", "10"),
                List.of(purchase("8000", "5"), purchase("7000", "5"))
        );

        assertThat(result.averagePrice()).isEqualByComparingTo("8750");
        assertThat(result.totalCost()).isEqualByComparingTo("175000");
        assertThat(result.quantity()).isEqualByComparingTo("20");
    }

    @Test
    void representsEmptyPositionWithoutDividingByZero() {
        PositionSummary result = PositionCalculator.calculate(baseline("0", "0"), List.of());

        assertThat(result.averagePrice()).isNull();
        assertThat(result.totalCost()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(result.quantity()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void preservesLargeAndFractionalValuesWithoutFloatingPointDrift() {
        PositionSummary result = PositionCalculator.calculate(
                baseline("999999999999.99", "0.1"),
                List.of(purchase("0.01", "0.2"))
        );

        assertThat(result.averagePrice()).isEqualByComparingTo("333333333333.33666667");
        assertThat(result.totalCost()).isEqualByComparingTo("100000000000.001");
        assertThat(result.quantity()).isEqualByComparingTo("0.3");
    }

    private static PositionBaselineAmount baseline(String averagePrice, String quantity) {
        return new PositionBaselineAmount(new BigDecimal(averagePrice), new BigDecimal(quantity));
    }

    private static PurchaseAmount purchase(String price, String quantity) {
        return new PurchaseAmount(new BigDecimal(price), new BigDecimal(quantity));
    }
}
