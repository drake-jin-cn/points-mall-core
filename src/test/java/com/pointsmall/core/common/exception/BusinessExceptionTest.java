package com.pointsmall.core.common.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.ObjectStreamClass;
import org.junit.jupiter.api.Test;

class BusinessExceptionTest {

  @Test
  void shouldDefineStableSerialVersionUidAndExposeErrorCode() {
    BusinessException exception = new BusinessException(CoreErrorCode.INVALID_CREDENTIALS);

    assertEquals(CoreErrorCode.INVALID_CREDENTIALS, exception.getErrorCode());
    assertEquals(CoreErrorCode.INVALID_CREDENTIALS.getMessage(), exception.getMessage());
    assertEquals(1L, ObjectStreamClass.lookup(BusinessException.class).getSerialVersionUID());
  }
}
