package ru.timetracker.scheduler;

import jakarta.transaction.Transactional;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import ru.timetracker.repository.TimeEntryRepository;
import ru.timetracker.repository.TaskRepository;
import ru.timetracker.repository.UserRepository;

import java.time.LocalDateTime;

/**
 * Автоматическая очистка устаревших данных.
 */
@Component
@Transactional
public class DataCleanupScheduler {
    private static final Logger logger = LogManager.getLogger(DataCleanupScheduler.class);

    private final TimeEntryRepository timeEntryRepository;
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;

    @Value("${timetracker.data.retention.days:30}")
    private int retentionDays;

    /**
     * Конструктор для внедрения зависимостей.
     */
    public DataCleanupScheduler(
            TimeEntryRepository timeEntryRepository,
            TaskRepository taskRepository,
            UserRepository userRepository
    ) {
        this.timeEntryRepository = timeEntryRepository;
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
    }

    /**
     * Ежедневно удаляет данные старше указанного срока.
     */
    @Scheduled(cron = "${app.cleanup.cron:0 0 1 * * ?}")
    public void cleanupOldData() {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(retentionDays);

        logger.info("Starting data cleanup for records older than {} days (before {})", retentionDays, cutoffDate);

        int deletedEntries = timeEntryRepository.deleteByStartTimeBefore(cutoffDate);
        logger.info("Deleted {} time entries", deletedEntries);

        int deletedTasks = taskRepository.deleteInactiveTasksOlderThan(cutoffDate);
        logger.info("Deleted {} inactive tasks", deletedTasks);

        int deletedUsers = userRepository.deleteInactiveUsers(cutoffDate);
        logger.info("Deleted {} inactive users", deletedUsers);
    }
}