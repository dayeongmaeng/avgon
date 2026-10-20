package com.avgon.portfolio.domain;

import static org.assertj.core.api.Assertions.assertThat;

import com.avgon.instrument.domain.Instrument;
import com.avgon.instrument.domain.Market;
import com.avgon.user.domain.AppUser;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
import org.junit.jupiter.api.Test;

public class UserStockPositionTest {

    @Test
    void recalculatesFromBaselineAndWholeHistoryAfterAddUpdateAndDelete() {
        UserStockPosition position = position("100", "10");
        UUID purchaseId = UUID.randomUUID();
        Instant changedAt = Instant.parse("2026-10-20T00:00:00Z");

        position.addPurchase(
                purchaseId,
                new BigDecimal("80"),
                new BigDecimal("5"),
                LocalDate.parse("2026-10-20"),
                changedAt
        );
        assertSummary(position, "93.33333333", "1400", "15");

        position.updatePurchase(
                purchaseId,
                new BigDecimal("70"),
                new BigDecimal("10"),
                LocalDate.parse("2026-10-20"),
                changedAt.plusSeconds(1)
        );
        assertSummary(position, "85", "1700", "20");

        position.removePurchase(purchaseId, changedAt.plusSeconds(2));
        assertSummary(position, "100", "1000", "10");
    }

    private static void assertSummary(
            UserStockPosition position,
            String averagePrice,
            String totalCost,
            String quantity
    ) {
        assertThat(position.getCurrentAveragePrice()).isEqualByComparingTo(averagePrice);
        assertThat(position.getCurrentTotalCost()).isEqualByComparingTo(totalCost);
        assertThat(position.getCurrentQuantity()).isEqualByComparingTo(quantity);
    }

    public static UserStockPosition position(String averagePrice, String quantity) {
        AppUser user = new AppUser(UUID.randomUUID(), "test", UUID.randomUUID().toString());
        Market market = new Market("KRX", "한국거래소");
        Instrument instrument = new Instrument(UUID.randomUUID(), market, "005930", "삼성전자");
        return new UserStockPosition(
                UUID.randomUUID(),
                user,
                instrument,
                new BigDecimal(averagePrice),
                new BigDecimal(quantity),
                UUID.randomUUID()
        );
    }
}
