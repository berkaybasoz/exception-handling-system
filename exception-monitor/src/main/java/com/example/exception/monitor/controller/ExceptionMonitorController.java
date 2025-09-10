package com.example.exception.monitor.controller;

import com.example.exception.monitor.entity.ExceptionRecord;
import com.example.exception.monitor.service.ExceptionRecordService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@Controller
@RequiredArgsConstructor
public class ExceptionMonitorController {
    
    private final ExceptionRecordService exceptionRecordService;
    private final ObjectMapper objectMapper;
    
    @Value("${spring.application.version:1.0.0}")
    private String applicationVersion;
    
    @GetMapping("/")
    public String dashboard(
            @RequestParam(required = false) String timeRange,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime customStartDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime customEndDate,
            Model model) {
        
        // Application version
        model.addAttribute("applicationVersion", applicationVersion);
        
        // Calculate date range based on timeRange parameter
        LocalDateTime startDate = null;
        LocalDateTime endDate = LocalDateTime.now();
        
        if (timeRange != null && !timeRange.isEmpty()) {
            switch (timeRange) {
                case "5m":
                    startDate = endDate.minusMinutes(5);
                    break;
                case "15m":
                    startDate = endDate.minusMinutes(15);
                    break;
                case "30m":
                    startDate = endDate.minusMinutes(30);
                    break;
                case "1h":
                    startDate = endDate.minusHours(1);
                    break;
                case "6h":
                    startDate = endDate.minusHours(6);
                    break;
                case "12h":
                    startDate = endDate.minusHours(12);
                    break;
                case "1d":
                    startDate = endDate.minusDays(1);
                    break;
                case "7d":
                    startDate = endDate.minusDays(7);
                    break;
                case "30d":
                    startDate = endDate.minusDays(30);
                    break;
                case "custom":
                    if (customStartDate != null && customEndDate != null) {
                        startDate = customStartDate;
                        endDate = customEndDate;
                    }
                    break;
                default:
                    // Default to last 24 hours
                    startDate = endDate.minusHours(24);
            }
        } else {
            // Default to last 24 hours
            startDate = endDate.minusHours(24);
        }
        
        // Dashboard statistics
        model.addAttribute("totalExceptions", exceptionRecordService.getTotalExceptions());
        model.addAttribute("exceptionsLast24h", exceptionRecordService.getExceptionsInLast24Hours());
        model.addAttribute("exceptionsLastHour", exceptionRecordService.getExceptionsInLastHour());
        model.addAttribute("exceptionsInRange", exceptionRecordService.getExceptionsByTimeRange(startDate, endDate));
        model.addAttribute("selectedTimeRange", timeRange != null ? timeRange : "24h");
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);
        
        // Top exception types (with time filtering)
        List<Object[]> exceptionTypeStats = exceptionRecordService.getExceptionTypeStatistics(startDate, endDate);
        model.addAttribute("exceptionTypeStats", exceptionTypeStats);
        
        // Top projects (with time filtering)
        List<Object[]> projectStats = exceptionRecordService.getProjectStatistics(startDate, endDate);
        model.addAttribute("projectStats", projectStats);
        
        // Component statistics (with time filtering)
        List<Object[]> componentStats = exceptionRecordService.getComponentStatistics(startDate, endDate);
        model.addAttribute("componentStats", componentStats);
        
        // Environment statistics (with time filtering)
        List<Object[]> environmentStats = exceptionRecordService.getEnvironmentStatistics(startDate, endDate);
        model.addAttribute("environmentStats", environmentStats);
        
        // Recent exceptions
        Page<ExceptionRecord> recentExceptions = exceptionRecordService.findAll(PageRequest.of(0, 10));
        model.addAttribute("recentExceptions", recentExceptions.getContent());
        
        return "dashboard";
    }
    
    @GetMapping("/exceptions")
    public String listExceptions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String projectName,
            @RequestParam(required = false) String exceptionType,
            @RequestParam(required = false) String environment,
            @RequestParam(required = false) String componentName,
            @RequestParam(required = false) String serviceName,
            @RequestParam(required = false) String method,
            @RequestParam(required = false) String advancedQuery,
            @RequestParam(required = false) String timeRange,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime customStartDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime customEndDate,
            Model model) {
        
        // Calculate date range based on timeRange parameter
        LocalDateTime startDate = null;
        LocalDateTime endDate = LocalDateTime.now();
        
        if (timeRange != null && !timeRange.isEmpty()) {
            switch (timeRange) {
                case "5m":
                    startDate = endDate.minusMinutes(5);
                    break;
                case "15m":
                    startDate = endDate.minusMinutes(15);
                    break;
                case "30m":
                    startDate = endDate.minusMinutes(30);
                    break;
                case "1h":
                    startDate = endDate.minusHours(1);
                    break;
                case "6h":
                    startDate = endDate.minusHours(6);
                    break;
                case "12h":
                    startDate = endDate.minusHours(12);
                    break;
                case "1d":
                    startDate = endDate.minusDays(1);
                    break;
                case "7d":
                    startDate = endDate.minusDays(7);
                    break;
                case "30d":
                    startDate = endDate.minusDays(30);
                    break;
                case "custom":
                    if (customStartDate != null && customEndDate != null) {
                        startDate = customStartDate;
                        endDate = customEndDate;
                    }
                    break;
            }
        }
        
        Pageable pageable = PageRequest.of(page, size);
        Page<ExceptionRecord> exceptions;
        
        // Use advanced query if provided, otherwise use standard filters
        if (advancedQuery != null && !advancedQuery.trim().isEmpty()) {
            exceptions = exceptionRecordService.findWithAdvancedQuery(advancedQuery, projectName, exceptionType, 
                                                                    environment, componentName, serviceName, method,
                                                                    startDate, endDate, pageable);
        } else {
            exceptions = exceptionRecordService.findWithAllFilters(projectName, exceptionType, environment, 
                                                                 componentName, serviceName, method,
                                                                 null, null,
                                                                 startDate, endDate, pageable);
        }
        
        // Get distinct values for filter dropdowns
        model.addAttribute("distinctProjects", exceptionRecordService.getDistinctProjects());
        model.addAttribute("distinctExceptionTypes", exceptionRecordService.getDistinctExceptionTypes());
        model.addAttribute("distinctEnvironments", exceptionRecordService.getDistinctEnvironments());
        model.addAttribute("distinctComponents", exceptionRecordService.getDistinctComponents());
        model.addAttribute("distinctServices", exceptionRecordService.getDistinctServices());
        model.addAttribute("distinctMethods", exceptionRecordService.getDistinctMethods());
        
        model.addAttribute("exceptions", exceptions);
        model.addAttribute("currentPage", page);
        model.addAttribute("projectName", projectName);
        model.addAttribute("exceptionType", exceptionType);
        model.addAttribute("environment", environment);
        model.addAttribute("componentName", componentName);
        model.addAttribute("serviceName", serviceName);
        model.addAttribute("method", method);
        model.addAttribute("advancedQuery", advancedQuery);
        model.addAttribute("selectedTimeRange", timeRange != null ? timeRange : "all");
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);
        model.addAttribute("applicationVersion", applicationVersion);
        
        return "exceptions";
    }
    
    @GetMapping("/exceptions/{id}")
    public String exceptionDetail(@PathVariable String id, Model model) {
        Optional<ExceptionRecord> exception = exceptionRecordService.findById(id);
        
        if (exception.isPresent()) {
            ExceptionRecord record = exception.get();
            model.addAttribute("exception", record);
            
            // Additional data'yı parse et
            try {
                log.info("Processing exception detail for ID: {}", id);
                log.info("Additional data: {}", record.getAdditionalData());
                
                if (record.getAdditionalData() != null && !record.getAdditionalData().trim().isEmpty()) {
                    JsonNode additionalDataJson = objectMapper.readTree(record.getAdditionalData());
                    model.addAttribute("additionalDataJson", additionalDataJson);
                    
                    // HTTP headers'ı ayrı olarak ekle
                    if (additionalDataJson.has("httpHeaders")) {
                        JsonNode httpHeaders = additionalDataJson.get("httpHeaders");
                        log.info("Found HTTP headers: {}", httpHeaders);
                        model.addAttribute("httpHeaders", httpHeaders);
                    }
                    
                    // Request parameters'ı ayrı olarak ekle
                    if (additionalDataJson.has("requestParameters")) {
                        JsonNode requestParams = additionalDataJson.get("requestParameters");
                        log.info("Found request parameters: {}", requestParams);
                        model.addAttribute("requestParameters", requestParams);
                    }
                    
                    // Remote info'yu ayrı olarak ekle
                    if (additionalDataJson.has("remoteAddress")) {
                        model.addAttribute("remoteAddress", additionalDataJson.get("remoteAddress").asText());
                    }
                    if (additionalDataJson.has("remoteHost")) {
                        model.addAttribute("remoteHost", additionalDataJson.get("remoteHost").asText());
                    }
                    if (additionalDataJson.has("remotePort")) {
                        model.addAttribute("remotePort", additionalDataJson.get("remotePort").asText());
                    }
                }
            } catch (Exception e) {
                log.error("Error parsing additional data for exception {}: {}", id, e.getMessage());
                model.addAttribute("additionalDataParseError", true);
            }
            
            model.addAttribute("applicationVersion", applicationVersion);
            return "exception-detail";
        } else {
            return "redirect:/exceptions";
        }
    }
    
    @GetMapping("/api/exceptions/{id}")
    @org.springframework.web.bind.annotation.ResponseBody
    public ExceptionRecord getExceptionJson(@PathVariable String id) {
        Optional<ExceptionRecord> exception = exceptionRecordService.findById(id);
        return exception.orElse(null);
    }
    
    @GetMapping("/components")
    public String components(
            @RequestParam(required = false) String timeRange,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime customStartDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime customEndDate,
            Model model) {
        
        // Calculate date range based on timeRange parameter
        LocalDateTime startDate = calculateStartDate(timeRange, customStartDate, customEndDate);
        LocalDateTime endDate = calculateEndDate(timeRange, customStartDate, customEndDate);
        
        // Component statistics (with time filtering)
        List<Object[]> componentStats = exceptionRecordService.getComponentStatistics(startDate, endDate);
        model.addAttribute("componentStats", componentStats);
        
        // Get component breakdown by pods (with time filtering)
        if (!componentStats.isEmpty()) {
            String topComponent = (String) componentStats.get(0)[0];
            List<Object[]> componentPods = exceptionRecordService.getPodsByComponent(topComponent, startDate, endDate);
            model.addAttribute("componentPods", componentPods);
            model.addAttribute("selectedComponent", topComponent);
        }
        
        // Time range attributes
        model.addAttribute("selectedTimeRange", timeRange != null ? timeRange : "all");
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);
        model.addAttribute("applicationVersion", applicationVersion);
        return "components";
    }
    
    @GetMapping("/projects")
    public String projects(
            @RequestParam(required = false) String timeRange,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime customStartDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime customEndDate,
            Model model) {
        
        // Calculate date range based on timeRange parameter
        LocalDateTime startDate = calculateStartDate(timeRange, customStartDate, customEndDate);
        LocalDateTime endDate = calculateEndDate(timeRange, customStartDate, customEndDate);
        
        // Project statistics (with time filtering)
        List<Object[]> projectStats = exceptionRecordService.getProjectStatistics(startDate, endDate);
        model.addAttribute("projectStats", projectStats);
        
        // Get projects by environment (with time filtering)
        List<Object[]> projectsByEnv = new java.util.ArrayList<>();
        for (String env : new String[]{"UAT", "INT", "PROD"}) {
            List<Object[]> envProjects = exceptionRecordService.getProjectsByEnvironment(env, startDate, endDate);
            if (!envProjects.isEmpty()) {
                projectsByEnv.add(new Object[]{env, envProjects});
            }
        }
        model.addAttribute("projectsByEnvironment", projectsByEnv);
        
        // Time range attributes
        model.addAttribute("selectedTimeRange", timeRange != null ? timeRange : "all");
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);
        model.addAttribute("applicationVersion", applicationVersion);
        
        return "projects";
    }
    
    @GetMapping("/environments")
    public String environments(
            @RequestParam(required = false) String timeRange,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime customStartDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime customEndDate,
            Model model) {
        
        // Calculate date range based on timeRange parameter
        LocalDateTime startDate = calculateStartDate(timeRange, customStartDate, customEndDate);
        LocalDateTime endDate = calculateEndDate(timeRange, customStartDate, customEndDate);
        
        // Environment statistics (with time filtering)
        List<Object[]> environmentStats = exceptionRecordService.getEnvironmentStatistics(startDate, endDate);
        model.addAttribute("environmentStats", environmentStats);
        
        // Get components by environment (with time filtering)
        List<Object[]> componentsByEnv = new java.util.ArrayList<>();
        for (Object[] envStat : environmentStats) {
            String env = (String) envStat[0];
            List<Object[]> envComponents = exceptionRecordService.getComponentsByEnvironment(env, startDate, endDate);
            componentsByEnv.add(new Object[]{env, envComponents});
        }
        model.addAttribute("componentsByEnvironment", componentsByEnv);
        
        // Time range attributes
        model.addAttribute("selectedTimeRange", timeRange != null ? timeRange : "all");
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);
        model.addAttribute("applicationVersion", applicationVersion);
        
        return "environments";
    }
    
    // Helper methods for date range calculation
    private LocalDateTime calculateStartDate(String timeRange, LocalDateTime customStartDate, LocalDateTime customEndDate) {
        LocalDateTime endDate = LocalDateTime.now();
        
        if (timeRange != null && !timeRange.isEmpty()) {
            switch (timeRange) {
                case "5m":
                    return endDate.minusMinutes(5);
                case "15m":
                    return endDate.minusMinutes(15);
                case "30m":
                    return endDate.minusMinutes(30);
                case "1h":
                    return endDate.minusHours(1);
                case "6h":
                    return endDate.minusHours(6);
                case "12h":
                    return endDate.minusHours(12);
                case "1d":
                    return endDate.minusDays(1);
                case "7d":
                    return endDate.minusDays(7);
                case "30d":
                    return endDate.minusDays(30);
                case "custom":
                    return customStartDate;
                default:
                    return null;
            }
        }
        return null;
    }
    
    private LocalDateTime calculateEndDate(String timeRange, LocalDateTime customStartDate, LocalDateTime customEndDate) {
        if (timeRange != null && timeRange.equals("custom")) {
            return customEndDate;
        }
        return LocalDateTime.now();
    }
}