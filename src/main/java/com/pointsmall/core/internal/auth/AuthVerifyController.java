package com.pointsmall.core.internal.auth;

import com.pointsmall.core.common.ApiResponse;
import com.pointsmall.core.internal.auth.dto.VerifyRequest;
import com.pointsmall.core.internal.auth.dto.VerifyResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal/auth")
public class AuthVerifyController {

  private final EmployeeAuthService employeeAuthService;

  public AuthVerifyController(EmployeeAuthService employeeAuthService) {
    this.employeeAuthService = employeeAuthService;
  }

  @PostMapping("/verify")
  public ResponseEntity<ApiResponse<VerifyResponse>> verify(
      @Valid @RequestBody VerifyRequest request) {
    VerifyResponse result = employeeAuthService.verify(request);
    return ResponseEntity.ok(ApiResponse.ok(result));
  }
}
