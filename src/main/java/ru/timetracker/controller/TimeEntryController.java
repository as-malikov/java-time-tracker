package ru.timetracker.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.timetracker.dto.*;
import ru.timetracker.service.TimeEntryService;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/users/{userId}/time-entries")
@Data
@Tag(name = "Time Entries", description = "Управление записями времени")
@Slf4j
public class TimeEntryController {
    private final TimeEntryService timeEntryService;

    @PostMapping("/start")
    @ResponseStatus(HttpStatus.CREATED)
    public TimeEntryDTO startTimeEntry(@PathVariable Long userId, @RequestBody @Valid TimeEntryCreateDTO dto) {
        return timeEntryService.startTimeEntry(userId, dto);
    }

    @PostMapping("/{timeEntryId}/stop")
    public ResponseEntity<TimeEntryDTO> stopTimeEntry(@PathVariable Long timeEntryId) {
        log.info("Request to stop time entry ID: {}", timeEntryId);
        try {
            TimeEntryDTO timeEntry = timeEntryService.stopTimeEntry(timeEntryId);
            log.info("Successfully stopped time entry ID: {}", timeEntryId);
            return ResponseEntity.ok(timeEntry);
        } catch (Exception e) {
            log.error("Error stopping time entry ID: {}", timeEntryId, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping
    public List<TimeEntryDTO> getTimeEntries(@PathVariable Long userId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to)
    {
        return timeEntryService.getUserTimeEntries(userId, from, to);
    }

    @GetMapping("/task-durations")
    public List<TaskDurationDTO> getUserTaskDurations(@PathVariable Long userId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to)
    {
        return timeEntryService.getUserTaskDurations(userId, from, to);
    }

    @GetMapping("/time-intervals")
    public List<TimeIntervalDTO> getUserTimeIntervals(
            @PathVariable Long userId,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {
        return timeEntryService.getUserTimeIntervals(userId, from, to);
    }

    @GetMapping("/total-work-duration")
    public TotalWorkDurationDTO getTotalWorkDuration(
            @PathVariable Long userId,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {

        return timeEntryService.getTotalWorkDuration(userId, from, to);
    }

    @DeleteMapping(("/tracking-data"))
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void clearTrackingData(@PathVariable Long userId) {
        timeEntryService.clearUserTrackingData(userId);
    }
}
