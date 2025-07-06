package ru.timetracker.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TimeEntryRequestDto {
    @NotNull(message = "Task ID cannot be null")
    private Long taskId;

    @NotNull(message = "User ID cannot be null")
    private Long userId;

    @FutureOrPresent(message = "Start time must be in present or future")
    private LocalDateTime startTime;

    @Size(max = 500, message = "Описание не должно превышать 500 символов")
    private String description;
}
