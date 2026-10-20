package com.avgon.common.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

public final class DecimalPolicy {

    public static final int PRICE_PRECISION = 24;
    public static final int PRICE_SCALE = 8;
    public static final int QUANTITY_PRECISION = 24;
    public static final int QUANTITY_SCALE = 12;
    public static final int COST_PRECISION = 50;
    public static final int COST_SCALE = 20;
    public static final int AVERAGE_PRICE_SCALE = 8;
    public static final RoundingMode AVERAGE_PRICE_ROUNDING = RoundingMode.HALF_UP;

    private DecimalPolicy() {
    }

    public static BigDecimal positivePrice(BigDecimal value, String fieldName) {
        return require(value, fieldName, PRICE_PRECISION, PRICE_SCALE, false);
    }

    public static BigDecimal nonNegativePrice(BigDecimal value, String fieldName) {
        return require(value, fieldName, PRICE_PRECISION, PRICE_SCALE, true);
    }

    public static BigDecimal positiveQuantity(BigDecimal value, String fieldName) {
        return require(value, fieldName, QUANTITY_PRECISION, QUANTITY_SCALE, false);
    }

    public static BigDecimal nonNegativeQuantity(BigDecimal value, String fieldName) {
        return require(value, fieldName, QUANTITY_PRECISION, QUANTITY_SCALE, true);
    }

    public static BigDecimal nonNegativeCost(BigDecimal value, String fieldName) {
        return require(value, fieldName, COST_PRECISION, COST_SCALE, true);
    }

    private static BigDecimal require(
            BigDecimal value,
            String fieldName,
            int precision,
            int scale,
            boolean zeroAllowed
    ) {
        Objects.requireNonNull(value, fieldName + "은(는) 필수입니다.");
        if (zeroAllowed ? value.signum() < 0 : value.signum() <= 0) {
            throw new IllegalArgumentException(fieldName + (zeroAllowed ? "은(는) 0 이상이어야 합니다." : "은(는) 0보다 커야 합니다."));
        }

        try {
            value.setScale(scale, RoundingMode.UNNECESSARY);
        } catch (ArithmeticException exception) {
            throw new IllegalArgumentException(fieldName + "의 소수 자릿수가 허용 범위를 초과합니다.", exception);
        }

        BigDecimal normalized = value.signum() == 0 ? BigDecimal.ZERO : value.stripTrailingZeros();
        int integerDigits = Math.max(0, normalized.precision() - normalized.scale());
        if (integerDigits > precision - scale) {
            throw new IllegalArgumentException(fieldName + "의 정수 자릿수가 허용 범위를 초과합니다.");
        }
        return value;
    }
}
