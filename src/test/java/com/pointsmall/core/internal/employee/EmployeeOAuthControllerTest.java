package com.pointsmall.core.internal.employee;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.pointsmall.core.common.exception.GlobalExceptionHandler;
import com.pointsmall.core.employee.Employee;
import com.pointsmall.core.employee.EmployeeRepository;
import com.pointsmall.core.employee.RoleRepository;
import com.pointsmall.core.internal.filter.InternalApiKeyFilter;
import java.util.Set;
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
    scripts = {"/db/seed-roles.sql"},
    executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS)
class EmployeeOAuthControllerTest {

  private static final String FIND_OR_CREATE_URL = "/internal/employees/find-or-create-by-github";
  private static final String INTERNAL_API_KEY_HEADER = "INTERNAL_API_KEY";
  private static final String INTERNAL_API_KEY = "test-internal-key-for-tests";

  @Autowired private EmployeeOAuthController employeeOAuthController;
  @Autowired private EmployeeRepository employeeRepository;
  @Autowired private RoleRepository roleRepository;

  private MockMvc mockMvc;

  @BeforeEach
  void setUp() {
    employeeRepository.deleteAll();
    mockMvc =
        MockMvcBuilders.standaloneSetup(employeeOAuthController)
            .setControllerAdvice(new GlobalExceptionHandler())
            .addFilters(new InternalApiKeyFilter(new ObjectMapper(), INTERNAL_API_KEY))
            .build();
  }

  @Test
  void findOrCreateByGithub_createsNewEmployeeWhenNoMatchExists() throws Exception {
    mockMvc
        .perform(
            post(FIND_OR_CREATE_URL)
                .header(INTERNAL_API_KEY_HEADER, INTERNAL_API_KEY)
                .contentType(APPLICATION_JSON)
                .content(
                    """
                    {
                      "githubId": "github-123",
                      "email": "new-github@example.com",
                      "name": "New Github User",
                      "avatarUrl": "https://avatars.example.com/u/123"
                    }
                    """))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code").value("OK"))
        .andExpect(jsonPath("$.data.email").value("new-github@example.com"))
        .andExpect(jsonPath("$.data.name").value("New Github User"))
        .andExpect(jsonPath("$.data.githubId").value("github-123"))
        .andExpect(jsonPath("$.data.avatarUrl").value("https://avatars.example.com/u/123"))
        .andExpect(jsonPath("$.data.isActive").value(true))
        .andExpect(jsonPath("$.data.roles[0]").value("EMPLOYEE"))
        .andExpect(jsonPath("$.traceId").doesNotExist());

    Employee employee = employeeRepository.findByEmail("new-github@example.com").orElseThrow();
    org.junit.jupiter.api.Assertions.assertNull(employee.getPasswordHash());
    org.junit.jupiter.api.Assertions.assertEquals("github-123", employee.getGithubId());
    org.junit.jupiter.api.Assertions.assertEquals(
        "https://avatars.example.com/u/123", employee.getAvatarUrl());
  }

  @Test
  void findOrCreateByGithub_returnsExistingEmployeeWhenGithubIdMatches() throws Exception {
    Employee existingEmployee = new Employee();
    existingEmployee.setName("Existing Github User");
    existingEmployee.setEmail("existing-github@example.com");
    existingEmployee.setPasswordHash("unused-password-hash");
    existingEmployee.setGithubId("github-existing");
    existingEmployee.setAvatarUrl("https://avatars.example.com/u/existing");
    existingEmployee.setActive(true);
    existingEmployee.setRoles(Set.of(roleRepository.findByName("EMPLOYEE").orElseThrow()));
    existingEmployee = employeeRepository.save(existingEmployee);

    mockMvc
        .perform(
            post(FIND_OR_CREATE_URL)
                .header(INTERNAL_API_KEY_HEADER, INTERNAL_API_KEY)
                .contentType(APPLICATION_JSON)
                .content(
                    """
                    {
                      "githubId": "github-existing",
                      "email": "different@example.com",
                      "name": "Different Name",
                      "avatarUrl": "https://avatars.example.com/u/updated"
                    }
                    """))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code").value("OK"))
        .andExpect(jsonPath("$.data.id").value(existingEmployee.getId()))
        .andExpect(jsonPath("$.data.email").value("existing-github@example.com"))
        .andExpect(jsonPath("$.data.name").value("Existing Github User"))
        .andExpect(jsonPath("$.data.githubId").value("github-existing"))
        .andExpect(jsonPath("$.data.avatarUrl").value("https://avatars.example.com/u/existing"))
        .andExpect(jsonPath("$.data.roles[0]").value("EMPLOYEE"));

    org.junit.jupiter.api.Assertions.assertEquals(1, employeeRepository.count());
  }

  @Test
  void findOrCreateByGithub_linksGithubIdOntoExistingEmailAccount() throws Exception {
    Employee existingEmployee = new Employee();
    existingEmployee.setName("Password Employee");
    existingEmployee.setEmail("password-employee@example.com");
    existingEmployee.setPasswordHash("existing-password-hash");
    existingEmployee.setActive(true);
    existingEmployee.setRoles(Set.of(roleRepository.findByName("EMPLOYEE").orElseThrow()));
    existingEmployee = employeeRepository.save(existingEmployee);

    mockMvc
        .perform(
            post(FIND_OR_CREATE_URL)
                .header(INTERNAL_API_KEY_HEADER, INTERNAL_API_KEY)
                .contentType(APPLICATION_JSON)
                .content(
                    """
                    {
                      "githubId": "github-linked",
                      "email": "password-employee@example.com",
                      "name": "Github Name Should Not Replace",
                      "avatarUrl": "https://avatars.example.com/u/linked"
                    }
                    """))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code").value("OK"))
        .andExpect(jsonPath("$.data.id").value(existingEmployee.getId()))
        .andExpect(jsonPath("$.data.email").value("password-employee@example.com"))
        .andExpect(jsonPath("$.data.name").value("Password Employee"))
        .andExpect(jsonPath("$.data.githubId").value("github-linked"))
        .andExpect(jsonPath("$.data.avatarUrl").value("https://avatars.example.com/u/linked"))
        .andExpect(jsonPath("$.data.roles[0]").value("EMPLOYEE"));

    Employee linkedEmployee =
        employeeRepository.findByEmail("password-employee@example.com").orElseThrow();
    org.junit.jupiter.api.Assertions.assertEquals(existingEmployee.getId(), linkedEmployee.getId());
    org.junit.jupiter.api.Assertions.assertEquals(
        "existing-password-hash", linkedEmployee.getPasswordHash());
    org.junit.jupiter.api.Assertions.assertEquals("github-linked", linkedEmployee.getGithubId());
    org.junit.jupiter.api.Assertions.assertEquals(
        "https://avatars.example.com/u/linked", linkedEmployee.getAvatarUrl());
    org.junit.jupiter.api.Assertions.assertEquals(1, employeeRepository.count());
  }

  @Test
  void findOrCreateByGithub_missingApiKey_returns401() throws Exception {
    mockMvc
        .perform(
            post(FIND_OR_CREATE_URL)
                .contentType(APPLICATION_JSON)
                .content(
                    """
                    {
                      "githubId": "github-unauthorized",
                      "email": "unauthorized@example.com",
                      "name": "Unauthorized",
                      "avatarUrl": null
                    }
                    """))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.code").value("core-1003"))
        .andExpect(jsonPath("$.message").value("Missing or invalid API key"))
        .andExpect(jsonPath("$.traceId").isNotEmpty());
  }

  @Test
  void findOrCreateByGithub_invalidEmail_returns400() throws Exception {
    mockMvc
        .perform(
            post(FIND_OR_CREATE_URL)
                .header(INTERNAL_API_KEY_HEADER, INTERNAL_API_KEY)
                .contentType(APPLICATION_JSON)
                .content(
                    """
                    {
                      "githubId": "github-invalid-email",
                      "email": "not-an-email",
                      "name": "Invalid Email User",
                      "avatarUrl": null
                    }
                    """))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value("core-1010"))
        .andExpect(jsonPath("$.message").value("Request validation failed"))
        .andExpect(jsonPath("$.traceId").isNotEmpty());
  }
}
