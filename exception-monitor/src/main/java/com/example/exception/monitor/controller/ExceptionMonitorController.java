package com.example.exception.monitor.controller;

import com.example.exception.monitor.entity.ExceptionRecord;
import com.example.exception.monitor.service.ExceptionRecordService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
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
                if (record.getAdditionalData() != null && !record.getAdditionalData().trim().isEmpty()) {
                    JsonNode additionalDataJson = objectMapper.readTree(record.getAdditionalData());
                    model.addAttribute("additionalDataJson", additionalDataJson);
                    
                    // HTTP headers'ı ayrı olarak ekle
                    if (additionalDataJson.has("httpHeaders")) {
                        model.addAttribute("httpHeaders", additionalDataJson.get("httpHeaders"));
                    }
                    
                    // Request parameters'ı ayrı olarak ekle
                    if (additionalDataJson.has("requestParameters")) {
                        model.addAttribute("requestParameters", additionalDataJson.get("requestParameters"));
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
                // JSON parse hatası durumunda raw data'yı göster
                model.addAttribute("additionalDataParseError", true);
            }
            
            return "exception-detail";
        } else {
            return "redirect:/exceptions";
        }
    }
}