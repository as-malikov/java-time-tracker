package ru.timetracker.dto.timeentry;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * DTO для обновления записи времени. Используется преимущественно для остановки трекинга.
 */
@Data
public class TimeEntryUpdateDTO {
    /**
     * Время окончания работы
     * @return Дата и время окончания
     */
    private LocalDateTime endTime;

    /**
     * Конструктор по умолчанию, необходимый для Javadoc.
     */
    public TimeEntryUpdateDTO() {
    }
}
