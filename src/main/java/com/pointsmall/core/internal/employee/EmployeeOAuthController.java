package com.pointsmall.core.internal.employee;

import com.pointsmall.core.common.ApiResponse;
import com.pointsmall.core.internal.employee.dto.EmployeeResponse;
import com.pointsmall.core.internal.employee.dto.FindOrCreateGithubRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal/employees")
public class EmployeeOAuthController {

  private final EmployeeOAuthService employeeOAuthService;

  public EmployeeOAuthController(EmployeeOAuthService employeeOAuthService) {
    this.employeeOAuthService = employeeOAuthService;
  }

  @PostMapping("/find-or-create-by-github")
  public ResponseEntity<ApiResponse<EmployeeResponse>> findOrCreateByGithub(
      @Valid @RequestBody FindOrCreateGithubRequest request) {
    EmployeeResponse result = employeeOAuthService.findOrCreateByGithub(request);
    return ResponseEntity.ok(ApiResponse.ok(result));
  }
}
