package ru.timetracker.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TimeEntryResponseDto {
    private Long id;
    private Long taskId;
    private String taskTitle;
    private Long userId;
    private String userLogin;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String duration;
    private String description;
    private LocalDateTime createdAt;
}
