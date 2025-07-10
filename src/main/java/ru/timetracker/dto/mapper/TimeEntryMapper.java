package ru.timetracker.dto.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import ru.timetracker.dto.timeentry.TimeEntryDTO;
import ru.timetracker.model.TimeEntry;

import java.time.Duration;

/**
 * Маппер для преобразования между сущностью TimeEntry и DTO.
 * Обеспечивает сложные преобразования с расчетом продолжительности.
 *
 * <p>Особенности:
 * <ul>
 *   <li>Автоматический расчет продолжительности</li>
 *   <li>Определение активности записи</li>
 *   <li>Преобразование связанных сущностей</li>
 * </ul>
 */
@Mapper(componentModel = "spring")
public interface TimeEntryMapper {

    /**
     * Преобразует сущность TimeEntry в TimeEntryDTO
     *
     * @param timeEntry Сущность записи времени
     * @return DTO записи времени
     */
    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "taskId", source = "task.id")
    @Mapping(target = "taskTitle", source = "task.title")
    @Mapping(target = "duration", source = ".", qualifiedByName = "calculateDuration")
    @Mapping(target = "active", source = ".", qualifiedByName = "checkActive")
    TimeEntryDTO toDTO(TimeEntry timeEntry);

    /**
     * Вычисляет продолжительность записи времени
     *
     * @param timeEntry Сущность записи времени
     * @return Продолжительность
     */
    @Named("calculateDuration")
    default Duration calculateDuration(TimeEntry timeEntry) {
        return timeEntry.getDuration();
    }

    /**
     * Проверяет активна ли запись времени
     *
     * @param timeEntry Сущность записи времени
     * @return true если запись активна
     */
    @Named("checkActive")
    default boolean checkActive(TimeEntry timeEntry) {
        return timeEntry.isActive();
    }
}
