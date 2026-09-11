package io.rashed.finance.domain.users;

/**
 * Which Google capability a stored grant covers.
 *
 * Only Drive needs offline access today. Sign-In verifies an ID token
 * per request and stores nothing, so it is deliberately absent.
 */
public enum GoogleOAuthScope {

    DRIVE

}
