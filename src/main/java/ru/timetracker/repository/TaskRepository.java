package ru.timetracker.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;
import ru.timetracker.model.Task;
import ru.timetracker.model.User;

import java.util.List;
import java.util.Optional;

public interface TaskRepository extends JpaRepository<Task, Long> {
    List<Task> findByUser(User user);

    List<Task> findByUserIdAndActiveTrue(Long userId);

    @Query("SELECT t FROM Task t WHERE t.user.id = :userId AND (t.active = true OR :includeInactive = true)")
    List<Task> findByUserId(Long userId, boolean includeInactive);

    boolean existsByUserIdAndTitle(Long userId, String title);

    Optional<Task> findByIdAndUserId(Long id, Long userId);

    @Transactional
    @Modifying
    void deleteByUser(User user);
}
