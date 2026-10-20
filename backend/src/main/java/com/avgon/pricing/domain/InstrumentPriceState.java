package com.avgon.pricing.domain;

import static com.avgon.common.domain.DecimalPolicy.positivePrice;

import com.avgon.common.persistence.BaseTimeEntity;
import com.avgon.instrument.domain.Instrument;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "instrument_price_state")
public class InstrumentPriceState extends BaseTimeEntity {

    @Id
    @Column(name = "instrument_id")
    private UUID instrumentId;

    @MapsId
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "instrument_id", foreignKey = @ForeignKey(name = "fk_instrument_price_state_instrument"))
    private Instrument instrument;

    @Column(name = "last_price", precision = 24, scale = 8)
    private BigDecimal lastPrice;

    @Column(name = "price_observed_at")
    private Instant priceObservedAt;

    @Column(name = "last_source_code", length = 50)
    private String lastSourceCode;

    @Column(name = "last_fetch_attempt_at")
    private Instant lastFetchAttemptAt;

    @Column(name = "last_fetch_succeeded_at")
    private Instant lastFetchSucceededAt;

    @Column(name = "consecutive_failures", nullable = false)
    private int consecutiveFailures;

    @Column(name = "last_error_code", length = 100)
    private String lastErrorCode;

    @Version
    @Column(nullable = false)
    private Long version;

    protected InstrumentPriceState() {
    }

    public InstrumentPriceState(Instrument instrument) {
        this.instrument = Objects.requireNonNull(instrument, "종목은 필수입니다.");
    }

    public boolean recordSuccess(
            BigDecimal price,
            Instant observedAt,
            String sourceCode,
            Instant fetchedAt
    ) {
        BigDecimal validatedPrice = positivePrice(price, "관측 가격");
        Instant validatedObservedAt = Objects.requireNonNull(observedAt, "가격 관측 시각은 필수입니다.");
        Instant validatedFetchedAt = Objects.requireNonNull(fetchedAt, "조회 시각은 필수입니다.");
        String validatedSource = requireText(sourceCode, "시세 공급자 코드");

        lastFetchAttemptAt = validatedFetchedAt;
        if (priceObservedAt != null && validatedObservedAt.isBefore(priceObservedAt)) {
            return false;
        }

        lastPrice = validatedPrice;
        priceObservedAt = validatedObservedAt;
        lastSourceCode = validatedSource;
        lastFetchSucceededAt = validatedFetchedAt;
        consecutiveFailures = 0;
        lastErrorCode = null;
        return true;
    }

    public void recordFailure(String errorCode, Instant attemptedAt) {
        lastFetchAttemptAt = Objects.requireNonNull(attemptedAt, "조회 시각은 필수입니다.");
        lastErrorCode = requireText(errorCode, "오류 코드");
        consecutiveFailures = Math.addExact(consecutiveFailures, 1);
    }

    public BigDecimal getLastPrice() {
        return lastPrice;
    }

    public Instant getPriceObservedAt() {
        return priceObservedAt;
    }

    public int getConsecutiveFailures() {
        return consecutiveFailures;
    }

    public String getLastErrorCode() {
        return lastErrorCode;
    }

    private static String requireText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + "은(는) 필수입니다.");
        }
        return value.trim();
    }
}
