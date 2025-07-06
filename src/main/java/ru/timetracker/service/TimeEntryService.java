package ru.timetracker.service;

import lombok.Data;
import org.springframework.stereotype.Service;
import ru.timetracker.dto.Mapper.TimeEntryMapper;
import ru.timetracker.dto.TimeEntryRequestDto;
import ru.timetracker.dto.TimeEntryResponseDto;
import ru.timetracker.dto.TimeEntrySummaryDto;
import ru.timetracker.exception.BusinessException;
import ru.timetracker.exception.EntityNotFoundException;
import ru.timetracker.model.Task;
import ru.timetracker.model.TimeEntry;
import ru.timetracker.model.User;
import ru.timetracker.repository.TaskRepository;
import ru.timetracker.repository.TimeEntryRepository;
import ru.timetracker.repository.UserRepository;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Data
public class TimeEntryService {
    private final TimeEntryRepository timeEntryRepository;
    private final TaskRepository taskRepository;
    private final TimeEntryMapper timeEntryMapper;
    private final UserRepository userRepository;

    public TimeEntryResponseDto startTracking(TimeEntryRequestDto requestDto) {
        Task task = taskRepository.findById(requestDto.getTaskId())
                .orElseThrow(() -> new EntityNotFoundException("Задача не найдена"));

        User user = userRepository.findById(requestDto.getUserId())
                .orElseThrow(() -> new EntityNotFoundException("Пользователь не найден"));

        if (timeEntryRepository.existsByTaskIdAndUserIdAndEndTimeIsNull(
                requestDto.getTaskId(), requestDto.getUserId())) {
            throw new BusinessException("У вас уже есть активный таймер для этой задачи");
        }

        TimeEntry entry = TimeEntry.builder()
                .task(task)
                .user(user)
                .startTime(requestDto.getStartTime() != null ?
                        requestDto.getStartTime() : LocalDateTime.now())
                .description(requestDto.getDescription())
                .build();

        TimeEntry savedEntry = timeEntryRepository.save(entry);
        return timeEntryMapper.toDto(savedEntry);
    }

    public TimeEntryResponseDto stopTracking(Long entryId) {
        TimeEntry entry = timeEntryRepository.findById(entryId)
                .orElseThrow(() -> new EntityNotFoundException("Time entry not found"));

        entry.setEndTime(LocalDateTime.now());
        TimeEntry updatedEntry = timeEntryRepository.save(entry);
        return timeEntryMapper.toDto(updatedEntry);
    }

    public List<TimeEntryResponseDto> getTimeEntriesByTask(Long taskId) {
        return timeEntryRepository.findByTaskId(taskId).stream()
                .map(timeEntryMapper::toDto)
                .collect(Collectors.toList());
    }

    public List<TimeEntrySummaryDto> getTimeSummary(Long userId, LocalDate start, LocalDate end) {
        LocalDateTime startDate = start.atStartOfDay();
        LocalDateTime endDate = end.atTime(23, 59, 59);

        List<TimeEntry> entries = timeEntryRepository
                .findByTaskUserIdAndStartTimeBetween(userId, startDate, endDate);

        Map<Task, Duration> summary = entries.stream()
                .collect(Collectors.groupingBy(
                        TimeEntry::getTask,
                        Collectors.reducing(
                                Duration.ZERO,
                                e -> Duration.between(
                                        e.getStartTime(),
                                        e.getEndTime() != null ? e.getEndTime() : LocalDateTime.now()
                                ),
                                Duration::plus
                        )
                ));

        return summary.entrySet().stream()
                .map(e -> new TimeEntrySummaryDto(
                        e.getKey().getId(),
                        e.getKey().getTitle(),
                        formatDuration(e.getValue())
                ))
                        .collect(Collectors.toList());
    }

    private String formatDuration(Duration duration) {
        long hours = duration.toHours();
        long minutes = duration.minusHours(hours).toMinutes();
        return String.format("%02d:%02d", hours, minutes);
    }
}
