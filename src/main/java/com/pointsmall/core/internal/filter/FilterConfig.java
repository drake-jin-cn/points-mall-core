package com.pointsmall.core.internal.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FilterConfig {

  @Bean
  public FilterRegistrationBean<InternalApiKeyFilter> internalApiKeyFilterRegistration(
      ObjectMapper objectMapper, @Value("${internal.api-key}") String apiKey) {
    FilterRegistrationBean<InternalApiKeyFilter> reg =
        new FilterRegistrationBean<>(new InternalApiKeyFilter(objectMapper, apiKey));
    reg.addUrlPatterns("/internal/*");
    reg.setOrder(1);
    return reg;
  }
}
