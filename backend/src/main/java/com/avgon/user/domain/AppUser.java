package com.avgon.user.domain;

import com.avgon.common.persistence.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(
        name = "app_user",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_app_user_auth_identity",
                columnNames = {"auth_issuer", "auth_subject"}
        )
)
public class AppUser extends BaseTimeEntity {

    @Id
    private UUID id;

    @Column(name = "auth_issuer", nullable = false, length = 255)
    private String authIssuer;

    @Column(name = "auth_subject", nullable = false, length = 255)
    private String authSubject;

    protected AppUser() {
    }

    public AppUser(UUID id, String authIssuer, String authSubject) {
        this.id = Objects.requireNonNull(id, "사용자 ID는 필수입니다.");
        this.authIssuer = requireText(authIssuer, "인증 issuer");
        this.authSubject = requireText(authSubject, "인증 subject");
    }

    public UUID getId() {
        return id;
    }

    public String getAuthIssuer() {
        return authIssuer;
    }

    public String getAuthSubject() {
        return authSubject;
    }

    private static String requireText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + "은(는) 필수입니다.");
        }
        return value.trim();
    }
}
