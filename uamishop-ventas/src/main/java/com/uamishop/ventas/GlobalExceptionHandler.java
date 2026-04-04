package com.uamishop.ventas;

import com.uamishop.ventas.dto.ApiError;
import com.uamishop.ventas.shared.exception.BusinessRuleException;
import com.uamishop.ventas.shared.exception.RecursoNoEncontradoException;
import com.uamishop.ventas.shared.exception.ServicioNoDisponibleException;
import com.uamishop.ventas.shared.exception.StockInsuficienteException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(RecursoNoEncontradoException.class)
    public ResponseEntity<ApiError> handleResourceNotFound(
            RecursoNoEncontradoException ex, WebRequest request) {
        log.warn("Recurso no encontrado: {}", ex.getMessage());
        ApiError apiError = new ApiError(ex.getMessage(), HttpStatus.NOT_FOUND.value(), "Not Found", getPath(request));
        return new ResponseEntity<>(apiError, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(ServicioNoDisponibleException.class)
    public ResponseEntity<ApiError> handleServicioNoDisponible(
            ServicioNoDisponibleException ex, WebRequest request) {
        log.warn("Servicio no disponible: {}", ex.getMessage());
        ApiError apiError = new ApiError(ex.getMessage(), HttpStatus.SERVICE_UNAVAILABLE.value(), "Service Unavailable", getPath(request));
        return new ResponseEntity<>(apiError, HttpStatus.SERVICE_UNAVAILABLE);
    }

    @ExceptionHandler({BusinessRuleException.class, StockInsuficienteException.class, IllegalStateException.class})
    public ResponseEntity<ApiError> handleBusinessRuleException(
            RuntimeException ex, WebRequest request) {
        log.warn("Regla de negocio: {}", ex.getMessage());
        ApiError apiError = new ApiError(ex.getMessage(), HttpStatus.UNPROCESSABLE_ENTITY.value(), "Unprocessable Entity", getPath(request));
        return new ResponseEntity<>(apiError, HttpStatus.UNPROCESSABLE_ENTITY);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiError> handleIllegalArgument(
            IllegalArgumentException ex, WebRequest request) {
        log.warn("Argumento invalido: {}", ex.getMessage());
        ApiError apiError = new ApiError(ex.getMessage(), HttpStatus.BAD_REQUEST.value(), "Bad Request", getPath(request));
        return new ResponseEntity<>(apiError, HttpStatus.BAD_REQUEST);
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex, HttpHeaders headers,
            HttpStatusCode status, WebRequest request) {
        String errors = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));
        ApiError apiError = new ApiError(errors, HttpStatus.BAD_REQUEST.value(), "Validation Error", getPath(request));
        return new ResponseEntity<>(apiError, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleGenericException(Exception ex, WebRequest request) {
        log.error("Error interno del servidor", ex);
        ApiError apiError = new ApiError("Ocurrio un error inesperado", HttpStatus.INTERNAL_SERVER_ERROR.value(), "Internal Server Error", getPath(request));
        return new ResponseEntity<>(apiError, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private String getPath(WebRequest request) {
        if (request instanceof ServletWebRequest swr) {
            return swr.getRequest().getRequestURI();
        }
        return null;
    }
}
