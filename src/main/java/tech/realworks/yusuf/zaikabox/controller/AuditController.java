package tech.realworks.yusuf.zaikabox.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import tech.realworks.yusuf.zaikabox.entity.AuditEntity;
import tech.realworks.yusuf.zaikabox.repository.AuditRepository;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/audit")
public class AuditController {

    private final AuditRepository auditRepository;

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<List<AuditEntity>> getAllAuditLogs() {
        return ResponseEntity.ok(auditRepository.findAll());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<AuditEntity>> getAuditLogsByUserId(@PathVariable String userId) {
        return ResponseEntity.ok(auditRepository.findByUserId(userId));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/email")
    public ResponseEntity<List<AuditEntity>> getAuditLogsByEmail(@RequestParam String email) {
        return ResponseEntity.ok(auditRepository.findByEmail(email));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/event-type")
    public ResponseEntity<List<AuditEntity>> getAuditLogsByEventType(@RequestParam String eventType) {
        return ResponseEntity.ok(auditRepository.findByEventType(eventType));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/date-range")
    public ResponseEntity<List<AuditEntity>> getAuditLogsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        return ResponseEntity.ok(auditRepository.findByTimestampBetween(startDate, endDate));
    }
}
