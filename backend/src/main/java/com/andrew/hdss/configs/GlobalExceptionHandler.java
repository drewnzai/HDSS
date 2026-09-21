package com.andrew.hdss.configs;

import com.andrew.hdss.exceptions.*;
import com.andrew.hdss.utils.ApiError;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
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
    public ApiError handleUserNotVerified(UserNotVerifiedException exception, HttpServletRequest request){
        return new ApiError(
                "Unverified User",
                400,
                exception.getMessage(),
                request.getRequestURI(),
                Instant.now()
        );
    }

    @ExceptionHandler(UserDeletedException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleUserDeleted(UserDeletedException exception, HttpServletRequest request){
        return new ApiError(
                "Deleted User",
                400,
                exception.getMessage(),
                request.getRequestURI(),
                Instant.now()
        );
    }

    @ExceptionHandler(ResourceAlreadyExistsException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleResourceAlreadyExists(ResourceAlreadyExistsException exception, HttpServletRequest request){
        return new ApiError(
                "Duplicate Resource",
                400,
                exception.getMessage(),
                request.getRequestURI(),
                Instant.now()
        );
    }

    @ExceptionHandler({
            IllegalArgumentException.class,
            IllegalStateException.class
    })
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleIllegalArguments(Exception exception, HttpServletRequest request){
        return new ApiError(
                "Malformed Parameters",
                400,
                exception.getMessage(),
                request.getRequestURI(),
                Instant.now()
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleEmptyRequiredFields(MethodArgumentNotValidException exception, HttpServletRequest request){
        String message = exception.getBindingResult().getFieldErrors().stream()
                .map(f -> f.getField() + ": " + f.getDefaultMessage())
                .findFirst()
                .orElse("Missing Required Parameters");

        return new ApiError(
                "Validation Error",
                400,
                message,
                request.getRequestURI(),
                Instant.now()
        );
    }

    @ExceptionHandler(EntityNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiError handleEntityNotFound(EntityNotFoundException exception, HttpServletRequest request){
        return new ApiError(
                "Entity not found",
                404,
                exception.getMessage(),
                request.getRequestURI(),
                Instant.now()
        );
    }

    @ExceptionHandler({
            BadCredentialsException.class,
            UsernameNotFoundException.class
    })
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleWrongPassword(Exception exception, HttpServletRequest request){
        return new ApiError(
                "Bad Credentials",
                400,
                "Wrong username or password",
                request.getRequestURI(),
                Instant.now()
        );
    }

    @ExceptionHandler({
            MalformedJwtException.class,
            ExpiredJwtException.class,
            UnsupportedJwtException.class
    })
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleMalformedJwt(Exception e, HttpServletRequest request){
        return new ApiError(
                "Malformed JWT",
                400,
                "Malformed JWT",
                request.getRequestURI(),
                Instant.now()
        );
    }

    @ExceptionHandler(FormLockedException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleEditedFormManipulation(FormLockedException ex, HttpServletRequest req) {
        return new ApiError(
                "Already Edited Form",
                500,
                ex.getMessage(),
                req.getRequestURI(),
                Instant.now()
        );
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiError handleGeneric(Exception ex, HttpServletRequest req) {
        log.error("Unexpected error", ex);
        return new ApiError(
                "Internal Server Error",
                500,
                "An unexpected error occurred",
                req.getRequestURI(),
                Instant.now()
        );
    }
}
