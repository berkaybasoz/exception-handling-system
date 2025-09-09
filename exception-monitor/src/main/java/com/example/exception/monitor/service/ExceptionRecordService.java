package com.example.exception.monitor.service;

import com.example.exception.monitor.entity.ExceptionRecord;
import com.example.exception.monitor.repository.ExceptionRecordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExceptionRecordService {
    
    private final ExceptionRecordRepository repository;
    
    public ExceptionRecord save(ExceptionRecord record) {
        log.debug("Saving exception record: {}", record.getId());
        return repository.save(record);
    }
    
    public Page<ExceptionRecord> findAll(Pageable pageable) {
        return repository.findAllByOrderByTimestampDesc(pageable);
    }
    
    public Page<ExceptionRecord> findByProject(String projectName, Pageable pageable) {
        return repository.findByProjectNameOrderByTimestampDesc(projectName, pageable);
    }
    
    public Page<ExceptionRecord> findByExceptionType(String exceptionType, Pageable pageable) {
        return repository.findByExceptionTypeOrderByTimestampDesc(exceptionType, pageable);
    }
    
    public Page<ExceptionRecord> findByDateRange(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable) {
        return repository.findByTimestampBetweenOrderByTimestampDesc(startDate, endDate, pageable);
    }
    
    public Page<ExceptionRecord> findByEnvironment(String environment, Pageable pageable) {
        return repository.findByEnvironmentOrderByTimestampDesc(environment, pageable);
    }
    
    public Page<ExceptionRecord> findByComponentName(String componentName, Pageable pageable) {
        return repository.findByComponentNameOrderByTimestampDesc(componentName, pageable);
    }
    
    public Page<ExceptionRecord> findWithFilters(String projectName, String exceptionType, 
                                               String environment, String componentName,
                                               LocalDateTime startDate, LocalDateTime endDate, 
                                               Pageable pageable) {
        
        String normalizedProjectName = (projectName != null && projectName.trim().isEmpty()) ? null : projectName;
        String normalizedExceptionType = (exceptionType != null && exceptionType.trim().isEmpty()) ? null : exceptionType;
        String normalizedEnvironment = (environment != null && environment.trim().isEmpty()) ? null : environment;
        String normalizedComponentName = (componentName != null && componentName.trim().isEmpty()) ? null : componentName;
        
        return repository.findWithFilters(normalizedProjectName, normalizedExceptionType, 
                                        normalizedEnvironment, normalizedComponentName,
                                        startDate, endDate, pageable);
    }
    
    public Optional<ExceptionRecord> findById(String id) {
        return repository.findById(id);
    }
    
    public List<Object[]> getExceptionTypeStatistics() {
        return repository.getExceptionTypeStatistics();
    }
    
    public List<Object[]> getProjectStatistics() {
        return repository.getProjectStatistics();
    }
    
    public Long getTotalExceptions() {
        return repository.count();
    }
    
    public Long getExceptionsInLast24Hours() {
        LocalDateTime since = LocalDateTime.now().minusHours(24);
        return repository.countExceptionsSince(since);
    }
    
    public Long getExceptionsInLastHour() {
        LocalDateTime since = LocalDateTime.now().minusHours(1);
        return repository.countExceptionsSince(since);
    }
    
    public List<Object[]> getComponentStatistics() {
        return repository.getComponentStatistics();
    }
    
    public List<Object[]> getEnvironmentStatistics() {
        return repository.getEnvironmentStatistics();
    }
    
    public List<Object[]> getComponentsByEnvironment(String environment) {
        return repository.getComponentsByEnvironment(environment);
    }
    
    public List<Object[]> getPodsByComponent(String componentName) {
        return repository.getPodsByComponent(componentName);
    }
    
    public List<Object[]> getProjectsByEnvironment(String environment) {
        return repository.getProjectsByEnvironment(environment);
    }
}