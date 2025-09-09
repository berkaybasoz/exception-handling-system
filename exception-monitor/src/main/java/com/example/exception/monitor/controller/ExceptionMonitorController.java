package com.example.exception.monitor.controller;

import com.example.exception.monitor.entity.ExceptionRecord;
import com.example.exception.monitor.service.ExceptionRecordService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
    
    @GetMapping("/")
    public String dashboard(Model model) {
        // Dashboard statistics
        model.addAttribute("totalExceptions", exceptionRecordService.getTotalExceptions());
        model.addAttribute("exceptionsLast24h", exceptionRecordService.getExceptionsInLast24Hours());
        model.addAttribute("exceptionsLastHour", exceptionRecordService.getExceptionsInLastHour());
        
        // Top exception types
        List<Object[]> exceptionTypeStats = exceptionRecordService.getExceptionTypeStatistics();
        model.addAttribute("exceptionTypeStats", exceptionTypeStats);
        
        // Top projects
        List<Object[]> projectStats = exceptionRecordService.getProjectStatistics();
        model.addAttribute("projectStats", projectStats);
        
        // Component statistics
        List<Object[]> componentStats = exceptionRecordService.getComponentStatistics();
        model.addAttribute("componentStats", componentStats);
        
        // Environment statistics
        List<Object[]> environmentStats = exceptionRecordService.getEnvironmentStatistics();
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
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            Model model) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<ExceptionRecord> exceptions;
        
        if (projectName != null && !projectName.trim().isEmpty()) {
            exceptions = exceptionRecordService.findByProject(projectName.trim(), pageable);
        } else if (exceptionType != null && !exceptionType.trim().isEmpty()) {
            exceptions = exceptionRecordService.findByExceptionType(exceptionType.trim(), pageable);
        } else if (startDate != null && endDate != null) {
            exceptions = exceptionRecordService.findByDateRange(startDate, endDate, pageable);
        } else {
            exceptions = exceptionRecordService.findAll(pageable);
        }
        
        model.addAttribute("exceptions", exceptions);
        model.addAttribute("currentPage", page);
        model.addAttribute("projectName", projectName);
        model.addAttribute("exceptionType", exceptionType);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);
        
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
    public String components(Model model) {
        // Component statistics
        List<Object[]> componentStats = exceptionRecordService.getComponentStatistics();
        model.addAttribute("componentStats", componentStats);
        
        // Get component breakdown by pods
        if (!componentStats.isEmpty()) {
            String topComponent = (String) componentStats.get(0)[0];
            List<Object[]> componentPods = exceptionRecordService.getPodsByComponent(topComponent);
            model.addAttribute("componentPods", componentPods);
            model.addAttribute("selectedComponent", topComponent);
        }
        
        return "components";
    }
    
    @GetMapping("/projects")
    public String projects(Model model) {
        // Project statistics
        List<Object[]> projectStats = exceptionRecordService.getProjectStatistics();
        model.addAttribute("projectStats", projectStats);
        
        // Get projects by environment
        List<Object[]> projectsByEnv = new java.util.ArrayList<>();
        for (String env : new String[]{"UAT", "INT", "PROD"}) {
            List<Object[]> envProjects = exceptionRecordService.getProjectsByEnvironment(env);
            if (!envProjects.isEmpty()) {
                projectsByEnv.add(new Object[]{env, envProjects});
            }
        }
        model.addAttribute("projectsByEnvironment", projectsByEnv);
        
        return "projects";
    }
    
    @GetMapping("/environments")
    public String environments(Model model) {
        // Environment statistics
        List<Object[]> environmentStats = exceptionRecordService.getEnvironmentStatistics();
        model.addAttribute("environmentStats", environmentStats);
        
        // Get components by environment
        List<Object[]> componentsByEnv = new java.util.ArrayList<>();
        for (Object[] envStat : environmentStats) {
            String env = (String) envStat[0];
            List<Object[]> envComponents = exceptionRecordService.getComponentsByEnvironment(env);
            componentsByEnv.add(new Object[]{env, envComponents});
        }
        model.addAttribute("componentsByEnvironment", componentsByEnv);
        
        return "environments";
    }
}