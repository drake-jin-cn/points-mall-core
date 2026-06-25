package com.pointsmall.core.internal.auth;

import com.pointsmall.core.common.exception.BusinessException;
import com.pointsmall.core.common.exception.CoreErrorCode;
import com.pointsmall.core.employee.Employee;
import com.pointsmall.core.employee.EmployeeRepository;
import com.pointsmall.core.employee.Role;
import com.pointsmall.core.internal.auth.dto.VerifyRequest;
import com.pointsmall.core.internal.auth.dto.VerifyResponse;
import java.util.List;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class EmployeeAuthService {

  private final EmployeeRepository employeeRepository;
  private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder(12);

  public EmployeeAuthService(EmployeeRepository employeeRepository) {
    this.employeeRepository = employeeRepository;
  }

  public VerifyResponse verify(VerifyRequest request) {
    return verify(request.getEmail(), request.getPassword());
  }

  public VerifyResponse verify(String email, String password) {
    Employee employee =
        employeeRepository
            .findByEmail(email)
            .orElseThrow(() -> new BusinessException(CoreErrorCode.INVALID_CREDENTIALS));

    if (!passwordEncoder.matches(password, employee.getPasswordHash())) {
      throw new BusinessException(CoreErrorCode.INVALID_CREDENTIALS);
    }

    if (!employee.isActive()) {
      throw new BusinessException(CoreErrorCode.ACCOUNT_DISABLED);
    }

    List<String> roles = employee.getRoles().stream().map(Role::getName).toList();
    return new VerifyResponse(
        employee.getId(), employee.getName(), employee.getEmail(), true, roles);
  }
}
