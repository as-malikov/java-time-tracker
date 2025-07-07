package ru.timetracker.service;

import lombok.Data;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.timetracker.dto.*;
import ru.timetracker.dto.Mapper.TimeEntryMapper;
import ru.timetracker.exception.ResourceNotFoundException;
import ru.timetracker.model.Task;
import ru.timetracker.model.TimeEntry;
import ru.timetracker.model.User;
import ru.timetracker.repository.TaskRepository;
import ru.timetracker.repository.TimeEntryRepository;
import ru.timetracker.repository.UserRepository;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;


@Service
@Data
public class TimeEntryService {
    private final TimeEntryRepository timeEntryRepository;
    private final UserRepository userRepository;
    private final TaskRepository taskRepository;
    private final TimeEntryMapper timeEntryMapper;

    @Transactional
    public TimeEntryDTO startTimeEntry(Long userId, TimeEntryCreateDTO dto) {
        User user = getUser(userId);
        Task task = getTask(dto.getTaskId());

        // Проверка, что задача принадлежит пользователю
        if (!task.getUser().getId().equals(userId)) {
            throw new ResourceNotFoundException("Task not found");
        }

        // Завершаем предыдущую активную запись (если есть)
        timeEntryRepository.findByUserAndEndTimeIsNull(user)
                .ifPresent(entry -> {
                    entry.setEndTime(LocalDateTime.now());
                    timeEntryRepository.save(entry);
                });

        TimeEntry entry = new TimeEntry();
        entry.setUser(user);
        entry.setTask(task);
        entry = timeEntryRepository.save(entry);

        return timeEntryMapper.toDTO(entry);
    }

    @Transactional
    public TimeEntryDTO stopTimeEntry(Long userId) {
        TimeEntry entry = timeEntryRepository.findByUserAndEndTimeIsNull(getUser(userId))
                .orElseThrow(() -> new IllegalStateException("No active time entry"));

        entry.setEndTime(LocalDateTime.now());
        entry = timeEntryRepository.save(entry);
        return timeEntryMapper.toDTO(entry);
    }

    @Transactional(readOnly = true)
    public List<TimeEntryDTO> getUserTimeEntries(Long userId, LocalDateTime from, LocalDateTime to) {
        // Если оба параметра null, устанавливаем период "текущие сутки"
        if (from == null && to == null) {
            LocalDateTime now = LocalDateTime.now();
            from = now.toLocalDate().atStartOfDay(); // Начало текущих суток (00:00)
            to = now; // Текущий момент времени
        }
        // Если указан только from, устанавливаем to = текущее время
        else if (from != null && to == null) {
            to = LocalDateTime.now();
        }
        // Если указан только to, устанавливаем from = начало суток для to
        else if (to != null && from == null) {
            from = to.toLocalDate().atStartOfDay();
        }

        return timeEntryRepository.findByUserAndStartTimeBetweenOrderByStartTime(
                        getUser(userId), from, to)
                .stream()
                .map(timeEntryMapper::toDTO)
                .toList();
    }

    private User getUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private Task getTask(Long taskId) {
        return taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found"));
    }

    public List<TaskDurationDTO> getUserTaskDurations(Long userId, LocalDateTime from, LocalDateTime to) {
        // Валидация периода
        if (from != null && to != null && from.isAfter(to)) {
            throw new IllegalArgumentException("End date must be after start date");
        }

        // Установка периода по умолчанию
        if (from == null && to == null) {
            LocalDateTime now = LocalDateTime.now();
            from = now.toLocalDate().atStartOfDay();
            to = now;
        } else if (from != null && to == null) {
            to = LocalDateTime.now();
        } else if (to != null && from == null) {
            from = to.toLocalDate().atStartOfDay();
        }

        try {
            List<Object[]> results = timeEntryRepository.findTaskDurationsByUserAndPeriod(
                    userId, from, to);

            return results.stream()
                    .map(row -> {
                        try {
                            Long taskId = ((Number) row[0]).longValue();
                            String title = (String) row[1];
                            long totalSeconds = ((Number) row[2]).longValue();

                            LocalDateTime firstEntry = timeEntryRepository
                                    .findFirstByUserIdAndTaskIdOrderByStartTimeAsc(userId, taskId)
                                    .map(TimeEntry::getStartTime)
                                    .orElse(null);

                            return new TaskDurationDTO(
                                    taskId,
                                    title,
                                    formatDuration(totalSeconds),
                                    firstEntry
                            );
                        } catch (Exception e) {
                            throw new IllegalArgumentException("Error processing time entry data", e);
                        }
                    })
                    .sorted(Comparator.comparing(
                            TaskDurationDTO::getFirstEntryTime,
                            Comparator.nullsLast(Comparator.naturalOrder())))
                    .toList();
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to get user task durations", e);
        }
    }

    private String formatDurationUserTimeIntervals(Duration duration) {
        long hours = duration.toHours();
        long minutes = duration.minusHours(hours).toMinutes();
        return String.format("%02d:%02d", hours, minutes);
    }

    public List<TimeIntervalDTO> getUserTimeIntervals(Long userId, LocalDateTime from, LocalDateTime to) {
        // Установка периода по умолчанию
        if (from == null && to == null) {
            to = LocalDateTime.now();
            from = to.minusDays(7);
        }

        // Валидация периода
        if (from.isAfter(to)) {
            throw new IllegalArgumentException("Start date must be before end date");
        }

        // Получаем записи из БД
        List<TimeEntry> entries = timeEntryRepository
                .findByUserAndStartTimeBetweenOrderByStartTime(getUser(userId), from, to);

        List<TimeIntervalDTO> result = new ArrayList<>();
        LocalDateTime previousEnd = from;

        // Обрабатываем интервалы
        for (TimeEntry entry : entries) {
            LocalDateTime entryStart = entry.getStartTime();
            LocalDateTime entryEnd = entry.getEndTime() != null ?
                    entry.getEndTime() : LocalDateTime.now();

            // Добавляем период неактивности перед текущей записью
            if (previousEnd.isBefore(entryStart)) {
                addInactiveInterval(result, previousEnd, entryStart);
            }

            // Добавляем период работы
            addActiveInterval(result, entry, entryStart, entryEnd);

            previousEnd = entryEnd.isAfter(previousEnd) ? entryEnd : previousEnd;
        }

        // Добавляем последний период неактивности
        if (previousEnd.isBefore(to)) {
            addInactiveInterval(result, previousEnd, to);
        }

        return result;
    }

    private void addActiveInterval(List<TimeIntervalDTO> result,
            TimeEntry entry,
            LocalDateTime start,
            LocalDateTime end) {
        Duration duration = Duration.between(start, end);
        result.add(new TimeIntervalDTO(
                formatDurationUserTimeIntervals(duration),
                entry.getTask().getTitle(),
                true,
                start,
                end
        ));
    }

    private void addInactiveInterval(List<TimeIntervalDTO> result,
            LocalDateTime start,
            LocalDateTime end) {
        Duration duration = Duration.between(start, end);
        result.add(new TimeIntervalDTO(
                formatDurationUserTimeIntervals(duration),
                "Неактивность",
                false,
                start,
                end
        ));
    }

    public TotalWorkDurationDTO getTotalWorkDuration(Long userId, LocalDateTime from, LocalDateTime to) {
        // Установка периода по умолчанию (текущая неделя)
        if (from == null && to == null) {
            to = LocalDateTime.now();
            from = to.minusDays(7);
        } else if (from == null) {
            from = to.minusDays(7);
        } else if (to == null) {
            to = from.plusDays(7);
        }

        // Получаем сумму в секундах из БД
        Long totalSeconds = timeEntryRepository.sumWorkDurationByUserAndPeriod(userId, from, to);
        if (totalSeconds == null) {
            totalSeconds = 0L;
        }

        // Рассчитываем количество дней в периоде
        long daysInPeriod = ChronoUnit.DAYS.between(
                from.toLocalDate(),
                to.toLocalDate()
        ) + 1; // +1 чтобы включить оба крайних дня

        return new TotalWorkDurationDTO(
                formatDuration(totalSeconds),
                totalSeconds,
                (int) daysInPeriod,
                from,
                to
        );
    }

    private String formatDuration(long totalSeconds) {
        long hours = totalSeconds / 3600;
        long minutes = (totalSeconds % 3600) / 60;
        return String.format("%02d:%02d", hours, minutes);
    }

    @Transactional
    public void clearUserTrackingData(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        // 1. Удаление всех временных записей пользователя
        timeEntryRepository.deleteByUser(user);

        // 2. Очистка связей задач (без удаления самих задач)
        List<Task> userTasks = taskRepository.findByUser(user);
        userTasks.forEach(task -> {
            task.getTimeEntries()
                    .clear(); // Очищаем коллекцию timeEntries
            taskRepository.save(task); // Обновляем задачу
        });
    }
}
