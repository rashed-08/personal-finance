package io.rashed.finance.api.advice;

import io.rashed.finance.common.exception.InvalidCredentialsException;
import io.rashed.finance.common.exception.InvalidRefreshTokenException;
import io.rashed.finance.common.exception.ResourceNotFoundException;
import io.rashed.finance.common.exception.TransactionValidationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ProblemDetail handleNotFound(ResourceNotFoundException ex) {

        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.NOT_FOUND);

        problem.setTitle("Resource Not Found");
        problem.setDetail(ex.getMessage());
        problem.setProperty("timestamp", Instant.now());

        return problem;
    }

    @ExceptionHandler({IllegalArgumentException.class, TransactionValidationException.class})
    public ProblemDetail handleIllegalArgument(RuntimeException ex) {

        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);

        problem.setTitle("Validation Failed");
        problem.setDetail(ex.getMessage());
        problem.setProperty("timestamp", Instant.now());

        return problem;
    }

    @ExceptionHandler(IllegalStateException.class)
    public ProblemDetail handleIllegalState(IllegalStateException ex) {

        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.CONFLICT);

        problem.setTitle("Invalid State Transition");
        problem.setDetail(ex.getMessage());
        problem.setProperty("timestamp", Instant.now());

        return problem;
    }

    @ExceptionHandler({InvalidCredentialsException.class, InvalidRefreshTokenException.class})
    public ProblemDetail handleAuthenticationFailure(RuntimeException ex) {

        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.UNAUTHORIZED);

        problem.setTitle("Authentication Failed");
        problem.setDetail(ex.getMessage());
        problem.setProperty("timestamp", Instant.now());

        return problem;
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ProblemDetail handleAccessDenied(AccessDeniedException ex) {

        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.FORBIDDEN);

        problem.setTitle("Access Denied");
        problem.setDetail("You do not have permission to access this resource.");
        problem.setProperty("timestamp", Instant.now());

        return problem;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidation(MethodArgumentNotValidException ex) {

        String message = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .findFirst()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .orElse("Validation failed.");

        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);

        problem.setTitle("Request Validation Failed");
        problem.setDetail(message);
        problem.setProperty("timestamp", Instant.now());

        return problem;
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleUnknown(Exception ex) {

        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.INTERNAL_SERVER_ERROR);

        problem.setTitle("Internal Server Error");
        problem.setDetail(ex.getMessage());
        problem.setProperty("timestamp", Instant.now());

        return problem;
    }
}