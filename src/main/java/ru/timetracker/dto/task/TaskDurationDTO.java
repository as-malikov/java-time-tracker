package ru.timetracker.dto.task;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * DTO для передачи данных о продолжительности работы над задачей. Используется для аналитики и отчетов по времени.
 * <p>Содержит:
 * <ul>
 *   <li>Идентификатор и название задачи</li>
 *   <li>Суммарную продолжительность в формате "чч:мм"</li>
 *   <li>Дату первой записи времени</li>
 * </ul>
 */
@Data
@Builder
@AllArgsConstructor
public class TaskDurationDTO {
    /**
     * Идентификатор задачи
     * @return ID задачи
     */
    private Long taskId;

    /**
     * Название задачи
     * @return Название задачи
     */
    private String taskTitle;

    /**
     * Суммарная продолжительность работы
     * @return Продолжительность в формате "чч:мм"
     */
    private String duration;

    /**
     * Дата и время первой записи
     * @return Дата первой записи времени
     */
    private LocalDateTime firstEntryTime;

    /**
     * Конструктор по умолчанию, необходимый для Javadoc.
     */
    public TaskDurationDTO() {}
}
