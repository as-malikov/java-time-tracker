package ru.timetracker.dto.task;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TaskDurationDTO {
    private Long taskId;
    private String taskTitle;
    private String duration; // в формате "чч:мм"
    private LocalDateTime firstEntryTime;
}
