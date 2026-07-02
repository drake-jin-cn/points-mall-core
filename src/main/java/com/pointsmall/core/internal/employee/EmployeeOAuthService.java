package com.pointsmall.core.internal.employee;

import com.pointsmall.core.employee.Employee;
import com.pointsmall.core.employee.EmployeeRepository;
import com.pointsmall.core.employee.Role;
import com.pointsmall.core.employee.RoleRepository;
import com.pointsmall.core.internal.employee.dto.EmployeeResponse;
import com.pointsmall.core.internal.employee.dto.FindOrCreateGithubRequest;
import java.util.HashSet;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EmployeeOAuthService {

  private static final String EMPLOYEE_ROLE = "EMPLOYEE";

  private final EmployeeRepository employeeRepository;
  private final RoleRepository roleRepository;

  public EmployeeOAuthService(
      EmployeeRepository employeeRepository, RoleRepository roleRepository) {
    this.employeeRepository = employeeRepository;
    this.roleRepository = roleRepository;
  }

  @Transactional
  public EmployeeResponse findOrCreateByGithub(FindOrCreateGithubRequest request) {
    Employee employee =
        employeeRepository
            .findByGithubId(request.getGithubId())
            .orElseGet(() -> findByEmailOrCreate(request));
    return toResponse(employee);
  }

  private Employee findByEmailOrCreate(FindOrCreateGithubRequest request) {
    return employeeRepository
        .findByEmail(request.getEmail())
        .map(employee -> linkGithubAccount(employee, request))
        .orElseGet(() -> createEmployee(request));
  }

  private Employee linkGithubAccount(Employee employee, FindOrCreateGithubRequest request) {
    employee.setGithubId(request.getGithubId());
    if (request.getAvatarUrl() != null) {
      employee.setAvatarUrl(request.getAvatarUrl());
    }
    return employeeRepository.save(employee);
  }

  private Employee createEmployee(FindOrCreateGithubRequest request) {
    Role employeeRole =
        roleRepository
            .findByName(EMPLOYEE_ROLE)
            .orElseThrow(() -> new IllegalStateException("EMPLOYEE role is required"));

    Employee employee = new Employee();
    employee.setName(request.getName());
    employee.setEmail(request.getEmail());
    employee.setPasswordHash(null);
    employee.setGithubId(request.getGithubId());
    employee.setAvatarUrl(request.getAvatarUrl());
    employee.setActive(true);
    employee.setRoles(new HashSet<>(List.of(employeeRole)));
    return employeeRepository.save(employee);
  }

  private EmployeeResponse toResponse(Employee employee) {
    List<String> roles = employee.getRoles().stream().map(Role::getName).toList();
    return new EmployeeResponse(
        employee.getId(),
        employee.getName(),
        employee.getEmail(),
        employee.getGithubId(),
        employee.getAvatarUrl(),
        employee.isActive(),
        roles);
  }
}
