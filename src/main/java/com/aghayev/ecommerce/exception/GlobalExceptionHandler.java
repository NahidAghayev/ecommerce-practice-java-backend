package com.aghayev.ecommerce.exception;

import com.aghayev.ecommerce.dto.ApiResponse;
import com.aghayev.ecommerce.dto.ValidationErrorResponseDto;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<ValidationErrorResponseDto>> handleResourceNotFound(
            ResourceNotFoundException exception
    ) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(
                        exception.getMessage(),
                        new ValidationErrorResponseDto(exception.getFieldName(), exception.getMessage())
                ));
    }

    @ExceptionHandler({BadRequestException.class, InsufficientStockException.class})
    public ResponseEntity<ApiResponse<ValidationErrorResponseDto>> handleBadRequest(RuntimeException exception) {
        String fieldName = null;
        if (exception instanceof BadRequestException badRequestException) {
            fieldName = badRequestException.getFieldName();
        } else if (exception instanceof InsufficientStockException insufficientStockException) {
            fieldName = insufficientStockException.getFieldName();
        }

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(
                        exception.getMessage(),
                        new ValidationErrorResponseDto(fieldName, exception.getMessage())
                ));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<List<ValidationErrorResponseDto>>> handleValidation(
            MethodArgumentNotValidException exception
    ) {
        List<ValidationErrorResponseDto> fieldErrors = exception.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(this::toValidationError)
                .toList();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ApiResponse<>(
                        false,
                        fieldErrors,
                        "Request validation failed",
                        LocalDateTime.now()
                ));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleException(Exception exception) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("An unexpected error occurred", null));
    }

    private ValidationErrorResponseDto toValidationError(FieldError fieldError) {
        return new ValidationErrorResponseDto(fieldError.getField(), fieldError.getDefaultMessage());
    }
}
