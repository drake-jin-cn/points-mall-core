package com.pointsmall.core.internal.auth;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.pointsmall.core.common.exception.GlobalExceptionHandler;
import com.pointsmall.core.internal.filter.InternalApiKeyFilter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import tools.jackson.databind.ObjectMapper;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Sql(
    scripts = {"/db/seed-roles.sql", "/db/seed-test-employee.sql"},
    executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS)
@Sql(
    scripts = "/db/cleanup-test-employee.sql",
    executionPhase = Sql.ExecutionPhase.AFTER_TEST_CLASS)
class AuthVerifyControllerTest {

  private static final String VERIFY_URL = "/internal/auth/verify";
  private static final String INTERNAL_API_KEY_HEADER = "X-Internal-Api-Key";
  private static final String INTERNAL_API_KEY = "test-internal-key-for-tests";

  @Autowired private AuthVerifyController authVerifyController;

  private MockMvc mockMvc;

  @BeforeEach
  void setUp() {
    mockMvc =
        MockMvcBuilders.standaloneSetup(authVerifyController)
            .setControllerAdvice(new GlobalExceptionHandler())
            .build();
  }

  @Test
  void verify_validCredentials_returns200() throws Exception {
    mockMvc
        .perform(
            post(VERIFY_URL)
                .contentType(APPLICATION_JSON)
                .content(
                    """
                    {
                      "email": "test@example.com",
                      "password": "password123"
                    }
                    """))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code").value("OK"))
        .andExpect(jsonPath("$.data.email").value("test@example.com"))
        .andExpect(jsonPath("$.data.isActive").value(true));
  }

  @Test
  void verify_wrongPassword_returns401() throws Exception {
    mockMvc
        .perform(
            post(VERIFY_URL)
                .contentType(APPLICATION_JSON)
                .content(
                    """
                    {
                      "email": "test@example.com",
                      "password": "wrong-password"
                    }
                    """))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.code").value("core-1001"));
  }

  @Test
  void verify_unknownEmail_returns401() throws Exception {
    mockMvc
        .perform(
            post(VERIFY_URL)
                .contentType(APPLICATION_JSON)
                .content(
                    """
                    {
                      "email": "unknown@example.com",
                      "password": "password123"
                    }
                    """))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.code").value("core-1001"));
  }

  @Test
  void verify_invalidEmailFormat_returns400() throws Exception {
    mockMvc
        .perform(
            post(VERIFY_URL)
                .contentType(APPLICATION_JSON)
                .content(
                    """
                    {
                      "email": "not-an-email",
                      "password": "password123"
                    }
                    """))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value("core-1010"));
  }

  @Test
  void verify_missingApiKey_returns401() throws Exception {
    MockMvc filteredMockMvc =
        MockMvcBuilders.standaloneSetup(authVerifyController)
            .setControllerAdvice(new GlobalExceptionHandler())
            .addFilters(new InternalApiKeyFilter(new ObjectMapper(), INTERNAL_API_KEY))
            .build();

    filteredMockMvc
        .perform(
            post(VERIFY_URL)
                .contentType(APPLICATION_JSON)
                .content(
                    """
                    {
                      "email": "test@example.com",
                      "password": "password123"
                    }
                    """))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.code").value("core-1003"));
  }
}
