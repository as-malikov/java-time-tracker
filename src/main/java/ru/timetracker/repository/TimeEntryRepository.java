package ru.timetracker.repository;

import org.apache.catalina.User;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.timetracker.model.TimeEntry;

import java.time.LocalDateTime;
import java.util.List;

public interface TimeEntryRepository extends JpaRepository<TimeEntry, Long> {
    List<TimeEntry> findByUserAndStartTimeBetween(User user, LocalDateTime start, LocalDateTime end);

    List<TimeEntry> findByUserAndEndTimeIsNull(User user);

    List<TimeEntry> findByUser(User user);
}
