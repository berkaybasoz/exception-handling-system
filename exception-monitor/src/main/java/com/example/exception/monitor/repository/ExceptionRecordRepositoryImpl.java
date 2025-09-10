package com.example.exception.monitor.repository;

import com.example.exception.monitor.entity.ExceptionRecord;
import com.example.exception.monitor.util.QueryParser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Repository
@RequiredArgsConstructor
public class ExceptionRecordRepositoryImpl implements ExceptionRecordRepositoryCustom {
    
    @PersistenceContext
    private EntityManager entityManager;
    
    private final QueryParser queryParser;
    
    @Override
    public Page<ExceptionRecord> findWithAdvancedQuery(
            String advancedQuery,
            String projectName,
            String exceptionType,
            String environment,
            String componentName,
            String serviceName,
            String method,
            LocalDateTime startDate,
            LocalDateTime endDate,
            Pageable pageable) {
        
        StringBuilder jpql = new StringBuilder("SELECT e FROM ExceptionRecord e WHERE 1=1");
        Map<String, Object> params = new HashMap<>();
        
        // Parse and apply advanced query
        if (advancedQuery != null && !advancedQuery.trim().isEmpty()) {
            try {
                QueryParser.ParsedQuery parsed = queryParser.parse(advancedQuery);
                String sqlCondition = parsed.buildSQLCondition();
                
                if (sqlCondition != null && !sqlCondition.isEmpty()) {
                    jpql.append(" AND (").append(sqlCondition).append(")");
                }
            } catch (Exception ex) {
                log.error("Error parsing advanced query: {}", advancedQuery, ex);
                // If parsing fails, treat it as a simple text search in additionalData
                jpql.append(" AND e.additionalData LIKE :queryText");
                params.put("queryText", "%" + advancedQuery + "%");
            }
        }
        
        // Apply standard filters
        if (projectName != null && !projectName.trim().isEmpty()) {
            jpql.append(" AND e.projectName = :projectName");
            params.put("projectName", projectName);
        }
        
        if (exceptionType != null && !exceptionType.trim().isEmpty()) {
            jpql.append(" AND e.exceptionType = :exceptionType");
            params.put("exceptionType", exceptionType);
        }
        
        if (environment != null && !environment.trim().isEmpty()) {
            jpql.append(" AND e.environment = :environment");
            params.put("environment", environment);
        }
        
        if (componentName != null && !componentName.trim().isEmpty()) {
            jpql.append(" AND e.componentName = :componentName");
            params.put("componentName", componentName);
        }
        
        if (serviceName != null && !serviceName.trim().isEmpty()) {
            jpql.append(" AND e.serviceName = :serviceName");
            params.put("serviceName", serviceName);
        }
        
        if (method != null && !method.trim().isEmpty()) {
            jpql.append(" AND e.method = :method");
            params.put("method", method);
        }
        
        // Apply date filters
        if (startDate != null) {
            jpql.append(" AND e.timestamp >= :startDate");
            params.put("startDate", startDate);
        }
        
        if (endDate != null) {
            jpql.append(" AND e.timestamp <= :endDate");
            params.put("endDate", endDate);
        }
        
        // Add ordering
        jpql.append(" ORDER BY e.timestamp DESC");
        
        // Create count query for pagination
        String countJpql = jpql.toString().replace("SELECT e", "SELECT COUNT(e)");
        TypedQuery<Long> countQuery = entityManager.createQuery(countJpql, Long.class);
        
        // Create data query
        TypedQuery<ExceptionRecord> dataQuery = entityManager.createQuery(jpql.toString(), ExceptionRecord.class);
        
        // Set parameters for both queries
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            countQuery.setParameter(entry.getKey(), entry.getValue());
            dataQuery.setParameter(entry.getKey(), entry.getValue());
        }
        
        // Get total count
        Long total = countQuery.getSingleResult();
        
        // Apply pagination
        dataQuery.setFirstResult((int) pageable.getOffset());
        dataQuery.setMaxResults(pageable.getPageSize());
        
        // Get results
        List<ExceptionRecord> results = dataQuery.getResultList();
        
        return new PageImpl<>(results, pageable, total);
    }
}