package ru.timetracker.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TaskDTO {
    private Long id;
    private String title;
    private String description;
    private LocalDateTime createdAt;
    private Long userId;
    private boolean active;
}
