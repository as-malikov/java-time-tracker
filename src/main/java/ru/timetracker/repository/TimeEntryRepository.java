package ru.timetracker.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import ru.timetracker.model.TimeEntry;
import ru.timetracker.model.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface TimeEntryRepository extends JpaRepository<TimeEntry, Long> {
    List<TimeEntry> findByUserAndStartTimeBetweenOrderByStartTime(User user, LocalDateTime start, LocalDateTime end);

    Optional<TimeEntry> findByUserAndEndTimeIsNull(User user);

    @Query("SELECT t.id, t.title, " + "SUM(FUNCTION('TIMESTAMPDIFF', SECOND, te.startTime, " +
            "CASE WHEN te.endTime IS NULL THEN CURRENT_TIMESTAMP ELSE te.endTime END)) " +
            "FROM TimeEntry te JOIN te.task t " +
            "WHERE te.user.id = :userId AND te.startTime BETWEEN :start AND :end " + "GROUP BY t.id, t.title " +
            "ORDER BY MIN(te.startTime)")
    List<Object[]> findTaskDurationsByUserAndPeriod(
            @Param("userId") Long userId, @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);

    Optional<TimeEntry> findFirstByUserIdAndTaskIdOrderByStartTimeAsc(Long userId, Long taskId);

    @Query("SELECT SUM(FUNCTION('TIMESTAMPDIFF', SECOND, te.startTime, " +
            "CASE WHEN te.endTime IS NULL THEN CURRENT_TIMESTAMP ELSE te.endTime END)) " + "FROM TimeEntry te " +
            "WHERE te.user.id = :userId AND te.startTime BETWEEN :start AND :end")
    Long sumWorkDurationByUserAndPeriod(
            @Param("userId") Long userId, @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);

    @Transactional
    @Modifying
    void deleteByUser(User user);

    List<TimeEntry> findByEndTimeIsNull();
}
