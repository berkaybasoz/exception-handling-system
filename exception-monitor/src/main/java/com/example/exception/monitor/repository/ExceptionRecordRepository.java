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
public interface ExceptionRecordRepository extends JpaRepository<ExceptionRecord, String> {
    
    Page<ExceptionRecord> findAllByOrderByTimestampDesc(Pageable pageable);
    
    Page<ExceptionRecord> findByProjectNameOrderByTimestampDesc(String projectName, Pageable pageable);
    
    Page<ExceptionRecord> findByExceptionTypeOrderByTimestampDesc(String exceptionType, Pageable pageable);
    
    Page<ExceptionRecord> findByTimestampBetweenOrderByTimestampDesc(
        LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);
    
    @Query("SELECT e.exceptionType, COUNT(e) FROM ExceptionRecord e GROUP BY e.exceptionType ORDER BY COUNT(e) DESC")
    List<Object[]> getExceptionTypeStatistics();
    
    @Query("SELECT e.projectName, COUNT(e) FROM ExceptionRecord e GROUP BY e.projectName ORDER BY COUNT(e) DESC")
    List<Object[]> getProjectStatistics();
    
    @Query("SELECT COUNT(e) FROM ExceptionRecord e WHERE e.timestamp >= :since")
    Long countExceptionsSince(@Param("since") LocalDateTime since);
}