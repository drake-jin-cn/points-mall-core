package com.pointsmall.core.common.exception;

import com.pointsmall.core.common.ApiResponse;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

  private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

  @ExceptionHandler(BusinessException.class)
  public ResponseEntity<ApiResponse<Void>> handleBusiness(BusinessException ex) {
    CoreErrorCode ec = ex.getErrorCode();
    String traceId = UUID.randomUUID().toString();
    int status =
        switch (ec) {
          case INVALID_CREDENTIALS, UNAUTHORIZED_CALLER -> 401;
          case ACCOUNT_DISABLED -> 403;
          case VALIDATION_FAILED -> 400;
          default -> 500;
        };
    return ResponseEntity.status(status)
        .body(ApiResponse.error(ec.getCode(), ec.getMessage(), traceId));
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ApiResponse<Void>> handleValidation(MethodArgumentNotValidException ex) {
    String traceId = UUID.randomUUID().toString();
    CoreErrorCode ec = CoreErrorCode.VALIDATION_FAILED;
    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .body(ApiResponse.error(ec.getCode(), ec.getMessage(), traceId));
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ApiResponse<Void>> handleUnexpected(Exception ex) {
    String traceId = UUID.randomUUID().toString();
    log.error("Unexpected internal error [traceId={}]", traceId, ex);
    CoreErrorCode ec = CoreErrorCode.INTERNAL_ERROR;
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(ApiResponse.error(ec.getCode(), ec.getMessage(), traceId));
  }
}
