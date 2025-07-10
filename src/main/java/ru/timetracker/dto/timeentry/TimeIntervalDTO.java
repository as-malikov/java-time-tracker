package ru.timetracker.dto.timeentry;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO для передачи данных о временных интервалах.
 * Используется для визуализации графика работы/отдыха.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TimeIntervalDTO {
    /**
     * Продолжительность интервала в формате "чч:мм"
     * @return Форматированная длительность
     */
    private String period;

    /**
     * Название задачи (для рабочих интервалов)
     * @return Название задачи или "Неактивность"
     */
    private String taskTitle;

    /**
     * Тип интервала
     * @return true - рабочий интервал, false - перерыв
     */
    private boolean isWorkInterval;

    /**
     * Время начала интервала
     * @return Дата и время начала
     */
    private LocalDateTime startTime;

    /**
     * Время окончания интервала
     * @return Дата и время окончания
     */
    private LocalDateTime endTime;

    /**
     * Конструктор по умолчанию, необходимый для Javadoc.
     */
    public TimeIntervalDTO() {
    }
}
