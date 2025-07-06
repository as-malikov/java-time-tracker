package ru.timetracker.repository;

import org.apache.catalina.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.timetracker.model.Task;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface TaskRepository extends JpaRepository<Task, Long> {
    List<Task> findByUser(User user);

    Optional<Task> findByUserAndTitle(User user, String title);

    List<Task> findByUserId(Long userId);

    // Поиск задач по ID пользователя и части названия (без учета регистра)
    @Query(value = "SELECT * FROM tasks WHERE user_id = :userId AND LOWER(name) LIKE LOWER(CONCAT('%', :query, '%'))",
            nativeQuery = true)
    List<Task> findByUserIdAndNameContainingIgnoreCase(
            @Param("userId") Long userId,
            @Param("query") String query);

    // Получение задач, созданных после указанной даты
    @Query(value = "SELECT * FROM tasks WHERE created_at > :date", nativeQuery = true)
    List<Task> findTasksCreatedAfter(@Param("date") LocalDateTime date);

    // Подсчет общего времени по задачам пользователя (в минутах)
    @Query(value = """
        SELECT t.id, t.name, SUM(EXTRACT(EPOCH FROM (te.end_time - te.start_time))/60) AS total_minutes
        FROM tasks t
        JOIN time_entries te ON t.id = te.task_id
        WHERE t.user_id = :userId
        GROUP BY t.id, t.name
        """, nativeQuery = true)
    List<Object[]> findTaskTimeSummary(@Param("userId") Long userId);

    @Query("DELETE FROM Task t WHERE t.user.id = :userId")
    void deleteByUserId(@Param("userId") Long userId);
}
