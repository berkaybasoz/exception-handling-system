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
    
    // Distinct values for filters
    public List<String> getDistinctProjects() {
        return repository.findDistinctProjectNames();
    }
    
    public List<String> getDistinctExceptionTypes() {
        return repository.findDistinctExceptionTypes();
    }
    
    public List<String> getDistinctEnvironments() {
        return repository.findDistinctEnvironments();
    }
    
    public List<String> getDistinctComponents() {
        return repository.findDistinctComponentNames();
    }
    
    public List<String> getDistinctServices() {
        return repository.findDistinctServiceNames();
    }
    
    public List<String> getDistinctMethods() {
        return repository.findDistinctMethods();
    }
    
    // Time range based exception count
    public Long getExceptionsByTimeRange(LocalDateTime startDate, LocalDateTime endDate) {
        if (startDate == null || endDate == null) {
            return 0L;
        }
        return repository.countByTimestampBetween(startDate, endDate);
    }
    
    // Filtered search with service, method and request headers
    public Page<ExceptionRecord> findWithAllFilters(String projectName, String exceptionType, 
                                                   String environment, String componentName,
                                                   String serviceName, String method,
                                                   String requestHeaderFilter, String headerFilterType,
                                                   LocalDateTime startDate, LocalDateTime endDate, 
                                                   Pageable pageable) {
        
        String normalizedProjectName = (projectName != null && projectName.trim().isEmpty()) ? null : projectName;
        String normalizedExceptionType = (exceptionType != null && exceptionType.trim().isEmpty()) ? null : exceptionType;
        String normalizedEnvironment = (environment != null && environment.trim().isEmpty()) ? null : environment;
        String normalizedComponentName = (componentName != null && componentName.trim().isEmpty()) ? null : componentName;
        String normalizedServiceName = (serviceName != null && serviceName.trim().isEmpty()) ? null : serviceName;
        String normalizedMethod = (method != null && method.trim().isEmpty()) ? null : method;
        String normalizedHeaderFilter = (requestHeaderFilter != null && requestHeaderFilter.trim().isEmpty()) ? null : requestHeaderFilter;
        
        return repository.findWithAllFiltersIncludingHeaders(normalizedProjectName, normalizedExceptionType, 
                                           normalizedEnvironment, normalizedComponentName,
                                           normalizedServiceName, normalizedMethod,
                                           normalizedHeaderFilter, headerFilterType,
                                           startDate, endDate, pageable);
    }
    
    // Advanced query search
    public Page<ExceptionRecord> findWithAdvancedQuery(String advancedQuery, String projectName, String exceptionType,
                                                      String environment, String componentName, String serviceName,
                                                      String method, LocalDateTime startDate, LocalDateTime endDate,
                                                      Pageable pageable) {
        
        String normalizedProjectName = (projectName != null && projectName.trim().isEmpty()) ? null : projectName;
        String normalizedExceptionType = (exceptionType != null && exceptionType.trim().isEmpty()) ? null : exceptionType;
        String normalizedEnvironment = (environment != null && environment.trim().isEmpty()) ? null : environment;
        String normalizedComponentName = (componentName != null && componentName.trim().isEmpty()) ? null : componentName;
        String normalizedServiceName = (serviceName != null && serviceName.trim().isEmpty()) ? null : serviceName;
        String normalizedMethod = (method != null && method.trim().isEmpty()) ? null : method;
        
        return repository.findWithAdvancedQuery(advancedQuery, normalizedProjectName, normalizedExceptionType,
                                               normalizedEnvironment, normalizedComponentName, normalizedServiceName,
                                               normalizedMethod, startDate, endDate, pageable);
    }
    
    // Time-filtered statistics methods
    public List<Object[]> getExceptionTypeStatistics(LocalDateTime startDate, LocalDateTime endDate) {
        if (startDate != null || endDate != null) {
            return repository.getExceptionTypeStatisticsByTimeRange(startDate, endDate);
        }
        return repository.getExceptionTypeStatistics();
    }
    
    public List<Object[]> getProjectStatistics(LocalDateTime startDate, LocalDateTime endDate) {
        if (startDate != null || endDate != null) {
            return repository.getProjectStatisticsByTimeRange(startDate, endDate);
        }
        return repository.getProjectStatistics();
    }
    
    public List<Object[]> getComponentStatistics(LocalDateTime startDate, LocalDateTime endDate) {
        if (startDate != null || endDate != null) {
            return repository.getComponentStatisticsByTimeRange(startDate, endDate);
        }
        return repository.getComponentStatistics();
    }
    
    public List<Object[]> getEnvironmentStatistics(LocalDateTime startDate, LocalDateTime endDate) {
        if (startDate != null || endDate != null) {
            return repository.getEnvironmentStatisticsByTimeRange(startDate, endDate);
        }
        return repository.getEnvironmentStatistics();
    }
    
    public List<Object[]> getComponentsByEnvironment(String environment, LocalDateTime startDate, LocalDateTime endDate) {
        if (startDate != null || endDate != null) {
            return repository.getComponentsByEnvironmentAndTimeRange(environment, startDate, endDate);
        }
        return repository.getComponentsByEnvironment(environment);
    }
    
    public List<Object[]> getPodsByComponent(String componentName, LocalDateTime startDate, LocalDateTime endDate) {
        if (startDate != null || endDate != null) {
            return repository.getPodsByComponentAndTimeRange(componentName, startDate, endDate);
        }
        return repository.getPodsByComponent(componentName);
    }
    
    public List<Object[]> getProjectsByEnvironment(String environment, LocalDateTime startDate, LocalDateTime endDate) {
        if (startDate != null || endDate != null) {
            return repository.getProjectsByEnvironmentAndTimeRange(environment, startDate, endDate);
        }
        return repository.getProjectsByEnvironment(environment);
    }
}