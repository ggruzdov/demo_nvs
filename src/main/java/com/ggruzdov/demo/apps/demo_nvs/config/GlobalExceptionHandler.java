package com.ggruzdov.demo.apps.demo_nvs.config;

import com.ggruzdov.demo.apps.demo_nvs.exceptions.InvalidImageException;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;

import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.CompletionException;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // Handling CompletableFuture exceptions
    @ExceptionHandler(CompletionException.class)
    public ResponseEntity<ErrorResponse> handleCompletionException(CompletionException ex) {
        if (ex.getCause() instanceof InvalidImageException cause) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(handleInvalidImageException(cause));
        }

        return handleGenericException((Exception) ex.getCause());
    }

    @ExceptionHandler({
        MethodArgumentNotValidException.class,
        HandlerMethodValidationException.class,
        InvalidImageException.class,
    })
    @ApiResponse(
        responseCode = "400",
        description = "Validation error",
        content = @Content(
            mediaType = MediaType.APPLICATION_JSON_VALUE,
            schema = @Schema(implementation = ErrorResponse.class),
            examples = @ExampleObject(
                    value = """
                        {
                            "error": "Validation failed",
                            "message": "url: must not be blank,title: size must be between 3 and 100"
                        }
                        """
            )
        )
    )
    public ResponseEntity<ErrorResponse> handleBadRequestExceptions(Exception ex) {
        var errorResponse = switch (ex) {
            case InvalidImageException exc -> handleInvalidImageException(exc);
            case MethodArgumentNotValidException exc -> handleMethodArgumentNotValidException(exc);
            case HandlerMethodValidationException exc -> handleHandlerMethodValidationException(exc);
            default -> new ErrorResponse("Bad request", ex.getLocalizedMessage());
        };

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(EntityNotFoundException.class)
    @ApiResponse(
        responseCode = "404",
        description = "Entity not found error",
        content = @Content(
            mediaType = MediaType.APPLICATION_JSON_VALUE,
            schema = @Schema(implementation = ErrorResponse.class),
            examples = @ExampleObject(
                value = """
                    {
                        "error": "Entity not found",
                        "message": "SlideShow with id 123 not found"
                    }
                    """
            )
        )
    )
    public ResponseEntity<ErrorResponse> handleEntityNotFoundException(EntityNotFoundException ex) {
        log.info(ex.getMessage());
        var errorResponse = new ErrorResponse("Entity not found", ex.getLocalizedMessage());
        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(Exception.class)
    @ApiResponse(
        responseCode = "500",
        description = "Internal server error",
        content = @Content(
            mediaType = MediaType.APPLICATION_JSON_VALUE,
            schema = @Schema(implementation = ErrorResponse.class),
            examples = @ExampleObject(
                value = """
                    {
                        "error": "Internal error",
                        "message": "An unexpected error occurred"
                    }
                    """
            )
        )
    )
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
        log.error(ex.getMessage(), ex);
        var errorResponse = new ErrorResponse("Internal error", ex.getLocalizedMessage());
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private ErrorResponse handleInvalidImageException(InvalidImageException ex) {
        log.info(ex.getMessage());
        return new ErrorResponse("Image validation failed", ex.getLocalizedMessage());
    }

    private ErrorResponse handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {
        var details = ex.getBindingResult()
            .getFieldErrors()
            .stream()
            .map(error -> error.getField() + ": " +  error.getDefaultMessage())
            .collect(Collectors.joining(","));
        return new ErrorResponse("Validation failed", details);
    }

    private ErrorResponse handleHandlerMethodValidationException(HandlerMethodValidationException ex) {
        var details = Arrays.stream(Objects.requireNonNull(ex.getDetailMessageArguments()))
            .map(Object::toString)
            .collect(Collectors.joining(","));
        return new ErrorResponse("Validation failed", details);
    }

    @Schema(description = "Standard error response structure")
    public record ErrorResponse(
        @Schema(description = "Error type", example = "Validation failed")
        String error,
        @Schema(description = "Detailed error message", example = "Required field 'title' is missing")
        String message
    ) {}
}