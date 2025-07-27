package ru.timetracker.dto.task;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class TaskDurationDTO {
    private Long taskId;
    private String taskTitle;
    private String duration; // в формате "чч:мм"
    private LocalDateTime firstEntryTime;
}
