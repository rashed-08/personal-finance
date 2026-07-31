package io.rashed.finance.application.auth;

public record RegisterUserCommand(String email, String password, String name) {

}
