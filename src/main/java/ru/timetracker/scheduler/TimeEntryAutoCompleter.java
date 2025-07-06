package ru.timetracker.scheduler;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import ru.timetracker.model.TimeEntry;
import ru.timetracker.repository.TimeEntryRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Data
@Component
@Slf4j
public class TimeEntryAutoCompleter {
    private final TimeEntryRepository timeEntryRepository;

    // Запускается каждый день в 23:59
    @Scheduled(cron = "0 59 23 * * ?")
    public void completeOpenTimeEntries() {
        LocalDate today = LocalDate.now();
        LocalDateTime endOfDay = today.atTime(23, 59, 59);

        List<TimeEntry> openEntries = timeEntryRepository.findByEndTimeIsNull();

        openEntries.forEach(entry -> {
            // Если запись началась сегодня
            if (entry.getStartTime().toLocalDate().equals(today)) {
                entry.setEndTime(endOfDay);
                timeEntryRepository.save(entry);
                log.info("Auto-completed time entry ID: {}", entry.getId());
            }
        });
    }
}
