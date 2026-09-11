package io.rashed.finance.infrastructure.persistence.entity;

import io.rashed.finance.domain.users.GoogleOAuthScope;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "google_oauth_tokens")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class GoogleOAuthTokenEntity {

    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(name = "user_id", nullable = false, updatable = false)
    private UUID userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20, updatable = false)
    private GoogleOAuthScope scope;

    @Column(name = "refresh_token", nullable = false, columnDefinition = "text")
    private String refreshToken;

    @Column(name = "granted_scopes", columnDefinition = "text")
    private String grantedScopes;

    @Column(name = "account_email", length = 255)
    private String accountEmail;

    @Column(name = "connected_at", nullable = false)
    private LocalDateTime connectedAt;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    public GoogleOAuthTokenEntity(
            UUID id,
            UUID userId,
            GoogleOAuthScope scope,
            String refreshToken,
            String grantedScopes,
            String accountEmail,
            LocalDateTime connectedAt,
            LocalDateTime createdAt,
            LocalDateTime updatedAt) {

        this.id = id;
        this.userId = userId;
        this.scope = scope;
        this.refreshToken = refreshToken;
        this.grantedScopes = grantedScopes;
        this.accountEmail = accountEmail;
        this.connectedAt = connectedAt;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
}
