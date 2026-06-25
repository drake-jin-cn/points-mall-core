package com.pointsmall.core.health;

import java.lang.management.ManagementFactory;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/health")
public class HealthController {

  @Autowired(required = false)
  private DataSource dataSource;

  @GetMapping
  public ResponseEntity<Map<String, Object>> health() {
    String dbStatus = probeDatabase();
    long uptimeSeconds = ManagementFactory.getRuntimeMXBean().getUptime() / 1000;

    Map<String, Object> body = new LinkedHashMap<>();
    body.put("status", "ok");
    body.put("service", "points-mall-core");
    body.put("timestamp", Instant.now().toString());
    body.put("db", dbStatus);
    body.put("uptime", uptimeSeconds);

    return ResponseEntity.ok(body);
  }

  private String probeDatabase() {
    if (dataSource == null) {
      return "error";
    }
    try (var conn = dataSource.getConnection()) {
      return conn.isValid(1) ? "ok" : "error";
    } catch (Exception e) {
      return "error";
    }
  }
}
