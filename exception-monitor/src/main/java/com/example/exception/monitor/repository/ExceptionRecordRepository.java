package com.example.exception.monitor.repository;

import com.example.exception.monitor.entity.ExceptionRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ExceptionRecordRepository extends JpaRepository<ExceptionRecord, String>, ExceptionRecordRepositoryCustom {
    
    Page<ExceptionRecord> findAllByOrderByTimestampDesc(Pageable pageable);
    
    Page<ExceptionRecord> findByProjectNameOrderByTimestampDesc(String projectName, Pageable pageable);
    
    Page<ExceptionRecord> findByExceptionTypeOrderByTimestampDesc(String exceptionType, Pageable pageable);
    
    Page<ExceptionRecord> findByTimestampBetweenOrderByTimestampDesc(
        LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);
    
    Page<ExceptionRecord> findByEnvironmentOrderByTimestampDesc(String environment, Pageable pageable);
    
    Page<ExceptionRecord> findByComponentNameOrderByTimestampDesc(String componentName, Pageable pageable);
    
    @Query("SELECT e FROM ExceptionRecord e WHERE " +
           "(:projectName IS NULL OR e.projectName = :projectName) AND " +
           "(:exceptionType IS NULL OR e.exceptionType = :exceptionType) AND " +
           "(:environment IS NULL OR e.environment = :environment) AND " +
           "(:componentName IS NULL OR e.componentName = :componentName) AND " +
           "(:startDate IS NULL OR e.timestamp >= :startDate) AND " +
           "(:endDate IS NULL OR e.timestamp <= :endDate) " +
           "ORDER BY e.timestamp DESC")
    Page<ExceptionRecord> findWithFilters(
        @Param("projectName") String projectName,
        @Param("exceptionType") String exceptionType,
        @Param("environment") String environment,
        @Param("componentName") String componentName,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate,
        Pageable pageable);
    
    @Query("SELECT e.exceptionType, COUNT(e) FROM ExceptionRecord e GROUP BY e.exceptionType ORDER BY COUNT(e) DESC")
    List<Object[]> getExceptionTypeStatistics();
    
    @Query("SELECT e.projectName, COUNT(e) FROM ExceptionRecord e GROUP BY e.projectName ORDER BY COUNT(e) DESC")
    List<Object[]> getProjectStatistics();
    
    @Query("SELECT COUNT(e) FROM ExceptionRecord e WHERE e.timestamp >= :since")
    Long countExceptionsSince(@Param("since") LocalDateTime since);
    
    @Query("SELECT e.componentName, COUNT(e) FROM ExceptionRecord e GROUP BY e.componentName ORDER BY COUNT(e) DESC")
    List<Object[]> getComponentStatistics();
    
    @Query("SELECT e.environment, COUNT(e) FROM ExceptionRecord e GROUP BY e.environment ORDER BY COUNT(e) DESC")
    List<Object[]> getEnvironmentStatistics();
    
    @Query("SELECT e.componentName, COUNT(e) FROM ExceptionRecord e WHERE e.environment = :environment GROUP BY e.componentName ORDER BY COUNT(e) DESC")
    List<Object[]> getComponentsByEnvironment(@Param("environment") String environment);
    
    @Query("SELECT e.podName, e.podIp, COUNT(e) FROM ExceptionRecord e WHERE e.componentName = :componentName GROUP BY e.podName, e.podIp ORDER BY COUNT(e) DESC")
    List<Object[]> getPodsByComponent(@Param("componentName") String componentName);
    
    @Query("SELECT e.projectName, COUNT(e) FROM ExceptionRecord e WHERE e.environment = :environment GROUP BY e.projectName ORDER BY COUNT(e) DESC")
    List<Object[]> getProjectsByEnvironment(@Param("environment") String environment);
    
    // Time-based statistics queries
    @Query("SELECT e.exceptionType, COUNT(e) FROM ExceptionRecord e WHERE " +
           "(:startDate IS NULL OR e.timestamp >= :startDate) AND " +
           "(:endDate IS NULL OR e.timestamp <= :endDate) " +
           "GROUP BY e.exceptionType ORDER BY COUNT(e) DESC")
    List<Object[]> getExceptionTypeStatisticsByTimeRange(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT e.projectName, COUNT(e) FROM ExceptionRecord e WHERE " +
           "(:startDate IS NULL OR e.timestamp >= :startDate) AND " +
           "(:endDate IS NULL OR e.timestamp <= :endDate) " +
           "GROUP BY e.projectName ORDER BY COUNT(e) DESC")
    List<Object[]> getProjectStatisticsByTimeRange(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT e.componentName, COUNT(e) FROM ExceptionRecord e WHERE " +
           "(:startDate IS NULL OR e.timestamp >= :startDate) AND " +
           "(:endDate IS NULL OR e.timestamp <= :endDate) " +
           "GROUP BY e.componentName ORDER BY COUNT(e) DESC")
    List<Object[]> getComponentStatisticsByTimeRange(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT e.environment, COUNT(e) FROM ExceptionRecord e WHERE " +
           "(:startDate IS NULL OR e.timestamp >= :startDate) AND " +
           "(:endDate IS NULL OR e.timestamp <= :endDate) " +
           "GROUP BY e.environment ORDER BY COUNT(e) DESC")
    List<Object[]> getEnvironmentStatisticsByTimeRange(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT e.componentName, COUNT(e) FROM ExceptionRecord e WHERE " +
           "e.environment = :environment AND " +
           "(:startDate IS NULL OR e.timestamp >= :startDate) AND " +
           "(:endDate IS NULL OR e.timestamp <= :endDate) " +
           "GROUP BY e.componentName ORDER BY COUNT(e) DESC")
    List<Object[]> getComponentsByEnvironmentAndTimeRange(@Param("environment") String environment, 
                                                         @Param("startDate") LocalDateTime startDate, 
                                                         @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT e.podName, e.podIp, COUNT(e) FROM ExceptionRecord e WHERE " +
           "e.componentName = :componentName AND " +
           "(:startDate IS NULL OR e.timestamp >= :startDate) AND " +
           "(:endDate IS NULL OR e.timestamp <= :endDate) " +
           "GROUP BY e.podName, e.podIp ORDER BY COUNT(e) DESC")
    List<Object[]> getPodsByComponentAndTimeRange(@Param("componentName") String componentName,
                                                 @Param("startDate") LocalDateTime startDate, 
                                                 @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT e.projectName, COUNT(e) FROM ExceptionRecord e WHERE " +
           "e.environment = :environment AND " +
           "(:startDate IS NULL OR e.timestamp >= :startDate) AND " +
           "(:endDate IS NULL OR e.timestamp <= :endDate) " +
           "GROUP BY e.projectName ORDER BY COUNT(e) DESC")
    List<Object[]> getProjectsByEnvironmentAndTimeRange(@Param("environment") String environment,
                                                       @Param("startDate") LocalDateTime startDate, 
                                                       @Param("endDate") LocalDateTime endDate);
    
    // Distinct value queries for filters
    @Query("SELECT DISTINCT e.projectName FROM ExceptionRecord e WHERE e.projectName IS NOT NULL ORDER BY e.projectName")
    List<String> findDistinctProjectNames();
    
    @Query("SELECT DISTINCT e.exceptionType FROM ExceptionRecord e WHERE e.exceptionType IS NOT NULL ORDER BY e.exceptionType")
    List<String> findDistinctExceptionTypes();
    
    @Query("SELECT DISTINCT e.environment FROM ExceptionRecord e WHERE e.environment IS NOT NULL ORDER BY e.environment")
    List<String> findDistinctEnvironments();
    
    @Query("SELECT DISTINCT e.componentName FROM ExceptionRecord e WHERE e.componentName IS NOT NULL ORDER BY e.componentName")
    List<String> findDistinctComponentNames();
    
    @Query("SELECT DISTINCT e.serviceName FROM ExceptionRecord e WHERE e.serviceName IS NOT NULL ORDER BY e.serviceName")
    List<String> findDistinctServiceNames();
    
    @Query("SELECT DISTINCT e.method FROM ExceptionRecord e WHERE e.method IS NOT NULL ORDER BY e.method")
    List<String> findDistinctMethods();
    
    // Count by time range
    Long countByTimestampBetween(LocalDateTime startDate, LocalDateTime endDate);
    
    // Extended filter query with service and method
    @Query("SELECT e FROM ExceptionRecord e WHERE " +
           "(:projectName IS NULL OR e.projectName = :projectName) AND " +
           "(:exceptionType IS NULL OR e.exceptionType = :exceptionType) AND " +
           "(:environment IS NULL OR e.environment = :environment) AND " +
           "(:componentName IS NULL OR e.componentName = :componentName) AND " +
           "(:serviceName IS NULL OR e.serviceName = :serviceName) AND " +
           "(:method IS NULL OR e.method = :method) AND " +
           "(:startDate IS NULL OR e.timestamp >= :startDate) AND " +
           "(:endDate IS NULL OR e.timestamp <= :endDate) " +
           "ORDER BY e.timestamp DESC")
    Page<ExceptionRecord> findWithAllFilters(
        @Param("projectName") String projectName,
        @Param("exceptionType") String exceptionType,
        @Param("environment") String environment,
        @Param("componentName") String componentName,
        @Param("serviceName") String serviceName,
        @Param("method") String method,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate,
        Pageable pageable);
    
    // Extended filter query with service, method and request headers
    @Query("SELECT e FROM ExceptionRecord e WHERE " +
           "(:projectName IS NULL OR e.projectName = :projectName) AND " +
           "(:exceptionType IS NULL OR e.exceptionType = :exceptionType) AND " +
           "(:environment IS NULL OR e.environment = :environment) AND " +
           "(:componentName IS NULL OR e.componentName = :componentName) AND " +
           "(:serviceName IS NULL OR e.serviceName = :serviceName) AND " +
           "(:method IS NULL OR e.method = :method) AND " +
           "(:startDate IS NULL OR e.timestamp >= :startDate) AND " +
           "(:endDate IS NULL OR e.timestamp <= :endDate) AND " +
           "(:headerFilter IS NULL OR " +
           "  (:filterType = 'key' AND e.additionalData LIKE CONCAT('%\"httpHeaders\":{%', :headerFilter, '%')) OR " +
           "  (:filterType = 'value' AND e.additionalData LIKE CONCAT('%\"httpHeaders\":%', :headerFilter, '%')) OR " +
           "  (:filterType = 'both' AND e.additionalData LIKE CONCAT('%\"httpHeaders\":%', :headerFilter, '%'))" +
           ") " +
           "ORDER BY e.timestamp DESC")
    Page<ExceptionRecord> findWithAllFiltersIncludingHeaders(
        @Param("projectName") String projectName,
        @Param("exceptionType") String exceptionType,
        @Param("environment") String environment,
        @Param("componentName") String componentName,
        @Param("serviceName") String serviceName,
        @Param("method") String method,
        @Param("headerFilter") String headerFilter,
        @Param("filterType") String filterType,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate,
        Pageable pageable);
}