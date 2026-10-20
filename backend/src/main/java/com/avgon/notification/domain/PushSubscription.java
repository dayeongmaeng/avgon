package com.avgon.notification.domain;

import com.avgon.common.persistence.BaseTimeEntity;
import com.avgon.user.domain.AppUser;
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
        name = "push_subscription",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_push_subscription_provider_endpoint",
                columnNames = {"provider", "endpoint"}
        )
)
public class PushSubscription extends BaseTimeEntity {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, foreignKey = @ForeignKey(name = "fk_push_subscription_user"))
    private AppUser user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PushProvider provider;

    @Column(nullable = false, columnDefinition = "text")
    private String endpoint;

    @Column(name = "web_p256dh_key", columnDefinition = "text")
    private String webP256dhKey;

    @Column(name = "web_auth_secret", columnDefinition = "text")
    private String webAuthSecret;

    @Column(nullable = false)
    private boolean active = true;

    @Column(name = "invalidated_at")
    private Instant invalidatedAt;

    @Column(name = "last_seen_at", nullable = false)
    private Instant lastSeenAt;

    @Version
    @Column(nullable = false)
    private Long version;

    protected PushSubscription() {
    }

    private PushSubscription(
            UUID id,
            AppUser user,
            PushProvider provider,
            String endpoint,
            String webP256dhKey,
            String webAuthSecret,
            Instant seenAt
    ) {
        this.id = Objects.requireNonNull(id, "푸시 구독 ID는 필수입니다.");
        this.user = Objects.requireNonNull(user, "사용자는 필수입니다.");
        this.provider = Objects.requireNonNull(provider, "푸시 공급자는 필수입니다.");
        this.endpoint = requireText(endpoint, "푸시 endpoint");
        this.webP256dhKey = webP256dhKey;
        this.webAuthSecret = webAuthSecret;
        this.lastSeenAt = Objects.requireNonNull(seenAt, "마지막 확인 시각은 필수입니다.");
        validateProviderFields();
    }

    public static PushSubscription expo(UUID id, AppUser user, String token, Instant seenAt) {
        return new PushSubscription(id, user, PushProvider.EXPO, token, null, null, seenAt);
    }

    public static PushSubscription webPush(
            UUID id,
            AppUser user,
            String endpoint,
            String p256dhKey,
            String authSecret,
            Instant seenAt
    ) {
        return new PushSubscription(
                id,
                user,
                PushProvider.WEB_PUSH,
                endpoint,
                requireText(p256dhKey, "Web Push p256dh 키"),
                requireText(authSecret, "Web Push auth 비밀값"),
                seenAt
        );
    }

    public void markSeen(Instant seenAt) {
        lastSeenAt = Objects.requireNonNull(seenAt, "마지막 확인 시각은 필수입니다.");
        active = true;
        invalidatedAt = null;
    }

    public void invalidate(Instant invalidatedAt) {
        active = false;
        this.invalidatedAt = Objects.requireNonNull(invalidatedAt, "무효화 시각은 필수입니다.");
    }

    private void validateProviderFields() {
        if (provider == PushProvider.EXPO && (webP256dhKey != null || webAuthSecret != null)) {
            throw new IllegalArgumentException("Expo 구독에는 Web Push 키를 저장할 수 없습니다.");
        }
        if (provider == PushProvider.WEB_PUSH && (webP256dhKey == null || webAuthSecret == null)) {
            throw new IllegalArgumentException("Web Push 구독에는 p256dh 키와 auth 비밀값이 필요합니다.");
        }
    }

    public UUID getId() {
        return id;
    }

    public AppUser getUser() {
        return user;
    }

    public PushProvider getProvider() {
        return provider;
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
