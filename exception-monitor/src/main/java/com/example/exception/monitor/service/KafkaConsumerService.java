package com.example.exception.monitor.service;

import com.example.exception.monitor.entity.ExceptionRecord;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaConsumerService {
    
    private final ExceptionRecordService exceptionRecordService;
    private final ObjectMapper objectMapper;
    
    @KafkaListener(topics = "exceptions", groupId = "exception-monitor-group")
    public void handleExceptionMessage(String message) {
        try {
            log.info("Received exception message: {}", message);
            
            JsonNode jsonNode = objectMapper.readTree(message);
            ExceptionRecord record = mapToExceptionRecord(jsonNode);
            
            exceptionRecordService.save(record);
            log.info("Exception record saved with ID: {}", record.getId());
            
        } catch (Exception e) {
            log.error("Error processing exception message: {}", message, e);
        }
    }
    
    private ExceptionRecord mapToExceptionRecord(JsonNode jsonNode) {
        ExceptionRecord record = new ExceptionRecord();
        
        record.setId(getStringValue(jsonNode, "id"));
        record.setExceptionType(getStringValue(jsonNode, "exceptionType"));
        record.setMessage(getStringValue(jsonNode, "message"));
        record.setStackTrace(getStringValue(jsonNode, "stackTrace"));
        
        // Timestamp parsing
        String timestampStr = getStringValue(jsonNode, "timestamp");
        if (timestampStr != null) {
            try {
                record.setTimestamp(LocalDateTime.parse(timestampStr, DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            } catch (Exception e) {
                log.warn("Failed to parse timestamp: {}", timestampStr);
                record.setTimestamp(LocalDateTime.now());
            }
        } else {
            record.setTimestamp(LocalDateTime.now());
        }
        
        record.setProjectName(getStringValue(jsonNode, "projectName"));
        record.setPodName(getStringValue(jsonNode, "podName"));
        record.setPodIp(getStringValue(jsonNode, "podIp"));
        record.setClusterName(getStringValue(jsonNode, "clusterName"));
        record.setEnvironment(getStringValue(jsonNode, "environment"));
        record.setServiceName(getStringValue(jsonNode, "serviceName"));
        record.setMethod(getStringValue(jsonNode, "method"));
        record.setUrl(getStringValue(jsonNode, "url"));
        record.setUserAgent(getStringValue(jsonNode, "userAgent"));
        record.setSessionId(getStringValue(jsonNode, "sessionId"));
        
        // Additional data as JSON string
        JsonNode additionalDataNode = jsonNode.get("additionalData");
        if (additionalDataNode != null && !additionalDataNode.isNull()) {
            record.setAdditionalData(additionalDataNode.toString());
        }
        
        return record;
    }
    
    private String getStringValue(JsonNode jsonNode, String fieldName) {
        JsonNode node = jsonNode.get(fieldName);
        return (node != null && !node.isNull()) ? node.asText() : null;
    }
}