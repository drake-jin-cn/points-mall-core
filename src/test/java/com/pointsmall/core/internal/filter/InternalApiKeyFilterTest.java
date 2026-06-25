package com.pointsmall.core.internal.filter;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import tools.jackson.databind.ObjectMapper;

@ExtendWith(MockitoExtension.class)
class InternalApiKeyFilterTest {

  private final ObjectMapper objectMapper = new ObjectMapper();
  private final InternalApiKeyFilter filter = new InternalApiKeyFilter(objectMapper, "test-key");

  @Test
  void validKey_shouldPassThrough() throws Exception {
    MockHttpServletRequest request = new MockHttpServletRequest("GET", "/internal/auth/verify");
    request.addHeader("X-Internal-Api-Key", "test-key");
    MockHttpServletResponse response = new MockHttpServletResponse();
    MockFilterChain filterChain = new MockFilterChain();

    filter.doFilter(request, response, filterChain);

    assertThat(filterChain.getRequest()).isNotNull();
    assertThat(filterChain.getResponse()).isNotNull();
    assertThat(response.getContentAsString()).isEmpty();
  }

  @Test
  void missingKey_shouldReturn401() throws Exception {
    MockHttpServletRequest request = new MockHttpServletRequest("GET", "/internal/auth/verify");
    MockHttpServletResponse response = new MockHttpServletResponse();
    MockFilterChain filterChain = new MockFilterChain();

    filter.doFilter(request, response, filterChain);

    assertThat(response.getStatus()).isEqualTo(401);
    assertThat(response.getContentType()).isEqualTo("application/json; charset=UTF-8");
    assertThat(response.getContentAsString()).contains("\"core-1003\"");
    assertThat(response.getContentAsString()).contains("\"Unauthorized caller\"");
    assertThat(filterChain.getRequest()).isNull();
  }

  @Test
  void wrongKey_shouldReturn401() throws Exception {
    MockHttpServletRequest request = new MockHttpServletRequest("GET", "/internal/auth/verify");
    request.addHeader("X-Internal-Api-Key", "wrong-key");
    MockHttpServletResponse response = new MockHttpServletResponse();
    MockFilterChain filterChain = new MockFilterChain();

    filter.doFilter(request, response, filterChain);

    assertThat(response.getStatus()).isEqualTo(401);
    assertThat(response.getContentAsString()).contains("\"core-1003\"");
    assertThat(response.getContentAsString()).contains("\"Unauthorized caller\"");
    assertThat(filterChain.getRequest()).isNull();
  }

  @Test
  void nonInternalPath_shouldPassThrough() throws Exception {
    MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/health");
    MockHttpServletResponse response = new MockHttpServletResponse();
    MockFilterChain filterChain = new MockFilterChain();

    filter.doFilter(request, response, filterChain);

    assertThat(filterChain.getRequest()).isNotNull();
    assertThat(filterChain.getResponse()).isNotNull();
    assertThat(response.getContentAsString()).isEmpty();
  }
}
