package ru.timetracker.dto;

import lombok.Data;

import java.time.Duration;
import java.time.LocalDateTime;

@Data
public class TimeEntryDTO {
    private Long id;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Duration duration;
    private Long userId;
    private Long taskId;
    private String taskTitle;
    private boolean active;
}
