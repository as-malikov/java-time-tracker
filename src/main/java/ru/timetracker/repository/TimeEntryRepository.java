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

/**
 * Репозиторий для работы с записями времени. Предоставляет сложные запросы для аналитики временных интервалов {@link TimeEntry}.
 */
public interface TimeEntryRepository extends JpaRepository<TimeEntry, Long> {
    /**
     * Находит записи времени пользователя за указанный период
     * @param user  Пользователь
     * @param start Начало периода
     * @param end   Конец периода
     * @return Список записей, отсортированный по времени начала
     */
    List<TimeEntry> findByUserAndStartTimeBetweenOrderByStartTime(User user, LocalDateTime start, LocalDateTime end);

    /**
     * Находит активную (незавершенную) запись времени пользователя
     * @param user Пользователь
     * @return Активная запись времени, если существует
     */
    Optional<TimeEntry> findByUserAndEndTimeIsNull(User user);

    /**
     * Вычисляет продолжительность работы по задачам за период
     * @param userId ID пользователя
     * @param start  Начало периода
     * @param end    Конец периода
     * @return Список массивов [taskId, taskTitle, totalSeconds]
     */
    @Query("SELECT t.id, t.title, " + "SUM(FUNCTION('TIMESTAMPDIFF', SECOND, te.startTime, " +
            "CASE WHEN te.endTime IS NULL THEN CURRENT_TIMESTAMP ELSE te.endTime END)) " + "FROM TimeEntry te JOIN te.task t " +
            "WHERE te.user.id = :userId AND te.startTime BETWEEN :start AND :end " + "GROUP BY t.id, t.title " +
            "ORDER BY MIN(te.startTime)")
    List<Object[]> findTaskDurationsByUserAndPeriod(@Param("userId") Long userId, @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);

    /**
     * Находит первую запись времени для задачи пользователя
     * @param userId ID пользователя
     * @param taskId ID задачи
     * @return Самая ранняя запись времени, если существует
     */
    Optional<TimeEntry> findFirstByUserIdAndTaskIdOrderByStartTimeAsc(Long userId, Long taskId);

    /**
     * Вычисляет общее время работы пользователя за период
     * @param userId ID пользователя
     * @param start  Начало периода
     * @param end    Конец периода
     * @return Суммарное время работы в секундах
     */
    @Query("SELECT SUM(FUNCTION('TIMESTAMPDIFF', SECOND, te.startTime, " +
            "CASE WHEN te.endTime IS NULL THEN CURRENT_TIMESTAMP ELSE te.endTime END)) " + "FROM TimeEntry te " +
            "WHERE te.user.id = :userId AND te.startTime BETWEEN :start AND :end")
    Long sumWorkDurationByUserAndPeriod(@Param("userId") Long userId, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    /**
     * Удаляет все записи времени пользователя
     * @param user Пользователь, чьи записи нужно удалить
     */
    @Transactional
    @Modifying
    void deleteByUser(User user);

    /**
     * Находит все активные (незавершенные) записи времени
     * @return Список активных записей времени
     */
    List<TimeEntry> findByEndTimeIsNull();

    @Modifying
    @Query("DELETE FROM TimeEntry te WHERE te.startTime < :cutoffDate")
    int deleteByStartTimeBefore(@Param("cutoffDate") LocalDateTime cutoffDate);
}
