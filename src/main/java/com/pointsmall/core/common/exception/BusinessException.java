package com.pointsmall.core.common.exception;

public class BusinessException extends RuntimeException {

  @java.io.Serial private static final long serialVersionUID = 1L;

  private final CoreErrorCode errorCode;

  public BusinessException(CoreErrorCode errorCode) {
    super(errorCode.getMessage());
    this.errorCode = errorCode;
  }

  public CoreErrorCode getErrorCode() {
    return errorCode;
  }
}
