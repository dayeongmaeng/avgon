package com.avgon.alert.domain;

import static com.avgon.common.domain.DecimalPolicy.positivePrice;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;

public record AlertOccurrence(
        long occurrenceNo,
        BigDecimal thresholdAveragePrice,
        BigDecimal observedPrice,
        Instant priceObservedAt,
        Instant triggeredAt
) {

    public AlertOccurrence {
        if (occurrenceNo <= 0) {
            throw new IllegalArgumentException("알림 발생 순번은 0보다 커야 합니다.");
        }
        thresholdAveragePrice = positivePrice(thresholdAveragePrice, "알림 기준 평단가");
        observedPrice = positivePrice(observedPrice, "관측 가격");
        priceObservedAt = Objects.requireNonNull(priceObservedAt, "가격 관측 시각은 필수입니다.");
        triggeredAt = Objects.requireNonNull(triggeredAt, "알림 발생 시각은 필수입니다.");
    }
}
