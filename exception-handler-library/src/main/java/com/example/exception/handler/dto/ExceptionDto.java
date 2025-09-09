package com.example.exception.handler.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExceptionDto {
    private String id;
    private String exceptionType;
    private String message;
    private String stackTrace;
    private LocalDateTime timestamp;
    private String projectName;
    private String podName;
    private String podIp;
    private String clusterName;
    private String environment;
    private String serviceName;
    private String method;
    private String url;
    private String userAgent;
    private String sessionId;
    private Map<String, Object> additionalData;
}