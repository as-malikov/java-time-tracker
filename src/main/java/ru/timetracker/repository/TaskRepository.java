package ru.timetracker.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;
import ru.timetracker.model.Task;
import ru.timetracker.model.User;

import java.util.List;
import java.util.Optional;

/**
 * Репозиторий для работы с задачами.
 * Расширяет {@link JpaRepository} и добавляет специализированные методы для работы с {@link Task}.
 */
public interface TaskRepository extends JpaRepository<Task, Long> {
    /**
     * Находит все задачи пользователя
     *
     * @param user Пользователь-владелец задач
     * @return Список задач пользователя
     */
    List<Task> findByUser(User user);

    /**
     * Находит задачи пользователя с фильтрацией по статусу
     *
     * @param userId          ID пользователя
     * @param includeInactive Включать ли неактивные задачи
     * @return Список задач с учетом фильтра
     */
    @Query("SELECT t FROM Task t WHERE t.user.id = :userId AND (t.active = true OR :includeInactive = true)")
    List<Task> findByUserId(Long userId, boolean includeInactive);

    /**
     * Проверяет существование задачи с указанным названием у пользователя
     *
     * @param userId ID пользователя
     * @param title  Название задачи
     * @return true если задача с таким названием уже существует, false в противном случае
     */
    boolean existsByUserIdAndTitle(Long userId, String title);

    /**
     * Находит задачу по ID с проверкой владельца
     *
     * @param id     ID задачи
     * @param userId ID пользователя
     * @return Задача, если найдена и принадлежит пользователю
     */
    Optional<Task> findByIdAndUserId(Long id, Long userId);

    /**
     * Удаляет все задачи пользователя
     *
     * @param user Пользователь, чьи задачи нужно удалить
     */
    @Transactional
    @Modifying
    void deleteByUser(User user);
}
