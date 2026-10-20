package com.avgon.notification.domain;

import com.avgon.alert.domain.AveragePriceAlertEvent;
import com.avgon.common.persistence.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Version;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(
        name = "notification_delivery",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_notification_delivery_event_subscription",
                columnNames = {"event_id", "subscription_id"}
        )
)
public class NotificationDelivery extends BaseTimeEntity {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "event_id", nullable = false, foreignKey = @ForeignKey(name = "fk_notification_delivery_event"))
    private AveragePriceAlertEvent event;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "subscription_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_notification_delivery_subscription")
    )
    private PushSubscription subscription;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 24)
    private DeliveryStatus status = DeliveryStatus.PENDING;

    @Column(name = "attempt_count", nullable = false)
    private int attemptCount;

    @Column(name = "next_attempt_at")
    private Instant nextAttemptAt;

    @Column(name = "claimed_by", length = 100)
    private String claimedBy;

    @Column(name = "claim_until")
    private Instant claimUntil;

    @Column(name = "last_attempt_at")
    private Instant lastAttemptAt;

    @Column(name = "accepted_at")
    private Instant acceptedAt;

    @Column(name = "provider_message_id", length = 255)
    private String providerMessageId;

    @Column(name = "last_error_code", length = 100)
    private String lastErrorCode;

    @Column(name = "last_error_message", length = 1000)
    private String lastErrorMessage;

    @Version
    @Column(nullable = false)
    private Long version;

    protected NotificationDelivery() {
    }

    public NotificationDelivery(
            UUID id,
            AveragePriceAlertEvent event,
            PushSubscription subscription,
            Instant nextAttemptAt
    ) {
        this.id = Objects.requireNonNull(id, "알림 발송 ID는 필수입니다.");
        this.event = Objects.requireNonNull(event, "알림 이벤트는 필수입니다.");
        this.subscription = Objects.requireNonNull(subscription, "푸시 구독은 필수입니다.");
        if (!eventOwnerMatchesSubscription(event, subscription)) {
            throw new IllegalArgumentException("알림 이벤트와 푸시 구독의 사용자가 일치해야 합니다.");
        }
        this.nextAttemptAt = Objects.requireNonNull(nextAttemptAt, "최초 발송 예정 시각은 필수입니다.");
    }

    public void claim(String workerId, Instant attemptedAt, Instant claimUntil) {
        if (status != DeliveryStatus.PENDING && status != DeliveryStatus.RETRYABLE_FAILURE) {
            throw new IllegalStateException("대기 또는 재시도 상태의 발송만 claim할 수 있습니다.");
        }
        String validatedWorkerId = requireText(workerId, "worker ID");
        Instant validatedAttemptedAt = Objects.requireNonNull(attemptedAt, "발송 시도 시각은 필수입니다.");
        Instant validatedClaimUntil = Objects.requireNonNull(claimUntil, "claim 만료 시각은 필수입니다.");
        if (!validatedClaimUntil.isAfter(validatedAttemptedAt)) {
            throw new IllegalArgumentException("claim 만료 시각은 발송 시도 시각보다 뒤여야 합니다.");
        }

        status = DeliveryStatus.PROCESSING;
        claimedBy = validatedWorkerId;
        this.claimUntil = validatedClaimUntil;
        lastAttemptAt = validatedAttemptedAt;
        attemptCount = Math.addExact(attemptCount, 1);
    }

    public void accept(String providerMessageId, Instant acceptedAt) {
        requireProcessing();
        status = DeliveryStatus.ACCEPTED;
        this.providerMessageId = blankToNull(providerMessageId);
        this.acceptedAt = Objects.requireNonNull(acceptedAt, "공급자 수락 시각은 필수입니다.");
        clearClaimAndError();
        nextAttemptAt = null;
    }

    public void retryableFailure(
            String errorCode,
            String errorMessage,
            Instant nextAttemptAt
    ) {
        requireProcessing();
        status = DeliveryStatus.RETRYABLE_FAILURE;
        lastErrorCode = requireText(errorCode, "오류 코드");
        lastErrorMessage = blankToNull(errorMessage);
        this.nextAttemptAt = Objects.requireNonNull(nextAttemptAt, "재시도 시각은 필수입니다.");
        clearClaim();
    }

    public void permanentFailure(String errorCode, String errorMessage) {
        requireProcessing();
        status = DeliveryStatus.PERMANENT_FAILURE;
        lastErrorCode = requireText(errorCode, "오류 코드");
        lastErrorMessage = blankToNull(errorMessage);
        nextAttemptAt = null;
        clearClaim();
    }

    private void requireProcessing() {
        if (status != DeliveryStatus.PROCESSING) {
            throw new IllegalStateException("처리 중인 발송만 결과를 기록할 수 있습니다.");
        }
    }

    private void clearClaimAndError() {
        clearClaim();
        lastErrorCode = null;
        lastErrorMessage = null;
    }

    private void clearClaim() {
        claimedBy = null;
        claimUntil = null;
    }

    private static boolean eventOwnerMatchesSubscription(
            AveragePriceAlertEvent event,
            PushSubscription subscription
    ) {
        return event.getUser().getId().equals(subscription.getUser().getId());
    }

    private static String requireText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + "은(는) 필수입니다.");
        }
        return value.trim();
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    public DeliveryStatus getStatus() {
        return status;
    }

    public int getAttemptCount() {
        return attemptCount;
    }

    public Instant getNextAttemptAt() {
        return nextAttemptAt;
    }
}
