package com.pointsmall.core.employee;

import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@Profile({"dev", "test"})
public class EmployeeSeeder implements ApplicationRunner {

  private static final Logger log = LoggerFactory.getLogger(EmployeeSeeder.class);

  private final EmployeeRepository employeeRepository;
  private final RoleRepository roleRepository;
  private final BCryptPasswordEncoder passwordEncoder;

  public EmployeeSeeder(
      EmployeeRepository employeeRepository,
      RoleRepository roleRepository,
      BCryptPasswordEncoder passwordEncoder) {
    this.employeeRepository = employeeRepository;
    this.roleRepository = roleRepository;
    this.passwordEncoder = passwordEncoder;
  }

  @Override
  public void run(ApplicationArguments args) {
    seedEmployee("Admin User", "admin@pointsmall.com", "Admin@123456", "ADMIN");
    seedEmployee("Alice Employee", "alice@pointsmall.com", "Alice@123456", "EMPLOYEE");
    seedEmployee("Bob Employee", "bob@pointsmall.com", "Bob@123456", "EMPLOYEE");
  }

  private void seedEmployee(String name, String email, String password, String roleName) {
    if (employeeRepository.existsByEmail(email)) {
      return;
    }

    Optional<Role> role = findRole(roleName);
    if (role.isEmpty()) {
      log.warn("Role not found for employee {}: {}", email, roleName);
      return;
    }

    Employee employee = new Employee();
    employee.setName(name);
    employee.setEmail(email);
    employee.setPasswordHash(passwordEncoder.encode(password));
    employee.setActive(true);
    employee.setRoles(Set.of(role.get()));

    employeeRepository.save(employee);
    log.info("Seeded employee: {}", email);
  }

  private Optional<Role> findRole(String roleName) {
    Optional<Role> role = roleRepository.findByName(roleName);
    if (role.isPresent()) {
      return role;
    }
    return roleRepository.findByName(roleName.toLowerCase(Locale.ROOT));
  }
}
