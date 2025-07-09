package ru.timetracker.dto.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import ru.timetracker.dto.timeentry.TimeEntryDTO;
import ru.timetracker.model.TimeEntry;

import java.time.Duration;

@Mapper(componentModel = "spring")
public interface TimeEntryMapper {
    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "taskId", source = "task.id")
    @Mapping(target = "taskTitle", source = "task.title")
    @Mapping(target = "duration", source = ".", qualifiedByName = "calculateDuration")
    @Mapping(target = "active", source = ".", qualifiedByName = "checkActive")
    TimeEntryDTO toDTO(TimeEntry timeEntry);
}
