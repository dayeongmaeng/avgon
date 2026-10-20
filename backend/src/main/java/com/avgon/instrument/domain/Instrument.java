package com.avgon.instrument.domain;

import com.avgon.common.persistence.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(
        name = "instrument",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_instrument_market_symbol",
                columnNames = {"market_id", "symbol"}
        )
)
public class Instrument extends BaseTimeEntity {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "market_id", nullable = false, foreignKey = @ForeignKey(name = "fk_instrument_market"))
    private Market market;

    @Column(nullable = false, length = 64)
    private String symbol;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(nullable = false)
    private boolean active = true;

    protected Instrument() {
    }

    public Instrument(UUID id, Market market, String symbol, String name) {
        this.id = Objects.requireNonNull(id, "종목 ID는 필수입니다.");
        this.market = Objects.requireNonNull(market, "시장은 필수입니다.");
        this.symbol = requireText(symbol, "종목 코드").toUpperCase(Locale.ROOT);
        this.name = requireText(name, "종목명");
    }

    public void rename(String name) {
        this.name = requireText(name, "종목명");
    }

    public void deactivate() {
        active = false;
    }

    public UUID getId() {
        return id;
    }

    public Market getMarket() {
        return market;
    }

    public String getSymbol() {
        return symbol;
    }

    public String getName() {
        return name;
    }

    public boolean isActive() {
        return active;
    }

    private static String requireText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + "은(는) 필수입니다.");
        }
        return value.trim();
    }
}
