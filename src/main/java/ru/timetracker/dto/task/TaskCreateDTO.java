package ru.timetracker.dto.task;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class TaskCreateDTO {
    @NotBlank
    @Size(min = 3, max = 100)
    private String title;

    @Size(max = 1000)
    private String description;
}
