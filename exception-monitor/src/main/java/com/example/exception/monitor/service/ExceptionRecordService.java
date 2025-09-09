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