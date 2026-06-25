package com.pointsmall.core.employee;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@ExtendWith(MockitoExtension.class)
class EmployeeSeederTest {

  @Mock private EmployeeRepository employeeRepository;
  @Mock private RoleRepository roleRepository;
  @Mock private BCryptPasswordEncoder passwordEncoder;

  @Captor private ArgumentCaptor<Employee> employeeCaptor;

  private EmployeeSeeder seeder;

  @BeforeEach
  void setUp() {
    seeder = new EmployeeSeeder(employeeRepository, roleRepository, passwordEncoder);
  }

  @Test
  void seeder_skipsExistingEmployees() throws Exception {
    when(employeeRepository.existsByEmail("admin@pointsmall.com")).thenReturn(true);
    when(employeeRepository.existsByEmail("alice@pointsmall.com")).thenReturn(true);
    when(employeeRepository.existsByEmail("bob@pointsmall.com")).thenReturn(true);

    seeder.run(null);

    verify(employeeRepository, never()).save(any(Employee.class));
    verifyNoInteractions(roleRepository, passwordEncoder);
  }

  @Test
  void seeder_createsNewEmployees() throws Exception {
    Role adminRole = role("ADMIN");
    Role employeeRole = role("EMPLOYEE");
    when(employeeRepository.existsByEmail(any())).thenReturn(false);
    when(roleRepository.findByName("ADMIN")).thenReturn(Optional.of(adminRole));
    when(roleRepository.findByName("EMPLOYEE")).thenReturn(Optional.of(employeeRole));
    when(passwordEncoder.encode(any()))
        .thenAnswer(invocation -> "encoded-" + invocation.getArgument(0));

    seeder.run(null);

    verify(employeeRepository, times(3)).save(employeeCaptor.capture());

    assertThat(employeeCaptor.getAllValues())
        .extracting(Employee::getEmail)
        .containsExactly("admin@pointsmall.com", "alice@pointsmall.com", "bob@pointsmall.com");
    assertThat(employeeCaptor.getAllValues())
        .extracting(Employee::getName)
        .containsExactly("Admin User", "Alice Employee", "Bob Employee");
    assertThat(employeeCaptor.getAllValues())
        .extracting(Employee::getPasswordHash)
        .containsExactly("encoded-Admin@123456", "encoded-Alice@123456", "encoded-Bob@123456");
    assertThat(employeeCaptor.getAllValues().get(0).getRoles()).containsExactly(adminRole);
    assertThat(employeeCaptor.getAllValues().get(1).getRoles()).containsExactly(employeeRole);
    assertThat(employeeCaptor.getAllValues().get(2).getRoles()).containsExactly(employeeRole);
  }

  @Test
  void seeder_skipsWhenRoleNotFound() throws Exception {
    Role employeeRole = role("EMPLOYEE");
    when(employeeRepository.existsByEmail(any())).thenReturn(false);
    when(roleRepository.findByName("ADMIN")).thenReturn(Optional.empty());
    when(roleRepository.findByName("admin")).thenReturn(Optional.empty());
    when(roleRepository.findByName("EMPLOYEE")).thenReturn(Optional.of(employeeRole));
    when(passwordEncoder.encode(any()))
        .thenAnswer(invocation -> "encoded-" + invocation.getArgument(0));

    seeder.run(null);

    verify(employeeRepository, times(2)).save(employeeCaptor.capture());
    assertThat(employeeCaptor.getAllValues())
        .extracting(Employee::getEmail)
        .containsExactly("alice@pointsmall.com", "bob@pointsmall.com");
  }

  private static Role role(String name) {
    Role role = new Role();
    role.setName(name);
    return role;
  }
}
