package ru.timetracker.scheduler;

import jakarta.transaction.Transactional;
import lombok.Data;
import lombok.RequiredArgsConstructor;
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
 * Автозавершение незакрытых временных записей.
 */
@Component
@Slf4j
@Transactional
public class TimeEntryAutoCompleter {
    private final TimeEntryRepository timeEntryRepository;
    private final TimeEntryService timeEntryService;

    /**
     * Создает новый экземпляр TimeEntryAutoCompleter.
     * @param timeEntryRepository репозиторий для работы с записями времени
     * @param timeEntryService сервис для управления записями времени
     */
    public TimeEntryAutoCompleter(TimeEntryRepository timeEntryRepository, TimeEntryService timeEntryService) {
        this.timeEntryRepository = timeEntryRepository;
        this.timeEntryService = timeEntryService;
    }

    /**
     * Ежедневно завершает все активные записи времени в 23:59.
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