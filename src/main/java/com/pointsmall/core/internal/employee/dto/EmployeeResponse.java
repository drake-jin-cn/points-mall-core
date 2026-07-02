package com.pointsmall.core.internal.employee.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class EmployeeResponse {

  private Long id;
  private String name;
  private String email;
  private String githubId;
  private String avatarUrl;

  @JsonProperty("isActive")
  private boolean isActive;

  private List<String> roles;

  public EmployeeResponse(
      Long id,
      String name,
      String email,
      String githubId,
      String avatarUrl,
      boolean isActive,
      List<String> roles) {
    this.id = id;
    this.name = name;
    this.email = email;
    this.githubId = githubId;
    this.avatarUrl = avatarUrl;
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

  public String getGithubId() {
    return githubId;
  }

  public String getAvatarUrl() {
    return avatarUrl;
  }

  public boolean isActive() {
    return isActive;
  }

  public List<String> getRoles() {
    return roles;
  }
}
