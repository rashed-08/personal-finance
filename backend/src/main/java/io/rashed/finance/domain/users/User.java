package io.rashed.finance.domain.users;

import io.rashed.finance.common.enums.AuthProvider;
import io.rashed.finance.common.enums.UserRole;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Objects;

@Getter
@ToString(exclude = "passwordHash")
@EqualsAndHashCode(of = "id")
public final class User {

    private final UserId id;

    private final String email;

    /**
     * BCrypt hash of the password.
     *
     * Null for users that only authenticate through an external
     * provider (e.g. Google) and have never set a local password.
     */
    private final String passwordHash;

    private final String name;

    private final UserRole role;

    private final AuthProvider provider;

    /**
     * Subject identifier at the external provider (Google {@code sub}).
     * Null for LOCAL users.
     */
    private final String providerSubject;

    private final boolean emailVerified;

    private final LocalDateTime createdAt;

    private final LocalDateTime updatedAt;

    public User(UserId id, String email, String passwordHash, String name, UserRole role, AuthProvider provider, String providerSubject, boolean emailVerified, LocalDateTime createdAt, LocalDateTime updatedAt) {

        this.id = Objects.requireNonNull(id);
        validateEmail(email);
        this.email = normalizeEmail(email);
        this.passwordHash = passwordHash;
        validateName(name);
        this.name = name.trim();
        this.role = Objects.requireNonNull(role);
        this.provider = Objects.requireNonNull(provider);
        this.providerSubject = providerSubject;
        this.emailVerified = emailVerified;

        if (provider == AuthProvider.LOCAL && (passwordHash == null || passwordHash.isBlank())) {
            throw new IllegalArgumentException("A local user must have a password.");
        }

        this.createdAt = Objects.requireNonNull(createdAt);
        this.updatedAt = Objects.requireNonNull(updatedAt);
    }

    // -------------------------------------------------------------------------
    // Factory Methods
    // -------------------------------------------------------------------------

    public static User registerLocal(String email, String passwordHash, String name) {

        validateEmail(email);
        validateName(name);

        Objects.requireNonNull(passwordHash, "Password hash cannot be null.");

        LocalDateTime now = LocalDateTime.now();

        return new User(UserId.newId(), email, passwordHash, name, UserRole.OWNER, AuthProvider.LOCAL, null, false, now, now);
    }

    public static User registerWithGoogle(String email, String name, String providerSubject, boolean emailVerified) {

        validateEmail(email);
        validateName(name);

        Objects.requireNonNull(providerSubject, "Provider subject cannot be null.");

        LocalDateTime now = LocalDateTime.now();

        return new User(UserId.newId(), email, null, name, UserRole.OWNER, AuthProvider.GOOGLE, providerSubject, emailVerified, now, now);
    }

    // -------------------------------------------------------------------------
    // Validation
    // -------------------------------------------------------------------------

    private static void validateEmail(String email) {

        Objects.requireNonNull(email, "Email cannot be null.");

        if (email.isBlank()) {
            throw new IllegalArgumentException("Email cannot be empty.");
        }

        if (!email.contains("@")) {
            throw new IllegalArgumentException("Email is not valid.");
        }
    }

    private static void validateName(String name) {

        Objects.requireNonNull(name, "Name cannot be null.");

        if (name.isBlank()) {
            throw new IllegalArgumentException("Name cannot be empty.");
        }
    }

    private static String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    // -------------------------------------------------------------------------
    // Business Methods
    // -------------------------------------------------------------------------

    public boolean hasLocalPassword() {
        return passwordHash != null;
    }

    public User changePassword(String newPasswordHash) {

        Objects.requireNonNull(newPasswordHash, "Password hash cannot be null.");

        return new User(id, email, newPasswordHash, name, role, provider, providerSubject, emailVerified, createdAt, LocalDateTime.now());
    }

    public User verifyEmail() {

        if (emailVerified) {
            return this;
        }

        return new User(id, email, passwordHash, name, role, provider, providerSubject, true, createdAt, LocalDateTime.now());
    }

    public User rename(String newName) {

        validateName(newName);

        return new User(id, email, passwordHash, newName.trim(), role, provider, providerSubject, emailVerified, createdAt, LocalDateTime.now());
    }

    public User linkGoogle(String subject) {

        Objects.requireNonNull(subject, "Provider subject cannot be null.");

        return new User(id, email, passwordHash, name, role, AuthProvider.GOOGLE, subject, true, createdAt, LocalDateTime.now());
    }
}
