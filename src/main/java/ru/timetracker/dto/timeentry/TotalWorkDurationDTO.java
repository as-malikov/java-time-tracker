package ru.timetracker.dto.timeentry;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO для передачи данных об общем времени работы за период. Содержит информацию в различных форматах для аналитики.
 */
@Data
@Builder
@AllArgsConstructor
public class TotalWorkDurationDTO {

    /**
     * Суммарное время в формате "чч:мм"
     * @return Форматированная длительность
     */
    private String totalDuration;

    /**
     * Суммарное время в секундах
     * @return Длительность в секундах (для вычислений)
     */
    private long totalSeconds;

    /**
     * Количество дней в анализируемом периоде
     * @return Число дней
     */
    private int days;

    /**
     * Начало анализируемого периода
     * @return Дата и время начала
     */
    private LocalDateTime periodStart;

    /**
     * Окончание анализируемого периода
     * @return Дата и время окончания
     */
    private LocalDateTime periodEnd;

    /**
     * Конструктор по умолчанию, необходимый для Javadoc.
     */
    public TotalWorkDurationDTO() {
    }
}
