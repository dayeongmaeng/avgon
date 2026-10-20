package com.avgon.alert.domain;

import static org.assertj.core.api.Assertions.assertThat;

import com.avgon.portfolio.domain.UserStockPosition;
import com.avgon.portfolio.domain.UserStockPositionTest;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class AveragePriceAlertTest {

    private static final Instant BASE_TIME = Instant.parse("2026-10-20T00:00:00Z");

    @Test
    void activationAtOrAboveThresholdLatchesWithoutImmediateNotification() {
        AveragePriceAlert alert = alert();
        alert.enable(BASE_TIME);

        Optional<AlertOccurrence> occurrence = evaluate(alert, "100", "110", 1);

        assertThat(occurrence).isEmpty();
        assertThat(alert.getEvaluationState()).isEqualTo(AlertEvaluationState.LATCHED_AT_OR_ABOVE);
        assertThat(alert.getOccurrenceNo()).isZero();
    }

    @Test
    void equalityTriggersOnceWhenAlertWasArmedBelow() {
        AveragePriceAlert alert = alert();
        alert.enable(BASE_TIME);
        evaluate(alert, "100", "90", 1);

        Optional<AlertOccurrence> first = evaluate(alert, "100", "100", 2);
        Optional<AlertOccurrence> repeated = evaluate(alert, "100", "120", 3);

        assertThat(first).isPresent();
        assertThat(first.orElseThrow().occurrenceNo()).isEqualTo(1);
        assertThat(first.orElseThrow().observedPrice()).isEqualByComparingTo("100");
        assertThat(repeated).isEmpty();
        assertThat(alert.getOccurrenceNo()).isEqualTo(1);
    }

    @Test
    void fallingBelowRearmsAndAllowsASecondReachEvent() {
        AveragePriceAlert alert = alert();
        alert.enable(BASE_TIME);
        evaluate(alert, "100", "90", 1);
        evaluate(alert, "100", "101", 2);
        evaluate(alert, "100", "99", 3);

        Optional<AlertOccurrence> second = evaluate(alert, "100", "100", 4);

        assertThat(second).isPresent();
        assertThat(second.orElseThrow().occurrenceNo()).isEqualTo(2);
    }

    @Test
    void thresholdChangeReturnsEnabledAlertToPendingPolicy() {
        AveragePriceAlert alert = alert();
        alert.enable(BASE_TIME);
        evaluate(alert, "100", "90", 1);

        alert.onThresholdChanged(BASE_TIME.plusSeconds(2));
        Optional<AlertOccurrence> occurrence = evaluate(alert, "80", "90", 3);

        assertThat(occurrence).isEmpty();
        assertThat(alert.getEvaluationState()).isEqualTo(AlertEvaluationState.LATCHED_AT_OR_ABOVE);
    }

    private static AveragePriceAlert alert() {
        UserStockPosition position = UserStockPositionTest.position("100", "10");
        return position.getAlert();
    }

    private static Optional<AlertOccurrence> evaluate(
            AveragePriceAlert alert,
            String threshold,
            String observed,
            long seconds
    ) {
        Instant at = BASE_TIME.plusSeconds(seconds);
        return alert.evaluate(new BigDecimal(threshold), new BigDecimal(observed), at, at);
    }
}
