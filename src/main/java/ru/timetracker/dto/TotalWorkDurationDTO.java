package ru.timetracker.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class TotalWorkDurationDTO {
    private String totalDuration; // Суммарное время в формате "чч:мм"
    private long totalSeconds; // Суммарное время в секундах (для сортировки/аналитики)
    private int days; // Количество дней в периоде
    private LocalDateTime periodStart;
    private LocalDateTime periodEnd;
}
