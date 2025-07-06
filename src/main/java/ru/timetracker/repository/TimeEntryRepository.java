package ru.timetracker.repository;

import org.apache.catalina.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.timetracker.dto.TimeEntrySummaryDto;
import ru.timetracker.model.TimeEntry;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface TimeEntryRepository extends JpaRepository<TimeEntry, Long> {
    boolean existsByTaskIdAndUserIdAndEndTimeIsNull(Long taskId, Long userId);
    List<TimeEntry> findByTaskId(Long taskId);

    @Query("SELECT te FROM TimeEntry te WHERE te.task.user.id = :userId " +
            "AND te.startTime BETWEEN :start AND :end")
    List<TimeEntry> findByTaskUserIdAndStartTimeBetween(
            @Param("userId") Long userId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);

    @Query("SELECT te FROM TimeEntry te WHERE te.user.id = :userId AND te.startTime BETWEEN :start AND :end")
    List<TimeEntry> findByUserIdAndPeriod(
            @Param("userId") Long userId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);

    @Query("SELECT te FROM TimeEntry te WHERE te.endTime IS NULL")
    List<TimeEntry> findActiveEntries();

    @Query("SELECT te FROM TimeEntry te WHERE te.task.id = :taskId AND te.endTime IS NULL")
    Optional<TimeEntry> findActiveByTaskId(@Param("taskId") Long taskId);

    // Находим все незавершенные записи
    List<TimeEntry> findByEndTimeIsNull();

    List<TimeEntry> findByEndTimeIsNullAndStartTimeBefore(LocalDateTime beforeTime);

    @Query("DELETE FROM TimeEntry te WHERE te.task.user.id = :userId")
    void deleteByTaskUserId(@Param("userId") Long userId);

}
