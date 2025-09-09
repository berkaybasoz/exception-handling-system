package com.example.exception.handler.service;

import com.example.exception.handler.config.ExceptionHandlerProperties;
import com.example.exception.handler.dto.ExceptionDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExceptionHandler {
    
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ExceptionHandlerProperties properties;
    private final ObjectMapper objectMapper;
    
    public void handle(Exception exception) {
        handle(exception, null);
    }
    
    public void handle(Exception exception, Map<String, Object> additionalData) {
        try {
            ExceptionDto dto = createExceptionDto(exception, additionalData);
            
            // Kafka'ya gönder
            String jsonDto = objectMapper.writeValueAsString(dto);
            kafkaTemplate.send(properties.getKafka().getTopic(), dto.getId(), jsonDto);
            
            // Loga yaz
            log.error("Exception handled and sent to Kafka: {} - {}", 
                dto.getExceptionType(), dto.getMessage(), exception);
                
        } catch (JsonProcessingException e) {
            log.error("Error serializing exception DTO", e);
        } catch (Exception e) {
            log.error("Error handling exception", e);
        }
    }
    
    public void handleWithHttpHeaders(Exception exception) {
        handleWithHttpHeaders(exception, null);
    }
    
    public void handleWithHttpHeaders(Exception exception, Map<String, Object> additionalData) {
        try {
            // HTTP headers'ı otomatik olarak additional data'ya ekle
            Map<String, Object> enhancedData = enhanceWithHttpHeaders(additionalData);
            
            ExceptionDto dto = createExceptionDto(exception, enhancedData);
            
            // Kafka'ya gönder
            String jsonDto = objectMapper.writeValueAsString(dto);
            kafkaTemplate.send(properties.getKafka().getTopic(), dto.getId(), jsonDto);
            
            // Loga yaz
            log.error("Exception handled with HTTP headers and sent to Kafka: {} - {}", 
                dto.getExceptionType(), dto.getMessage(), exception);
                
        } catch (JsonProcessingException e) {
            log.error("Error serializing exception DTO", e);
        } catch (Exception e) {
            log.error("Error handling exception", e);
        }
    }
    
    private ExceptionDto createExceptionDto(Exception exception, Map<String, Object> additionalData) {
        ExceptionDto dto = new ExceptionDto();
        dto.setId(UUID.randomUUID().toString());
        dto.setExceptionType(exception.getClass().getSimpleName());
        dto.setMessage(exception.getMessage());
        dto.setStackTrace(getStackTrace(exception));
        dto.setTimestamp(LocalDateTime.now());
        
        // Configuration'dan gelen değerler
        dto.setProjectName(properties.getProjectName());
        dto.setComponentName(properties.getComponentName());
        dto.setPodName(properties.getPodName());
        dto.setPodIp(properties.getPodIp());
        dto.setClusterName(properties.getClusterName());
        dto.setEnvironment(properties.getEnvironment());
        
        // HTTP request bilgileri (varsa)
        try {
            ServletRequestAttributes attr = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
            HttpServletRequest request = attr.getRequest();
            
            dto.setServiceName(request.getServletPath());
            dto.setMethod(request.getMethod());
            dto.setUrl(request.getRequestURL().toString());
            dto.setUserAgent(request.getHeader("User-Agent"));
            dto.setSessionId(request.getSession().getId());
            
        } catch (IllegalStateException e) {
            // Request context yok, web dışı bir ortam
            log.debug("No request context available");
        }
        
        dto.setAdditionalData(additionalData != null ? additionalData : new HashMap<>());
        
        return dto;
    }
    
    private Map<String, Object> enhanceWithHttpHeaders(Map<String, Object> existingData) {
        Map<String, Object> enhancedData = existingData != null ? new HashMap<>(existingData) : new HashMap<>();
        
        try {
            ServletRequestAttributes attr = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
            HttpServletRequest request = attr.getRequest();
            
            // HTTP Headers'ı additional data'ya ekle
            Map<String, Object> httpHeaders = new HashMap<>();
            
            // Yaygın header'ları ekle
            addHeaderIfPresent(httpHeaders, request, "Accept");
            addHeaderIfPresent(httpHeaders, request, "Accept-Language");
            addHeaderIfPresent(httpHeaders, request, "Accept-Encoding");
            addHeaderIfPresent(httpHeaders, request, "Content-Type");
            addHeaderIfPresent(httpHeaders, request, "Content-Length");
            addHeaderIfPresent(httpHeaders, request, "Authorization");
            addHeaderIfPresent(httpHeaders, request, "X-Forwarded-For");
            addHeaderIfPresent(httpHeaders, request, "X-Real-IP");
            addHeaderIfPresent(httpHeaders, request, "X-Forwarded-Proto");
            addHeaderIfPresent(httpHeaders, request, "Origin");
            addHeaderIfPresent(httpHeaders, request, "Referer");
            addHeaderIfPresent(httpHeaders, request, "Host");
            addHeaderIfPresent(httpHeaders, request, "Connection");
            addHeaderIfPresent(httpHeaders, request, "Cache-Control");
            
            // Custom header'ları ekle (X- ile başlayan)
            request.getHeaderNames().asIterator().forEachRemaining(headerName -> {
                if (headerName.toLowerCase().startsWith("x-") && 
                    !httpHeaders.containsKey(headerName)) {
                    addHeaderIfPresent(httpHeaders, request, headerName);
                }
            });
            
            enhancedData.put("httpHeaders", httpHeaders);
            
            // Request parametreleri
            if (!request.getParameterMap().isEmpty()) {
                enhancedData.put("requestParameters", new HashMap<>(request.getParameterMap()));
            }
            
            // Remote address bilgileri
            enhancedData.put("remoteAddress", request.getRemoteAddr());
            enhancedData.put("remoteHost", request.getRemoteHost());
            enhancedData.put("remotePort", request.getRemotePort());
            
        } catch (IllegalStateException e) {
            // Request context yok, web dışı bir ortam
            log.debug("No HTTP request context available for header extraction");
        }
        
        return enhancedData;
    }
    
    private void addHeaderIfPresent(Map<String, Object> headers, HttpServletRequest request, String headerName) {
        String headerValue = request.getHeader(headerName);
        if (headerValue != null && !headerValue.trim().isEmpty()) {
            // Authorization header'ı güvenlik için mask'le
            if ("Authorization".equalsIgnoreCase(headerName)) {
                headers.put(headerName, "***MASKED***");
            } else {
                headers.put(headerName, headerValue);
            }
        }
    }
    
    private String getStackTrace(Exception exception) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        exception.printStackTrace(pw);
        return sw.toString();
    }
}