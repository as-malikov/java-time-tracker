package ru.timetracker.scheduler;

import jakarta.transaction.Transactional;
import lombok.Data;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import ru.timetracker.model.TimeEntry;
import ru.timetracker.repository.TimeEntryRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Data
@Component
public class TimeEntryAutoCompleter {
    private static final Logger logger = LogManager.getLogger(TimeEntryAutoCompleter.class);
    private final TimeEntryRepository timeEntryRepository;

    @Scheduled(cron = "${app.auto-complete.cron:0 59 23 * * ?}")
    @Transactional
    public void autoCompleteTimeEntries() {
        logger.info("Запуск автоматического завершения временных записей");

        LocalDateTime endOfDay = LocalDateTime.now().with(LocalTime.of(23, 59));
        LocalDate today = LocalDate.now();
        logger.debug("Определено время окончания дня: {}", endOfDay);

        List<TimeEntry> activeEntries = timeEntryRepository.findByEndTimeIsNull();
        logger.debug("Найдено {} активных временных записей", activeEntries.size());

        if (activeEntries.isEmpty()) {
            logger.info("Активные временные записи для автоматического завершения не найдены");
            return;
        }

        logger.info("Начато автоматическое завершение {} временных записей", activeEntries.size());

        activeEntries.stream()
                .filter(entry -> entry.getStartTime().toLocalDate().isBefore(today))
                .forEach(entry -> {
                    logger.debug("Обработка записи ID: {}, пользователь: {}, начало: {}",
                            entry.getId(),
                            entry.getUser().getId(),
                            entry.getStartTime());

                    entry.setEndTime(endOfDay);

                    logger.info("Автоматически завершена временная запись ID {} для пользователя {} (начало: {})",
                            entry.getId(),
                            entry.getUser().getId(),
                            entry.getStartTime());
                });

        try {
            List<TimeEntry> completedEntries = timeEntryRepository.saveAll(activeEntries);
            logger.info("Успешно завершено {} временных записей", completedEntries.size());
        } catch (Exception e) {
            logger.error("Ошибка при сохранении завершенных записей: {}", e.getMessage(), e);
            throw e;
        }

        logger.info("Автоматическое завершение временных записей успешно завершено");
    }
}