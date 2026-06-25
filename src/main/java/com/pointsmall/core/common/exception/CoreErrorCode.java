package com.pointsmall.core.common.exception;

public enum CoreErrorCode {
  INVALID_CREDENTIALS("core-1001", "Invalid credentials"),
  ACCOUNT_DISABLED("core-1002", "Account disabled"),
  UNAUTHORIZED_CALLER("core-1003", "Missing or invalid API key"),
  VALIDATION_FAILED("core-1010", "Request validation failed"),
  INTERNAL_ERROR("core-1099", "Unexpected internal error");

  private final String code;
  private final String message;

  CoreErrorCode(String code, String message) {
    this.code = code;
    this.message = message;
  }

  public String getCode() {
    return code;
  }

  public String getMessage() {
    return message;
  }
}
