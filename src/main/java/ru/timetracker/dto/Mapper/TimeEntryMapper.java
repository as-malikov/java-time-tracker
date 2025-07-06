package ru.timetracker.dto.Mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.timetracker.dto.TimeEntryRequestDto;
import ru.timetracker.dto.TimeEntryResponseDto;
import ru.timetracker.dto.TimeEntrySummaryDto;
import ru.timetracker.model.TimeEntry;

import java.time.Duration;
import java.time.LocalDateTime;

@Mapper(componentModel = "spring")
public interface TimeEntryMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "endTime", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "task", ignore = true)
    @Mapping(target = "user", ignore = true)
    TimeEntry toEntity(TimeEntryRequestDto dto);

    @Mapping(source = "task.id", target = "taskId")
    @Mapping(source = "task.title", target = "taskTitle")
    @Mapping(source = "user.id", target = "userId")
    @Mapping(source = "user.login", target = "userLogin")
    @Mapping(target = "duration", expression = "java(calculateDuration(entity.getStartTime(), entity.getEndTime()))")
    TimeEntryResponseDto toDto(TimeEntry entity);

    default String calculateDuration(LocalDateTime start, LocalDateTime end) {
        if (end == null) return "В процессе";
        Duration duration = Duration.between(start, end);
        return String.format("%02d:%02d", duration.toHours(), duration.toMinutesPart());
    }
}
