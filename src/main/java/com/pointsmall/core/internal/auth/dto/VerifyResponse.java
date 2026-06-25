package com.pointsmall.core.internal.auth.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class VerifyResponse {

  private Long id;
  private String name;
  private String email;

  @JsonProperty("isActive")
  private boolean isActive;

  private List<String> roles;

  public VerifyResponse(Long id, String name, String email, boolean isActive, List<String> roles) {
    this.id = id;
    this.name = name;
    this.email = email;
    this.isActive = isActive;
    this.roles = roles;
  }

  public Long getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public String getEmail() {
    return email;
  }

  public boolean isActive() {
    return isActive;
  }

  public List<String> getRoles() {
    return roles;
  }
}
