package com.example.exception.monitor.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "exception_records")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExceptionRecord {
    
    @Id
    private String id;
    
    @Column(name = "exception_type", nullable = false)
    private String exceptionType;
    
    @Column(name = "message", columnDefinition = "TEXT")
    private String message;
    
    @Column(name = "stack_trace", columnDefinition = "TEXT")
    private String stackTrace;
    
    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp;
    
    @Column(name = "project_name")
    private String projectName;
    
    @Column(name = "component_name")
    private String componentName;
    
    @Column(name = "pod_name")
    private String podName;
    
    @Column(name = "pod_ip")
    private String podIp;
    
    @Column(name = "cluster_name")
    private String clusterName;
    
    @Column(name = "environment")
    private String environment;
    
    @Column(name = "service_name")
    private String serviceName;
    
    @Column(name = "method")
    private String method;
    
    @Column(name = "url", columnDefinition = "TEXT")
    private String url;
    
    @Column(name = "user_agent", columnDefinition = "TEXT")
    private String userAgent;
    
    @Column(name = "session_id")
    private String sessionId;
    
    @Column(name = "additional_data", columnDefinition = "TEXT")
    private String additionalData;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}