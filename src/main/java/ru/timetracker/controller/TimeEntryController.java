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

@Slf4j
@RestController
@RequestMapping("/api/v1/users/{userId}/time-entries")
@Data
@Tag(name = "Time Entries", description = "Управление записями времени")
public class TimeEntryController {
    private final TimeEntryService timeEntryService;

    @PostMapping("/start")
    public ResponseEntity<TimeEntryDTO> startTimeEntry(
            @PathVariable Long userId,
            @RequestBody @Valid TimeEntryCreateDTO dto) {

        log.info("Starting time entry for user {} with task {}",
                userId, dto.getTaskId());

        try {
            TimeEntryDTO createdEntry = timeEntryService.startTimeEntry(userId, dto);
            log.info("Time entry started successfully. Entry ID: {}, User ID: {}",
                    createdEntry.getId(), userId);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdEntry);
        } catch (Exception e) {
            log.error("Failed to start time entry for user {}. Task: {}",
                    userId, dto.getTaskId(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/{timeEntryId}/stop")
    public ResponseEntity<TimeEntryDTO> stopTimeEntry(@PathVariable Long timeEntryId) {
        log.info("Stopping time entry ID: {}", timeEntryId);

        try {
            TimeEntryDTO timeEntry = timeEntryService.stopTimeEntry(timeEntryId);
            log.info("Time entry stopped successfully. ID: {}, Duration: {} minutes",
                    timeEntryId, timeEntry.getDuration());
            return ResponseEntity.ok(timeEntry);
        } catch (Exception e) {
            log.error("Error stopping time entry ID: {}. Error: {}",
                    timeEntryId, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping
    public ResponseEntity<List<TimeEntryDTO>> getTimeEntries(
            @PathVariable Long userId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {

        log.debug("Fetching time entries for user {}. Period: {} - {}",
                userId, from, to);

        try {
            List<TimeEntryDTO> entries = timeEntryService.getUserTimeEntries(userId, from, to);
            log.info("Retrieved {} time entries for user {}", entries.size(), userId);
            return ResponseEntity.ok(entries);
        } catch (Exception e) {
            log.error("Failed to get time entries for user {}. Error: {}",
                    userId, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/task-durations")
    public ResponseEntity<List<TaskDurationDTO>> getUserTaskDurations(
            @PathVariable Long userId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {

        log.debug("Calculating task durations for user {}. Period: {} - {}",
                userId, from, to);

        try {
            List<TaskDurationDTO> durations = timeEntryService.getUserTaskDurations(userId, from, to);
            log.info("Calculated durations for {} tasks for user {}",
                    durations.size(), userId);
            return ResponseEntity.ok(durations);
        } catch (Exception e) {
            log.error("Failed to calculate task durations for user {}. Error: {}",
                    userId, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/time-intervals")
    public ResponseEntity<List<TimeIntervalDTO>> getUserTimeIntervals(
            @PathVariable Long userId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {

        log.debug("Fetching time intervals for user {}. Period: {} - {}",
                userId, from, to);

        try {
            List<TimeIntervalDTO> intervals = timeEntryService.getUserTimeIntervals(userId, from, to);
            log.info("Retrieved {} time intervals for user {}", intervals.size(), userId);
            return ResponseEntity.ok(intervals);
        } catch (Exception e) {
            log.error("Failed to get time intervals for user {}. Error: {}",
                    userId, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/total-work-duration")
    public ResponseEntity<TotalWorkDurationDTO> getTotalWorkDuration(
            @PathVariable Long userId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {

        log.debug("Calculating total work duration for user {}. Period: {} - {}",
                userId, from, to);

        try {
            TotalWorkDurationDTO duration = timeEntryService.getTotalWorkDuration(userId, from, to);
            log.info("Total work duration for user {}: {} minutes",
                    userId, duration.getTotalDuration());
            return ResponseEntity.ok(duration);
        } catch (Exception e) {
            log.error("Failed to calculate total work duration for user {}. Error: {}",
                    userId, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @DeleteMapping("/tracking-data")
    public ResponseEntity<Void> clearTrackingData(@PathVariable Long userId) {
        log.warn("Clearing ALL tracking data for user {}", userId);

        try {
            timeEntryService.clearUserTrackingData(userId);
            log.warn("Successfully cleared tracking data for user {}", userId);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            log.error("Failed to clear tracking data for user {}. Error: {}",
                    userId, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
}