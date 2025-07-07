package ru.timetracker.dto.Mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import ru.timetracker.dto.TaskCreateDTO;
import ru.timetracker.dto.TaskDTO;
import ru.timetracker.dto.TaskUpdateDTO;
import ru.timetracker.model.Task;
import ru.timetracker.repository.UserRepository;

@Mapper(componentModel = "spring",  uses = {UserRepository.class})
public interface TaskMapper {
    @Mapping(target = "userId", source = "user.id")
    TaskDTO toDTO(Task task);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "timeEntries", ignore = true)
    @Mapping(target = "active", ignore = true)
    Task toEntity(TaskCreateDTO taskCreateDTO);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "timeEntries", ignore = true)
    void updateEntity(TaskUpdateDTO taskUpdateDTO, @MappingTarget Task task);
}
