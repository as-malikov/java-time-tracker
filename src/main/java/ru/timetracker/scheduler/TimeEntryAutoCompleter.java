package ru.timetracker.scheduler;

import jakarta.transaction.Transactional;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import ru.timetracker.model.TimeEntry;
import ru.timetracker.repository.TimeEntryRepository;
import ru.timetracker.service.TimeEntryService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * Компонент для автоматического завершения незавершенных записей времени.
 * Выполняется по расписанию и завершает все активные записи времени в конце дня.
 *
 * <p>Особенности работы:
 * <ul>
 *   <li>Запускается ежедневно в 23:59 (по умолчанию)</li>
 *   <li>Находит все активные записи времени (без endTime)</li>
 *   <li>Автоматически завершает их с текущим временем</li>
 *   <li>Логирует процесс завершения</li>
 * </ul>
 *
 * <p>Конфигурация:
 * <ul>
 *   <li>Расписание настраивается через параметр {@code app.auto-complete.cron}</li>
 *   <li>По умолчанию: {@code 0 59 23 * * ?} (каждый день в 23:59)</li>
 * </ul>
 */
@Data
@Component
@Slf4j
@Transactional
public class TimeEntryAutoCompleter {
    private final TimeEntryRepository timeEntryRepository;
    private final TimeEntryService timeEntryService;

    /**
     * Метод для автоматического завершения активных записей времени.
     * Выполняется по cron-расписанию и работает в транзакционном контексте.
     *
     * <p>Логика работы:
     * <ol>
     *   <li>Находит все активные записи времени</li>
     *   <li>Для каждой записи вызывает сервис для остановки</li>
     *   <li>Сохраняет обновленные записи</li>
     *   <li>Логирует результаты</li>
     * </ol>
     *
     * <p>Обработка ошибок:
     * <ul>
     *   <li>Транзакция откатывается при ошибках</li>
     *   <li>Ошибки логируются</li>
     * </ul>
     */
    @Scheduled(cron = "${app.auto-complete.cron:0 59 23 * * ?}")
    @Transactional
    public void autoCompleteTimeEntries() {

        LocalDateTime endOfDay = LocalDateTime.now()
                .with(LocalTime.of(23, 59));
        LocalDateTime today = LocalDateTime.now();

        List<TimeEntry> activeEntries = timeEntryRepository.findByEndTimeIsNull();

        if (activeEntries.isEmpty()) {
            log.debug("No active time entries found for auto-completion");
            return;
        }

        log.info("Starting auto-completion of {} time entries", activeEntries.size());

        activeEntries.forEach(entry -> {
            timeEntryService.stopTimeEntry(entry.getUser()
                    .getId());
            log.info("Auto-completed time entry ID {} for user {} (started at {})", entry.getId(), entry.getUser()
                    .getId(), entry.getStartTime());
        });
        List<TimeEntry> completedEntries = timeEntryRepository.saveAll(activeEntries);
        log.info("Successfully completed {} time entries", completedEntries.size());
    }
}