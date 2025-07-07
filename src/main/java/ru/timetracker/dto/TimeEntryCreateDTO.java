package ru.timetracker.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class TimeEntryCreateDTO {
    @NotNull
    private Long taskId;
}
