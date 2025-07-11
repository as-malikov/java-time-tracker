package ru.timetracker.dto.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import ru.timetracker.dto.task.TaskCreateDTO;
import ru.timetracker.dto.task.TaskDTO;
import ru.timetracker.dto.task.TaskUpdateDTO;
import ru.timetracker.model.Task;
import ru.timetracker.repository.UserRepository;

/**
 * Маппер для преобразования между сущностью Task и DTO.
 * Использует MapStruct для автоматического преобразования объектов.
 * <p>Основные преобразования:
 * <ul>
 *   <li>Task ↔ TaskDTO</li>
 *   <li>TaskCreateDTO → Task</li>
 *   <li>TaskUpdateDTO → Обновление Task</li>
 * </ul>
 */
@Mapper(componentModel = "spring", uses = {UserRepository.class})
public interface TaskMapper {


    /**
     * Преобразует сущность Task в TaskDTO
     * @param task Сущность задачи
     * @return DTO задачи
     */
    @Mapping(target = "userId", source = "user.id")
    TaskDTO toDTO(Task task);

    /**
     * Преобразует TaskCreateDTO в сущность Task
     * @param taskCreateDTO DTO для создания задачи
     * @return Сущность задачи
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "timeEntries", ignore = true)
    @Mapping(target = "active", ignore = true)
    Task toEntity(TaskCreateDTO taskCreateDTO);

    /**
     * Обновляет сущность Task из TaskUpdateDTO
     * @param taskUpdateDTO DTO с обновленными данными
     * @param task Сущность для обновления
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "timeEntries", ignore = true)
    void updateEntity(TaskUpdateDTO taskUpdateDTO, @MappingTarget Task task);
}
