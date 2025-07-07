package ru.timetracker.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TimeEntryUpdateDTO {
    private LocalDateTime endTime;
}
