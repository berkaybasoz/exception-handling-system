package com.example.demo.controller;

import com.example.demo.service.DemoService;
import com.example.exception.handler.config.ExceptionHandlerProperties;
import com.example.exception.handler.service.ExceptionHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class DemoController {
    
    private final DemoService demoService;
    private final ExceptionHandler exceptionHandler;
    private final ExceptionHandlerProperties properties;
    
    @GetMapping("/hello")
    public ResponseEntity<String> hello() {
        return ResponseEntity.ok("Hello from Demo Application!");
    }
    
    @GetMapping("/config")
    public ResponseEntity<Map<String, Object>> getConfig() {
        Map<String, Object> config = new HashMap<>();
        config.put("applicationName", "demo-app");
        config.put("version", "1.2.0");
        config.put("projectName", properties.getProjectName());
        config.put("componentName", properties.getComponentName());
        config.put("podName", properties.getPodName());
        config.put("podIp", properties.getPodIp());
        config.put("clusterName", properties.getClusterName());
        config.put("environment", properties.getEnvironment());
        config.put("kafkaTopic", properties.getKafka().getTopic());
        config.put("kafkaBootstrapServers", properties.getKafka().getBootstrapServers());
        return ResponseEntity.ok(config);
    }
    
    @GetMapping("/throw-exception")
    public ResponseEntity<String> throwException(@RequestParam(defaultValue = "runtime") String type) {
        try {
            demoService.throwException(type);
            return ResponseEntity.ok("No exception thrown");
        } catch (Exception e) {
            // Exception handler library kullanımı
            Map<String, Object> additionalData = new HashMap<>();
            additionalData.put("requestType", type);
            additionalData.put("endpoint", "/throw-exception");
            
            exceptionHandler.handle(e, additionalData);
            
            return ResponseEntity.internalServerError()
                .body("Exception occurred and sent to monitoring system");
        }
    }
    
    @PostMapping("/user/{id}")
    public ResponseEntity<String> processUser(@PathVariable Long id, @RequestBody Map<String, Object> userData) {
        try {
            demoService.processUser(id, userData);
            return ResponseEntity.ok("User processed successfully");
        } catch (Exception e) {
            Map<String, Object> additionalData = new HashMap<>();
            additionalData.put("userId", id);
            additionalData.put("userData", userData);
            additionalData.put("operation", "processUser");
            
            exceptionHandler.handle(e, additionalData);
            
            return ResponseEntity.badRequest()
                .body("User processing failed - error logged to monitoring system");
        }
    }
    
    @GetMapping("/database-error")
    public ResponseEntity<String> databaseError() {
        try {
            demoService.simulateDatabaseError();
            return ResponseEntity.ok("Database operation completed");
        } catch (Exception e) {
            exceptionHandler.handle(e);
            return ResponseEntity.internalServerError()
                .body("Database error occurred and logged");
        }
    }
    
    @GetMapping("/validation-error")
    public ResponseEntity<String> validationError(@RequestParam String email) {
        try {
            demoService.validateEmail(email);
            return ResponseEntity.ok("Email is valid");
        } catch (Exception e) {
            Map<String, Object> additionalData = new HashMap<>();
            additionalData.put("email", email);
            additionalData.put("validationType", "email");
            
            exceptionHandler.handle(e, additionalData);
            
            return ResponseEntity.badRequest()
                .body("Validation failed - error logged");
        }
    }
    
    @GetMapping("/throw-exception-with-headers")
    public ResponseEntity<String> throwExceptionWithHeaders(@RequestParam(defaultValue = "runtime") String type) {
        try {
            demoService.throwException(type);
            return ResponseEntity.ok("No exception thrown");
        } catch (Exception e) {
            // HTTP headers ile exception handler kullanımı
            Map<String, Object> additionalData = new HashMap<>();
            additionalData.put("requestType", type);
            additionalData.put("endpoint", "/throw-exception-with-headers");
            additionalData.put("testData", "This exception includes HTTP headers automatically");
            
            exceptionHandler.handleWithHttpHeaders(e, additionalData);
            
            return ResponseEntity.internalServerError()
                .body("Exception occurred with HTTP headers and sent to monitoring system");
        }
    }
}