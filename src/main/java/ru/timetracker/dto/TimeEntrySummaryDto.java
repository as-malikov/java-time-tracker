package ru.timetracker.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TimeEntrySummaryDto {
    private Long taskId;
    private String taskTitle;
    private String totalTime; // Формат "HH:mm"
}
