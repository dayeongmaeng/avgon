package com.avgon.alert.domain;

import com.avgon.instrument.domain.Instrument;
import com.avgon.portfolio.domain.UserStockPosition;
import com.avgon.user.domain.AppUser;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(
        name = "average_price_alert_event",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_alert_event_alert_occurrence",
                columnNames = {"alert_id", "occurrence_no"}
        )
)
public class AveragePriceAlertEvent {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, foreignKey = @ForeignKey(name = "fk_alert_event_user"))
    private AppUser user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "position_id", foreignKey = @ForeignKey(name = "fk_alert_event_position"))
    private UserStockPosition position;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "alert_id", foreignKey = @ForeignKey(name = "fk_alert_event_alert"))
    private AveragePriceAlert alert;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "instrument_id", nullable = false, foreignKey = @ForeignKey(name = "fk_alert_event_instrument"))
    private Instrument instrument;

    @Column(name = "occurrence_no", nullable = false)
    private long occurrenceNo;

    @Column(name = "threshold_average_price", nullable = false, precision = 24, scale = 8)
    private BigDecimal thresholdAveragePrice;

    @Column(name = "observed_price", nullable = false, precision = 24, scale = 8)
    private BigDecimal observedPrice;

    @Column(name = "price_observed_at", nullable = false)
    private Instant priceObservedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected AveragePriceAlertEvent() {
    }

    public AveragePriceAlertEvent(UUID id, UserStockPosition position, AlertOccurrence occurrence) {
        this.id = Objects.requireNonNull(id, "알림 이벤트 ID는 필수입니다.");
        this.position = Objects.requireNonNull(position, "보유 종목은 필수입니다.");
        this.user = position.getUser();
        this.instrument = position.getInstrument();
        this.alert = position.getAlert();
        AlertOccurrence validated = Objects.requireNonNull(occurrence, "알림 발생 정보는 필수입니다.");
        this.occurrenceNo = validated.occurrenceNo();
        this.thresholdAveragePrice = validated.thresholdAveragePrice();
        this.observedPrice = validated.observedPrice();
        this.priceObservedAt = validated.priceObservedAt();
        this.createdAt = validated.triggeredAt();
    }

    @PrePersist
    void initializeCreatedAt() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }

    public UUID getId() {
        return id;
    }

    public AppUser getUser() {
        return user;
    }

    public long getOccurrenceNo() {
        return occurrenceNo;
    }

    public BigDecimal getThresholdAveragePrice() {
        return thresholdAveragePrice;
    }

    public BigDecimal getObservedPrice() {
        return observedPrice;
    }
}
