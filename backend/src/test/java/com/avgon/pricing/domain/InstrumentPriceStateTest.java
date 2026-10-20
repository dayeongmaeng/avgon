package com.avgon.pricing.domain;

import static org.assertj.core.api.Assertions.assertThat;

import com.avgon.instrument.domain.Instrument;
import com.avgon.instrument.domain.Market;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class InstrumentPriceStateTest {

    @Test
    void failedFetchDoesNotReplaceTheLastSuccessfulPrice() {
        Instrument instrument = new Instrument(
                UUID.randomUUID(),
                new Market("KRX", "한국거래소"),
                "005930",
                "삼성전자"
        );
        InstrumentPriceState state = new InstrumentPriceState(instrument);
        Instant observedAt = Instant.parse("2026-10-20T00:00:00Z");
        state.recordSuccess(new BigDecimal("73000"), observedAt, "test-provider", observedAt.plusSeconds(1));

        state.recordFailure("UPSTREAM_TIMEOUT", observedAt.plusSeconds(2));

        assertThat(state.getLastPrice()).isEqualByComparingTo("73000");
        assertThat(state.getPriceObservedAt()).isEqualTo(observedAt);
        assertThat(state.getConsecutiveFailures()).isEqualTo(1);
        assertThat(state.getLastErrorCode()).isEqualTo("UPSTREAM_TIMEOUT");
    }
}
