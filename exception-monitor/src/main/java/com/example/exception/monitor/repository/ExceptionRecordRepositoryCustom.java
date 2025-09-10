package com.example.exception.monitor.repository;

import com.example.exception.monitor.entity.ExceptionRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;

public interface ExceptionRecordRepositoryCustom {
    
    Page<ExceptionRecord> findWithAdvancedQuery(
        String advancedQuery,
        String projectName,
        String exceptionType,
        String environment,
        String componentName,
        String serviceName,
        String method,
        LocalDateTime startDate,
        LocalDateTime endDate,
        Pageable pageable
    );
}