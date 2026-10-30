package com.andrew.hdss.configs;

import com.andrew.hdss.exceptions.*;
import com.andrew.hdss.utils.ApiError;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(UserNotVerifiedException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleUserNotVerified(
            UserNotVerifiedException exception,
            HttpServletRequest request
    ) {
        return error(
                "Unverified User",
                HttpStatus.BAD_REQUEST,
                exception.getMessage(),
                request
        );
    }

    @ExceptionHandler(UserDeletedException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleUserDeleted(
            UserDeletedException exception,
            HttpServletRequest request
    ) {
        return error(
                "Deleted User",
                HttpStatus.BAD_REQUEST,
                exception.getMessage(),
                request
        );
    }

    @ExceptionHandler(ResourceAlreadyExistsException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleResourceAlreadyExists(
            ResourceAlreadyExistsException exception,
            HttpServletRequest request
    ) {
        return error(
                "Duplicate Resource",
                HttpStatus.BAD_REQUEST,
                exception.getMessage(),
                request
        );
    }

    @ExceptionHandler({
            IllegalArgumentException.class,
            IllegalStateException.class
    })
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleIllegalArguments(
            Exception exception,
            HttpServletRequest request
    ) {
        return error(
                "Malformed Parameters",
                HttpStatus.BAD_REQUEST,
                exception.getMessage(),
                request
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleEmptyRequiredFields(
            MethodArgumentNotValidException exception,
            HttpServletRequest request
    ) {
        String message = exception.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(f -> f.getField() + ": " + f.getDefaultMessage())
                .findFirst()
                .orElse("Missing Required Parameters");

        return error(
                "Validation Error",
                HttpStatus.BAD_REQUEST,
                message,
                request
        );
    }

    @ExceptionHandler(EntityNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiError handleEntityNotFound(
            EntityNotFoundException exception,
            HttpServletRequest request
    ) {
        return error(
                "Entity Not Found",
                HttpStatus.NOT_FOUND,
                exception.getMessage(),
                request
        );
    }

    @ExceptionHandler(RoundNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiError handleRoundNotFound(
            RoundNotFoundException exception,
            HttpServletRequest request
    ) {
        return error(
                "Round Not Found",
                HttpStatus.NOT_FOUND,
                exception.getMessage(),
                request
        );
    }

    @ExceptionHandler({
            BadCredentialsException.class,
            UsernameNotFoundException.class
    })
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleWrongPassword(
            Exception exception,
            HttpServletRequest request
    ) {
        return error(
                "Bad Credentials",
                HttpStatus.BAD_REQUEST,
                "Wrong username or password",
                request
        );
    }

    @ExceptionHandler(FormLockedException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleEditedFormManipulation(
            FormLockedException exception,
            HttpServletRequest request
    ) {
        return error(
                "Already Edited Form",
                HttpStatus.BAD_REQUEST,
                exception.getMessage(),
                request
        );
    }

    @ExceptionHandler(RoundCreationInProgressException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError handleRoundCreationInProgress(
            RoundCreationInProgressException exception,
            HttpServletRequest request
    ) {
        return error(
                "Round Creation In Progress",
                HttpStatus.CONFLICT,
                exception.getMessage(),
                request
        );
    }

    @ExceptionHandler(RoundCreationUnavailableException.class)
    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    public ApiError handleRoundCreationUnavailable(
            RoundCreationUnavailableException exception,
            HttpServletRequest request
    ) {
        log.error("Round creation could not be started", exception);

        return error(
                "Round Creation Unavailable",
                HttpStatus.SERVICE_UNAVAILABLE,
                exception.getMessage(),
                request
        );
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiError handleGeneric(
            Exception exception,
            HttpServletRequest request
    ) {
        log.error("Unexpected error", exception);

        return error(
                "Internal Server Error",
                HttpStatus.INTERNAL_SERVER_ERROR,
                "An unexpected error occurred",
                request
        );
    }

    private ApiError error(
            String title,
            HttpStatus status,
            String detail,
            HttpServletRequest request
    ) {
        return new ApiError(
                title,
                status.value(),
                detail,
                request.getRequestURI(),
                Instant.now()
        );
    }
}