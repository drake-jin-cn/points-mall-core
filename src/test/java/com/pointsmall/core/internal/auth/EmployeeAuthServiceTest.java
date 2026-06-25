package com.pointsmall.core.internal.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.catchThrowableOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.pointsmall.core.common.exception.BusinessException;
import com.pointsmall.core.common.exception.CoreErrorCode;
import com.pointsmall.core.employee.Employee;
import com.pointsmall.core.employee.EmployeeRepository;
import com.pointsmall.core.employee.Role;
import com.pointsmall.core.internal.auth.dto.VerifyResponse;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class EmployeeAuthServiceTest {

  @Mock private EmployeeRepository employeeRepository;

  private EmployeeAuthService service;

  // Use strength 4 in tests for speed; BCrypt matches() reads strength from the hash string
  private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(4);

  @BeforeEach
  void setUp() {
    service = new EmployeeAuthService(employeeRepository);
  }

  @Test
  void verify_validCredentials_returnsEmployeeInfo() {
    Employee emp =
        buildEmployee(1L, "alice@test.com", "Alice", encoder.encode("Pass@123"), true, "employee");
    when(employeeRepository.findByEmail("alice@test.com")).thenReturn(Optional.of(emp));

    VerifyResponse res = service.verify("alice@test.com", "Pass@123");

    assertThat(res.getId()).isEqualTo(1L);
    assertThat(res.getEmail()).isEqualTo("alice@test.com");
    assertThat(res.getName()).isEqualTo("Alice");
    assertThat(res.isActive()).isTrue();
    assertThat(res.getRoles()).containsExactly("employee");
  }

  @Test
  void verify_emailNotFound_throwsInvalidCredentials() {
    when(employeeRepository.findByEmail(any())).thenReturn(Optional.empty());

    assertThatThrownBy(() -> service.verify("ghost@test.com", "any"))
        .isInstanceOf(BusinessException.class)
        .satisfies(
            e ->
                assertThat(((BusinessException) e).getErrorCode())
                    .isEqualTo(CoreErrorCode.INVALID_CREDENTIALS));
  }

  @Test
  void verify_wrongPassword_throwsInvalidCredentials() {
    Employee emp =
        buildEmployee(1L, "alice@test.com", "Alice", encoder.encode("Pass@123"), true, "employee");
    when(employeeRepository.findByEmail("alice@test.com")).thenReturn(Optional.of(emp));

    assertThatThrownBy(() -> service.verify("alice@test.com", "WrongPassword"))
        .isInstanceOf(BusinessException.class)
        .satisfies(
            e ->
                assertThat(((BusinessException) e).getErrorCode())
                    .isEqualTo(CoreErrorCode.INVALID_CREDENTIALS));
  }

  @Test
  void verify_accountDisabled_throwsAccountDisabled() {
    Employee emp =
        buildEmployee(1L, "alice@test.com", "Alice", encoder.encode("Pass@123"), false, "employee");
    when(employeeRepository.findByEmail("alice@test.com")).thenReturn(Optional.of(emp));

    assertThatThrownBy(() -> service.verify("alice@test.com", "Pass@123"))
        .isInstanceOf(BusinessException.class)
        .satisfies(
            e ->
                assertThat(((BusinessException) e).getErrorCode())
                    .isEqualTo(CoreErrorCode.ACCOUNT_DISABLED));
  }

  @Test
  void verify_emailNotFoundAndWrongPassword_sameErrorCodePreventsEnumeration() {
    when(employeeRepository.findByEmail("ghost@test.com")).thenReturn(Optional.empty());
    Employee emp = buildEmployee(2L, "b@test.com", "B", encoder.encode("P@123"), true, "employee");
    when(employeeRepository.findByEmail("b@test.com")).thenReturn(Optional.of(emp));

    BusinessException notFound =
        catchThrowableOfType(
            () -> service.verify("ghost@test.com", "any"), BusinessException.class);
    BusinessException wrongPwd =
        catchThrowableOfType(() -> service.verify("b@test.com", "wrong"), BusinessException.class);

    assertThat(notFound.getErrorCode()).isEqualTo(wrongPwd.getErrorCode());
    assertThat(notFound.getMessage()).isEqualTo(wrongPwd.getMessage());
  }

  private Employee buildEmployee(
      Long id, String email, String name, String hash, boolean active, String roleName) {
    Role role = new Role();
    role.setName(roleName);
    Employee employee = new Employee();
    ReflectionTestUtils.setField(employee, "id", id);
    employee.setEmail(email);
    employee.setName(name);
    employee.setPasswordHash(hash);
    employee.setActive(active);
    employee.setRoles(Set.of(role));
    return employee;
  }
}
