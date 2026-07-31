package io.rashed.finance.common.enums;

/**
 * Application user roles.
 *
 * Version 0.11 assigns OWNER to every user; role-based authorization
 * is future-ready but not yet enforced.
 */
public enum UserRole {
    OWNER,
    ADMIN,
    VIEWER
}
