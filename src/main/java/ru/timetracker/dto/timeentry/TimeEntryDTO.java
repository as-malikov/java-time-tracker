package ru.timetracker.dto.timeentry;

import lombok.Data;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * DTO для передачи полных данных о записи времени.
 * Содержит всю информацию о временном интервале работы.
 */
@Data
public class TimeEntryDTO {
    /**
     * Уникальный идентификатор записи
     * @return ID записи времени
     */
    private Long id;

    /**
     * Время начала работы
     * @return Дата и время начала
     */
    private LocalDateTime startTime;

    /**
     * Время окончания работы (null для активных записей)
     * @return Дата и время окончания
     */
    private LocalDateTime endTime;

    /**
     * Продолжительность работы
     * @return Длительность интервала
     */
    private Duration duration;

    /**
     * ID пользователя, владельца записи
     * @return ID пользователя
     */
    private Long userId;

    /**
     * ID связанной задачи
     * @return ID задачи
     */
    private Long taskId;

    /**
     * Название связанной задачи
     * @return Название задачи
     */
    private String taskTitle;

    /**
     * Статус активности записи
     * @return true - запись активна (трекер работает), false - завершена
     */
    private boolean active;

    /**
     * Конструктор по умолчанию, необходимый для Javadoc.
     */
    public TimeEntryDTO() {
    }
}
