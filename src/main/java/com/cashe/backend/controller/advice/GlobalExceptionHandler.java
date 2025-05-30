package com.cashe.backend.controller.advice;

import com.cashe.backend.common.dto.ErrorResponse;
import com.cashe.backend.common.exception.FileStorageException;
import com.cashe.backend.common.exception.OperationNotAllowedException;
import com.cashe.backend.common.exception.ResourceNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

@ControllerAdvice
public class GlobalExceptionHandler {

        private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

        private ResponseEntity<ErrorResponse> buildErrorResponse(HttpStatus status, String message, String path) {
                ErrorResponse errorResponse = new ErrorResponse(
                                LocalDateTime.now(),
                                status.value(),
                                status.getReasonPhrase(),
                                message,
                                path);
                return new ResponseEntity<>(errorResponse, status);
        }

        @ExceptionHandler(ResourceNotFoundException.class)
        public ResponseEntity<ErrorResponse> handleResourceNotFoundException(ResourceNotFoundException ex,
                        HttpServletRequest request) {
                logger.warn("Resource not found: {}", ex.getMessage());
                return buildErrorResponse(HttpStatus.NOT_FOUND, ex.getMessage(), request.getRequestURI());
        }

        @ExceptionHandler(OperationNotAllowedException.class)
        public ResponseEntity<ErrorResponse> handleOperationNotAllowedException(OperationNotAllowedException ex,
                        HttpServletRequest request) {
                // Considerar si algunos casos de OperationNotAllowedException deberían ser
                // CONFLICT (409)
                // Por ejemplo, si la excepción es "Email ya en uso", podría ser
                // HttpStatus.CONFLICT.
                // Esto requeriría que OperationNotAllowedException pueda llevar un código de
                // estado o tener subclases.
                logger.warn("Operation not allowed: {}", ex.getMessage());
                return buildErrorResponse(HttpStatus.BAD_REQUEST, ex.getMessage(), request.getRequestURI());
        }

        @ExceptionHandler(FileStorageException.class)
        public ResponseEntity<ErrorResponse> handleFileStorageException(FileStorageException ex,
                        HttpServletRequest request) {
                logger.error("File storage error: {}", ex.getMessage(), ex);
                // Determinar si es un error del cliente (400) o del servidor (500)
                HttpStatus status = ex.getCause() instanceof java.io.IOException ? HttpStatus.INTERNAL_SERVER_ERROR
                                : HttpStatus.BAD_REQUEST;
                return buildErrorResponse(status, ex.getMessage(), request.getRequestURI());
        }

        @ExceptionHandler(MethodArgumentNotValidException.class)
        public ResponseEntity<ErrorResponse> handleValidationExceptions(MethodArgumentNotValidException ex,
                        HttpServletRequest request) {
                String errors = ex.getBindingResult().getFieldErrors().stream()
                                .map(error -> "'" + error.getField() + "': " + error.getDefaultMessage())
                                .collect(Collectors.joining(", "));
                String message = "Validation failed: " + errors;
                logger.warn("Validation error: {}", message);
                return buildErrorResponse(HttpStatus.BAD_REQUEST, message, request.getRequestURI());
        }

        @ExceptionHandler(IllegalArgumentException.class)
        public ResponseEntity<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException ex,
                        HttpServletRequest request) {
                logger.warn("Illegal argument: {}", ex.getMessage());
                return buildErrorResponse(HttpStatus.BAD_REQUEST, ex.getMessage(), request.getRequestURI());
        }

        @ExceptionHandler(AccessDeniedException.class)
        public ResponseEntity<ErrorResponse> handleAccessDeniedException(AccessDeniedException ex,
                        HttpServletRequest request) {
                logger.warn("Access denied: {} for resource {}", ex.getMessage(), request.getRequestURI());
                return buildErrorResponse(HttpStatus.FORBIDDEN,
                                "Access Denied: You do not have permission to access this resource.",
                                request.getRequestURI());
        }

        @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
        public ResponseEntity<ErrorResponse> handleHttpRequestMethodNotSupported(
                        HttpRequestMethodNotSupportedException ex, HttpServletRequest request) {
                logger.warn("HTTP method not supported: {}", ex.getMessage());
                String message = "Request method '" + ex.getMethod() + "' not supported for this endpoint.";
                return buildErrorResponse(HttpStatus.METHOD_NOT_ALLOWED, message, request.getRequestURI());
        }

        @ExceptionHandler(HttpMessageNotReadableException.class)
        public ResponseEntity<ErrorResponse> handleHttpMessageNotReadable(HttpMessageNotReadableException ex,
                        HttpServletRequest request) {
                logger.warn("HTTP message not readable: {}", ex.getMessage());
                return buildErrorResponse(HttpStatus.BAD_REQUEST, "Malformed JSON request or invalid data format.",
                                request.getRequestURI());
        }

        @ExceptionHandler(MissingServletRequestParameterException.class)
        public ResponseEntity<ErrorResponse> handleMissingServletRequestParameter(
                        MissingServletRequestParameterException ex, HttpServletRequest request) {
                logger.warn("Missing request parameter: {}", ex.getMessage());
                String message = "Required request parameter '" + ex.getParameterName() + "' of type '"
                                + ex.getParameterType() + "' is not present.";
                return buildErrorResponse(HttpStatus.BAD_REQUEST, message, request.getRequestURI());
        }

        @ExceptionHandler(MethodArgumentTypeMismatchException.class)
        public ResponseEntity<ErrorResponse> handleMethodArgumentTypeMismatch(MethodArgumentTypeMismatchException ex,
                        HttpServletRequest request) {
                logger.warn("Method argument type mismatch: {}", ex.getMessage());
                String message = "Parameter '" + ex.getName() + "' has invalid value '" + ex.getValue()
                                + "'. Expected type '" + ex.getRequiredType().getSimpleName() + ".";
                return buildErrorResponse(HttpStatus.BAD_REQUEST, message, request.getRequestURI());
        }

        @ExceptionHandler(DataIntegrityViolationException.class)
        public ResponseEntity<ErrorResponse> handleDataIntegrityViolationException(DataIntegrityViolationException ex,
                        HttpServletRequest request) {
                logger.warn("Data integrity violation: {}", ex.getMessage());
                // Analizar la causa raíz para dar un mensaje más específico si es posible (ej.
                // violación de unicidad)
                String message = "Data integrity violation. This could be due to a duplicate entry or a foreign key constraint.";
                if (ex.getCause() != null && ex.getCause().getMessage() != null) {
                        if (ex.getCause().getMessage().toLowerCase().contains("unique constraint")) {
                                message = "A record with the same unique identifier already exists.";
                        } else if (ex.getCause().getMessage().toLowerCase().contains("foreign key constraint")) {
                                message = "Cannot delete or update this record because it is referenced by other records.";
                        }
                }
                return buildErrorResponse(HttpStatus.CONFLICT, message, request.getRequestURI()); // 409 Conflict es a
                                                                                                  // menudo apropiado
                                                                                                  // aquí
        }

        @ExceptionHandler(BadCredentialsException.class)
        public ResponseEntity<ErrorResponse> handleBadCredentialsException(BadCredentialsException ex,
                        HttpServletRequest request) {
                logger.warn("Bad credentials: {}", ex.getMessage());
                return buildErrorResponse(HttpStatus.UNAUTHORIZED, "Invalid email or password.",
                                request.getRequestURI());
        }

        @ExceptionHandler(UsernameNotFoundException.class)
        public ResponseEntity<ErrorResponse> handleUsernameNotFoundException(UsernameNotFoundException ex,
                        HttpServletRequest request) {
                logger.warn("Username not found during auth attempt: {}", ex.getMessage());
                // Devolver UNAUTHORIZED en lugar de NOT_FOUND para no revelar si el usuario
                // existe o no
                return buildErrorResponse(HttpStatus.UNAUTHORIZED, "Invalid email or password.",
                                request.getRequestURI());
        }

        @ExceptionHandler(Exception.class)
        public ResponseEntity<ErrorResponse> handleGenericException(Exception ex, HttpServletRequest request) {
                logger.error("Unexpected error occurred at {}", request.getRequestURI(), ex);
                return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR,
                                "An unexpected internal server error occurred.", request.getRequestURI());
        }
}