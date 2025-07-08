package ru.timetracker.dto.timeentry;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TotalWorkDurationDTO {
    private String totalDuration; // Суммарное время в формате "чч:мм"
    private long totalSeconds; // Суммарное время в секундах (для сортировки/аналитики)
    private int days; // Количество дней в периоде
    private LocalDateTime periodStart;
    private LocalDateTime periodEnd;
}
