package com.avgon.portfolio.domain;

import com.avgon.alert.domain.AveragePriceAlert;
import com.avgon.common.persistence.BaseTimeEntity;
import com.avgon.instrument.domain.Instrument;
import com.avgon.user.domain.AppUser;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Version;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(
        name = "user_stock_position",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_user_stock_position_user_instrument",
                columnNames = {"user_id", "instrument_id"}
        )
)
public class UserStockPosition extends BaseTimeEntity {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, foreignKey = @ForeignKey(name = "fk_user_stock_position_user"))
    private AppUser user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "instrument_id", nullable = false, foreignKey = @ForeignKey(name = "fk_user_stock_position_instrument"))
    private Instrument instrument;

    @Column(name = "current_average_price", precision = 24, scale = 8)
    private BigDecimal currentAveragePrice;

    @Column(name = "current_quantity", nullable = false, precision = 24, scale = 12)
    private BigDecimal currentQuantity;

    @Column(name = "current_total_cost", nullable = false, precision = 50, scale = 20)
    private BigDecimal currentTotalCost;

    @Version
    @Column(nullable = false)
    private Long version;

    @OneToOne(mappedBy = "position", cascade = CascadeType.ALL, orphanRemoval = true, optional = false)
    private PositionBaseline baseline;

    @OneToMany(mappedBy = "position", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("purchasedOn ASC, id ASC")
    private List<PurchaseRecord> purchases = new ArrayList<>();

    @OneToOne(mappedBy = "position", cascade = CascadeType.ALL, orphanRemoval = true, optional = false)
    private AveragePriceAlert alert;

    protected UserStockPosition() {
    }

    public UserStockPosition(
            UUID id,
            AppUser user,
            Instrument instrument,
            BigDecimal baselineAveragePrice,
            BigDecimal baselineQuantity,
            UUID alertId
    ) {
        this.id = Objects.requireNonNull(id, "보유 종목 ID는 필수입니다.");
        this.user = Objects.requireNonNull(user, "사용자는 필수입니다.");
        this.instrument = Objects.requireNonNull(instrument, "종목은 필수입니다.");
        this.baseline = new PositionBaseline(this, baselineAveragePrice, baselineQuantity);
        this.alert = new AveragePriceAlert(alertId, this);
        apply(PositionCalculator.calculate(baseline.toAmount(), List.of()), Instant.now());
    }

    public PurchaseRecord addPurchase(
            UUID purchaseId,
            BigDecimal price,
            BigDecimal quantity,
            LocalDate purchasedOn,
            Instant changedAt
    ) {
        if (purchases.stream().anyMatch(purchase -> purchase.getId().equals(purchaseId))) {
            throw new IllegalArgumentException("같은 ID의 매수 이력이 이미 존재합니다.");
        }
        PurchaseRecord purchase = new PurchaseRecord(purchaseId, this, price, quantity, purchasedOn);
        purchases.add(purchase);
        recalculate(changedAt);
        return purchase;
    }

    public void updatePurchase(
            UUID purchaseId,
            BigDecimal price,
            BigDecimal quantity,
            LocalDate purchasedOn,
            Instant changedAt
    ) {
        PurchaseRecord purchase = findPurchase(purchaseId);
        purchase.change(price, quantity, purchasedOn);
        recalculate(changedAt);
    }

    public void removePurchase(UUID purchaseId, Instant changedAt) {
        PurchaseRecord purchase = findPurchase(purchaseId);
        purchases.remove(purchase);
        recalculate(changedAt);
    }

    public void changeBaseline(BigDecimal averagePrice, BigDecimal quantity, Instant changedAt) {
        baseline.change(averagePrice, quantity);
        recalculate(changedAt);
    }

    public PositionSummary recalculate(Instant changedAt) {
        List<PurchaseAmount> purchaseAmounts = purchases.stream()
                .map(PurchaseRecord::toAmount)
                .toList();
        PositionSummary summary = PositionCalculator.calculate(baseline.toAmount(), purchaseAmounts);
        apply(summary, Objects.requireNonNull(changedAt, "변경 시각은 필수입니다."));
        return summary;
    }

    private void apply(PositionSummary summary, Instant changedAt) {
        boolean thresholdChanged = !sameValue(currentAveragePrice, summary.averagePrice());
        currentAveragePrice = summary.averagePrice();
        currentTotalCost = summary.totalCost();
        currentQuantity = summary.quantity();
        if (thresholdChanged && alert != null) {
            alert.onThresholdChanged(changedAt);
        }
    }

    private PurchaseRecord findPurchase(UUID purchaseId) {
        return purchases.stream()
                .filter(purchase -> purchase.getId().equals(purchaseId))
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException("매수 이력을 찾을 수 없습니다."));
    }

    private static boolean sameValue(BigDecimal left, BigDecimal right) {
        if (left == null || right == null) {
            return left == right;
        }
        return left.compareTo(right) == 0;
    }

    public UUID getId() {
        return id;
    }

    public AppUser getUser() {
        return user;
    }

    public Instrument getInstrument() {
        return instrument;
    }

    public BigDecimal getCurrentAveragePrice() {
        return currentAveragePrice;
    }

    public BigDecimal getCurrentQuantity() {
        return currentQuantity;
    }

    public BigDecimal getCurrentTotalCost() {
        return currentTotalCost;
    }

    public PositionBaseline getBaseline() {
        return baseline;
    }

    public List<PurchaseRecord> getPurchases() {
        return Collections.unmodifiableList(purchases);
    }

    public AveragePriceAlert getAlert() {
        return alert;
    }
}
