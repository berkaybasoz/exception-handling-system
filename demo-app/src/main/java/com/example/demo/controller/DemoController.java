package com.example.demo.controller;

import com.example.demo.service.DemoService;
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
    
    @GetMapping("/hello")
    public ResponseEntity<String> hello() {
        return ResponseEntity.ok("Hello from Demo Application!");
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
}