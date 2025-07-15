package ru.timetracker.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.timetracker.model.User;

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
}
