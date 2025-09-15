package com.example.demo.repository;

import com.example.demo.entity.TimeSheet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface TimeSheetRepository extends JpaRepository<TimeSheet, Long> {
    
    List<TimeSheet> findByStaffId(Long staffId);
    
    List<TimeSheet> findByDate(LocalDate date);
    
    Optional<TimeSheet> findByStaffIdAndDate(Long staffId, LocalDate date);
    
    @Query("SELECT t FROM TimeSheet t WHERE t.staff.id = :staffId AND t.date BETWEEN :startDate AND :endDate ORDER BY t.date")
    List<TimeSheet> findByStaffIdAndDateBetween(@Param("staffId") Long staffId,
                                                @Param("startDate") LocalDate startDate,
                                                @Param("endDate") LocalDate endDate);
    
    @Query("SELECT SUM(t.hoursWorked) FROM TimeSheet t WHERE t.staff.id = :staffId AND t.date BETWEEN :startDate AND :endDate")
    BigDecimal getTotalHoursWorked(@Param("staffId") Long staffId,
                                  @Param("startDate") LocalDate startDate,
                                  @Param("endDate") LocalDate endDate);
    
    @Query("SELECT t FROM TimeSheet t WHERE t.checkOut IS NULL AND t.date = :date")
    List<TimeSheet> findActiveTimeSheets(@Param("date") LocalDate date);
}
