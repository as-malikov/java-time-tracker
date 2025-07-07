package ru.timetracker.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class TimeIntervalDTO {
    private String period; // "HH:MM"
    private String taskTitle;
    private boolean isWorkInterval; // true - работа, false - дыра
    private LocalDateTime startTime;
    private LocalDateTime endTime;
}
