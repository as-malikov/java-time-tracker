package ru.timetracker.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.timetracker.model.User;

import java.time.LocalDateTime;

/**
 * Репозиторий для работы с пользователями. Предоставляет базовые CRUD-операции и специализированные запросы для сущности {@link User}.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    /**
     * Проверяет существование пользователя с указанным email
     * @param email Email для проверки
     * @return true если email уже существует, false в противном случае
     */
    boolean existsByEmail(String email);

    @Modifying
    @Query("DELETE FROM User u WHERE " +
            "NOT EXISTS (SELECT 1 FROM Task t WHERE t.user = u) AND " +
            "NOT EXISTS (SELECT 1 FROM TimeEntry te WHERE te.user = u) AND " +
            "u.createdAt < :cutoffDate")
    int deleteInactiveUsers(@Param("cutoffDate") LocalDateTime cutoffDate);
}
