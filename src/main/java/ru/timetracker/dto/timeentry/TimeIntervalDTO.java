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
public class TimeIntervalDTO {
    private String period; // "HH:MM"
    private String taskTitle;
    private boolean isWorkInterval; // true - работа, false - дыра
    private LocalDateTime startTime;
    private LocalDateTime endTime;
}
