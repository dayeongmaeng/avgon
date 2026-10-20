package com.avgon.alert.domain;

import static com.avgon.common.domain.DecimalPolicy.positivePrice;

import com.avgon.common.persistence.BaseTimeEntity;
import com.avgon.portfolio.domain.UserStockPosition;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Entity
@Table(name = "average_price_alert")
public class AveragePriceAlert extends BaseTimeEntity {

    @Id
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "position_id",
            nullable = false,
            unique = true,
            foreignKey = @ForeignKey(name = "fk_average_price_alert_position")
    )
    private UserStockPosition position;

    @Column(nullable = false)
    private boolean enabled;

    @Enumerated(EnumType.STRING)
    @Column(name = "evaluation_state", nullable = false, length = 32)
    private AlertEvaluationState evaluationState;

    @Column(name = "last_evaluated_price", precision = 24, scale = 8)
    private BigDecimal lastEvaluatedPrice;

    @Column(name = "last_price_observed_at")
    private Instant lastPriceObservedAt;

    @Column(name = "occurrence_no", nullable = false)
    private long occurrenceNo;

    @Column(name = "last_triggered_at")
    private Instant lastTriggeredAt;

    @Column(name = "state_changed_at", nullable = false)
    private Instant stateChangedAt;

    @Version
    @Column(nullable = false)
    private Long version;

    protected AveragePriceAlert() {
    }

    public AveragePriceAlert(UUID id, UserStockPosition position) {
        this.id = Objects.requireNonNull(id, "알림 ID는 필수입니다.");
        this.position = Objects.requireNonNull(position, "보유 종목은 필수입니다.");
        this.enabled = false;
        this.evaluationState = AlertEvaluationState.DISABLED;
        this.stateChangedAt = Instant.now();
    }

    public void enable(Instant changedAt) {
        enabled = true;
        moveToPending(changedAt);
    }

    public void disable(Instant changedAt) {
        enabled = false;
        evaluationState = AlertEvaluationState.DISABLED;
        clearLastEvaluation();
        stateChangedAt = Objects.requireNonNull(changedAt, "상태 변경 시각은 필수입니다.");
    }

    public void onThresholdChanged(Instant changedAt) {
        if (enabled) {
            moveToPending(changedAt);
        }
    }

    public Optional<AlertOccurrence> evaluate(
            BigDecimal thresholdAveragePrice,
            BigDecimal observedPrice,
            Instant priceObservedAt,
            Instant evaluatedAt
    ) {
        if (!enabled) {
            return Optional.empty();
        }

        BigDecimal threshold = positivePrice(thresholdAveragePrice, "알림 기준 평단가");
        BigDecimal currentPrice = positivePrice(observedPrice, "관측 가격");
        Instant observedAt = Objects.requireNonNull(priceObservedAt, "가격 관측 시각은 필수입니다.");
        Instant decisionAt = Objects.requireNonNull(evaluatedAt, "판정 시각은 필수입니다.");

        if (lastPriceObservedAt != null && observedAt.isBefore(lastPriceObservedAt)) {
            return Optional.empty();
        }

        lastEvaluatedPrice = currentPrice;
        lastPriceObservedAt = observedAt;

        if (evaluationState == AlertEvaluationState.PENDING) {
            changeState(
                    currentPrice.compareTo(threshold) < 0
                            ? AlertEvaluationState.ARMED_BELOW
                            : AlertEvaluationState.LATCHED_AT_OR_ABOVE,
                    decisionAt
            );
            return Optional.empty();
        }

        if (currentPrice.compareTo(threshold) < 0) {
            if (evaluationState != AlertEvaluationState.ARMED_BELOW) {
                changeState(AlertEvaluationState.ARMED_BELOW, decisionAt);
            }
            return Optional.empty();
        }

        if (evaluationState == AlertEvaluationState.ARMED_BELOW) {
            occurrenceNo++;
            lastTriggeredAt = decisionAt;
            changeState(AlertEvaluationState.LATCHED_AT_OR_ABOVE, decisionAt);
            return Optional.of(new AlertOccurrence(
                    occurrenceNo,
                    threshold,
                    currentPrice,
                    observedAt,
                    decisionAt
            ));
        }

        return Optional.empty();
    }

    private void moveToPending(Instant changedAt) {
        evaluationState = AlertEvaluationState.PENDING;
        clearLastEvaluation();
        stateChangedAt = Objects.requireNonNull(changedAt, "상태 변경 시각은 필수입니다.");
    }

    private void clearLastEvaluation() {
        lastEvaluatedPrice = null;
        lastPriceObservedAt = null;
    }

    private void changeState(AlertEvaluationState state, Instant changedAt) {
        evaluationState = state;
        stateChangedAt = changedAt;
    }

    public UUID getId() {
        return id;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public AlertEvaluationState getEvaluationState() {
        return evaluationState;
    }

    public BigDecimal getLastEvaluatedPrice() {
        return lastEvaluatedPrice;
    }

    public Instant getLastPriceObservedAt() {
        return lastPriceObservedAt;
    }

    public long getOccurrenceNo() {
        return occurrenceNo;
    }

    public Instant getLastTriggeredAt() {
        return lastTriggeredAt;
    }
}
