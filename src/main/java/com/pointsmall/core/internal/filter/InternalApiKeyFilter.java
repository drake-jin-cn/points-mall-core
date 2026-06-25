package com.pointsmall.core.internal.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pointsmall.core.common.ApiResponse;
import com.pointsmall.core.common.exception.CoreErrorCode;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.filter.OncePerRequestFilter;

public class InternalApiKeyFilter extends OncePerRequestFilter {

  private static final Logger log = LoggerFactory.getLogger(InternalApiKeyFilter.class);
  private static final String INTERNAL_API_KEY_HEADER = "INTERNAL_API_KEY";

  private final ObjectMapper objectMapper;
  private final String internalApiKey;

  public InternalApiKeyFilter(ObjectMapper objectMapper, String internalApiKey) {
    this.objectMapper = objectMapper;
    this.internalApiKey = internalApiKey;
  }

  @Override
  protected boolean shouldNotFilter(HttpServletRequest request) {
    return !request.getRequestURI().startsWith("/internal/");
  }

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {
    try {
      String requestApiKey = request.getHeader(INTERNAL_API_KEY_HEADER);
      boolean keyValid =
          requestApiKey != null
              && MessageDigest.isEqual(
                  internalApiKey.getBytes(StandardCharsets.UTF_8),
                  requestApiKey.getBytes(StandardCharsets.UTF_8));
      if (!keyValid) {
        log.warn(
            "Rejected /internal request from {} - missing or invalid API key",
            request.getRemoteAddr());
        writeJsonResponse(
            response,
            HttpServletResponse.SC_UNAUTHORIZED,
            ApiResponse.error(
                CoreErrorCode.UNAUTHORIZED_CALLER.getCode(),
                CoreErrorCode.UNAUTHORIZED_CALLER.getMessage(),
                UUID.randomUUID().toString()));
        return;
      }

      filterChain.doFilter(request, response);
    } catch (Exception ex) {
      String traceId = UUID.randomUUID().toString();
      log.error("Failed to process internal API key filter [traceId={}]", traceId, ex);
      if (!response.isCommitted()) {
        writeJsonResponse(
            response,
            HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
            ApiResponse.error(
                CoreErrorCode.INTERNAL_ERROR.getCode(),
                CoreErrorCode.INTERNAL_ERROR.getMessage(),
                traceId));
      }
    }
  }

  private void writeJsonResponse(HttpServletResponse response, int status, ApiResponse<Void> body)
      throws IOException {
    response.setStatus(status);
    response.setContentType("application/json; charset=UTF-8");
    response.getWriter().write(objectMapper.writeValueAsString(body));
  }
}
