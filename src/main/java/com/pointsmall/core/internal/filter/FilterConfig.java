package com.pointsmall.core.internal.filter;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FilterConfig {

  @Bean
  public FilterRegistrationBean<InternalApiKeyFilter> internalApiKeyFilterRegistration(
      InternalApiKeyFilter filter) {
    FilterRegistrationBean<InternalApiKeyFilter> reg = new FilterRegistrationBean<>(filter);
    reg.addUrlPatterns("/internal/*");
    return reg;
  }
}
