package ru.timetracker.dto;

import lombok.Data;

import java.time.Duration;
import java.util.List;

public class TimeSummaryDTO {
    private List<TaskDurationDTO> tasks;
    private Duration totalDuration;

    @Data
    public static class TaskDurationDTO {
        private Long taskId;
        private String taskTitle;
        private Duration duration;
    }
}

