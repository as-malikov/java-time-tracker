package ru.timetracker.dto.timeentry;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO для создания новой записи времени. Содержит минимальные данные для старта трекинга времени.
 */
@Data
@AllArgsConstructor
@Builder
public class TimeEntryCreateDTO {
    /**
     * ID задачи, для которой создается запись времени
     * @return ID задачи (обязательное поле)
     */
    @NotNull private Long taskId;

    /**
     * Конструктор по умолчанию, необходимый для Javadoc.
     */
    public TimeEntryCreateDTO() {
    }
}
