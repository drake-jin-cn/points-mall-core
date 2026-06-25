package com.pointsmall.core.common;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

  private String code;
  private String message;
  private T data;
  private String traceId;

  private ApiResponse() {}

  public static <T> ApiResponse<T> ok(T data) {
    ApiResponse<T> response = new ApiResponse<>();
    response.code = "OK";
    response.message = "success";
    response.data = data;
    return response;
  }

  public static <T> ApiResponse<T> error(String code, String message, String traceId) {
    ApiResponse<T> response = new ApiResponse<>();
    response.code = code;
    response.message = message;
    response.traceId = traceId;
    return response;
  }

  public String getCode() {
    return code;
  }

  public String getMessage() {
    return message;
  }

  public T getData() {
    return data;
  }

  public String getTraceId() {
    return traceId;
  }
}
