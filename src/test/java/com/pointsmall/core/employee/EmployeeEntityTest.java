package com.pointsmall.core.employee;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.lang.reflect.Field;
import java.time.OffsetDateTime;
import java.util.HashSet;
import org.junit.jupiter.api.Test;

class EmployeeEntityTest {

  @Test
  void shouldTreatRolesWithSameIdAsEqualInsideSets() {
    Role first = new Role();
    first.setName("ADMIN");
    setId(first, 1L);

    Role second = new Role();
    second.setName("ADMIN");
    setId(second, 1L);

    HashSet<Role> roles = new HashSet<>();
    roles.add(first);
    roles.add(second);

    assertEquals(first, second);
    assertEquals(first.hashCode(), second.hashCode());
    assertEquals(1, roles.size());
  }

  @Test
  void shouldTreatEmployeesWithSameIdAsEqual() {
    Employee first = new Employee();
    first.setEmail("first@example.com");
    setId(first, 1L);

    Employee second = new Employee();
    second.setEmail("second@example.com");
    setId(second, 1L);

    assertEquals(first, second);
    assertEquals(first.hashCode(), second.hashCode());
  }

  @Test
  void shouldKeepTransientRolesDistinct() {
    Role first = new Role();
    Role second = new Role();

    assertNotEquals(first, second);
    assertFalse(first.equals(null));
    assertFalse(first.equals(new Object()));
  }

  @Test
  void shouldKeepTransientEmployeesDistinct() {
    Employee first = new Employee();
    Employee second = new Employee();

    assertNotEquals(first, second);
    assertFalse(first.equals(null));
    assertFalse(first.equals(new Object()));
  }

  @Test
  void shouldAssignMatchingCreationAndUpdateTimestampsOnPrePersist() {
    Employee employee = new Employee();

    employee.prePersist();

    OffsetDateTime createdAt = getOffsetDateTime(employee, "createdAt");
    OffsetDateTime updatedAt = getOffsetDateTime(employee, "updatedAt");

    assertNotNull(createdAt);
    assertSame(createdAt, updatedAt);
  }

  private static void setId(Object entity, Long id) {
    setField(entity, "id", id);
  }

  private static OffsetDateTime getOffsetDateTime(Object entity, String fieldName) {
    return (OffsetDateTime) getField(entity, fieldName);
  }

  private static Object getField(Object target, String fieldName) {
    try {
      Field field = target.getClass().getDeclaredField(fieldName);
      field.setAccessible(true);
      return field.get(target);
    } catch (ReflectiveOperationException e) {
      throw new AssertionError(e);
    }
  }

  private static void setField(Object target, String fieldName, Object value) {
    try {
      Field field = target.getClass().getDeclaredField(fieldName);
      field.setAccessible(true);
      field.set(target, value);
    } catch (ReflectiveOperationException e) {
      throw new AssertionError(e);
    }
  }
}
