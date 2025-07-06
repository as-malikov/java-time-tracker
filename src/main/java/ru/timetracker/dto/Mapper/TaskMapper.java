package ru.timetracker.dto.Mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import ru.timetracker.dto.TaskRequestDto;
import ru.timetracker.dto.TaskResponseDto;
import ru.timetracker.model.Task;
import ru.timetracker.repository.UserRepository;

@Mapper(componentModel = "spring",  uses = {UserRepository.class})
public interface TaskMapper {
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    Task toEntity(TaskRequestDto dto);

    @Mapping(source = "user.id", target = "userId")
    TaskResponseDto toDto(Task entity);

    @Mapping(target = "user", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "id", ignore = true)
    void updateEntity(TaskRequestDto dto, @MappingTarget Task entity);
}
