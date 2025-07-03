package tech.realworks.yusuf.zaikabox.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Audit Logs", description = "APIs for retrieving audit logs and events")
public class AuditController {

    private final AuditRepository auditRepository;

    @Operation(summary = "Get all audit logs", description = "Retrieves all audit logs. Requires ADMIN role.")
    @ApiResponse(responseCode = "200", description = "Audit logs retrieved successfully", content = @Content(schema = @Schema(implementation = AuditEntity.class)))
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<List<AuditEntity>> getAllAuditLogs() {
        return ResponseEntity.ok(auditRepository.findAll());
    }

    @Operation(summary = "Get audit logs by user ID", description = "Retrieves audit logs for a specific user. Requires ADMIN role.")
    @ApiResponse(responseCode = "200", description = "Audit logs retrieved successfully", content = @Content(schema = @Schema(implementation = AuditEntity.class)))
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<AuditEntity>> getAuditLogsByUserId(@PathVariable String userId) {
        return ResponseEntity.ok(auditRepository.findByUserId(userId));
    }

    @Operation(summary = "Get audit logs by email", description = "Retrieves audit logs for a specific email. Requires ADMIN role.")
    @ApiResponse(responseCode = "200", description = "Audit logs retrieved successfully", content = @Content(schema = @Schema(implementation = AuditEntity.class)))
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/email")
    public ResponseEntity<List<AuditEntity>> getAuditLogsByEmail(@RequestParam String email) {
        return ResponseEntity.ok(auditRepository.findByEmail(email));
    }

    @Operation(summary = "Get audit logs by event type", description = "Retrieves audit logs for a specific event type. Requires ADMIN role.")
    @ApiResponse(responseCode = "200", description = "Audit logs retrieved successfully", content = @Content(schema = @Schema(implementation = AuditEntity.class)))
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/event-type")
    public ResponseEntity<List<AuditEntity>> getAuditLogsByEventType(@RequestParam String eventType) {
        return ResponseEntity.ok(auditRepository.findByEventType(eventType));
    }

    @Operation(summary = "Get audit logs by date range", description = "Retrieves audit logs within a date range. Requires ADMIN role.")
    @ApiResponse(responseCode = "200", description = "Audit logs retrieved successfully", content = @Content(schema = @Schema(implementation = AuditEntity.class)))
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/date-range")
    public ResponseEntity<List<AuditEntity>> getAuditLogsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        return ResponseEntity.ok(auditRepository.findByTimestampBetween(startDate, endDate));
    }
}
