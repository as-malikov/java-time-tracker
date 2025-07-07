package ru.timetracker.scheduler;

import jakarta.transaction.Transactional;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import ru.timetracker.model.TimeEntry;
import ru.timetracker.repository.TimeEntryRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Data
@Component
@Slf4j
public class TimeEntryAutoCompleter {
    private final TimeEntryRepository timeEntryRepository;

    @Scheduled(cron = "${app.auto-complete.cron:0 59 23 * * ?}")
    @Transactional
    public void autoCompleteTimeEntries() {
        LocalDateTime endOfDay = LocalDateTime.now().with(LocalTime.of(23, 59));
        LocalDate today = LocalDate.now();

        List<TimeEntry> activeEntries = timeEntryRepository.findByEndTimeIsNull();

        if (activeEntries.isEmpty()) {
            log.debug("No active time entries found for auto-completion");
            return;
        }

        log.info("Starting auto-completion of {} time entries", activeEntries.size());

        activeEntries.stream()
                .filter(entry -> entry.getStartTime().toLocalDate().isBefore(today))
                .forEach(entry -> {
                    entry.setEndTime(endOfDay);
                    log.info("Auto-completed time entry ID {} for user {} (started at {})",
                            entry.getId(),
                            entry.getUser().getId(),
                            entry.getStartTime());
                });

        List<TimeEntry> completedEntries = timeEntryRepository.saveAll(activeEntries);
        log.info("Successfully completed {} time entries", completedEntries.size());
    }
}
