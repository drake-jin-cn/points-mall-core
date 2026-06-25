package com.pointsmall.core.internal.filter;

import com.pointsmall.core.common.ApiResponse;
import com.pointsmall.core.common.exception.CoreErrorCode;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import tools.jackson.databind.ObjectMapper;

@Component
public class InternalApiKeyFilter extends OncePerRequestFilter {

  private static final Logger log = LoggerFactory.getLogger(InternalApiKeyFilter.class);
  private static final String INTERNAL_PREFIX = "/internal/";
  private static final String INTERNAL_API_KEY_HEADER = "X-Internal-Api-Key";

  private final ObjectMapper objectMapper;
  private final String internalApiKey;

  public InternalApiKeyFilter(
      ObjectMapper objectMapper, @Value("${internal.api-key}") String internalApiKey) {
    this.objectMapper = objectMapper;
    this.internalApiKey = internalApiKey;
  }

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {
    try {
      String requestUri = request.getRequestURI();
      if (!requestUri.startsWith(INTERNAL_PREFIX)) {
        filterChain.doFilter(request, response);
        return;
      }

      String requestApiKey = request.getHeader(INTERNAL_API_KEY_HEADER);
      if (requestApiKey == null || !requestApiKey.equals(internalApiKey)) {
        log.warn(
            "Rejected /internal request from {} - missing or invalid API key",
            request.getRemoteAddr());
        writeJsonResponse(
            response,
            HttpServletResponse.SC_UNAUTHORIZED,
            ApiResponse.error(
                CoreErrorCode.UNAUTHORIZED_CALLER.getCode(),
                "Unauthorized caller",
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
