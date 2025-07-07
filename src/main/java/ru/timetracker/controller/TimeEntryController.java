package ru.timetracker.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.Data;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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
public class TimeEntryController {
    private static final Logger logger = LogManager.getLogger(TimeEntryController.class);
    private final TimeEntryService timeEntryService;

    @PostMapping("/start")
    public ResponseEntity<TimeEntryDTO> startTimeEntry(
            @PathVariable Long userId,
            @RequestBody @Valid TimeEntryCreateDTO dto) {

        logger.info("Starting time entry for user {} with data: {}", userId, dto);

        try {
            TimeEntryDTO createdEntry = timeEntryService.startTimeEntry(userId, dto);
            logger.info("Successfully started time entry. ID: {}, Task: {}, Start: {}",
                    createdEntry.getId(), createdEntry.getTaskId(), createdEntry.getStartTime());
            return ResponseEntity.status(HttpStatus.CREATED).body(createdEntry);
        } catch (Exception e) {
            logger.error("Failed to start time entry for user {}. Error: {}", userId, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/{timeEntryId}/stop")
    public ResponseEntity<TimeEntryDTO> stopTimeEntry(@PathVariable Long timeEntryId) {
        logger.info("Stopping time entry ID: {}", timeEntryId);

        try {
            TimeEntryDTO timeEntry = timeEntryService.stopTimeEntry(timeEntryId);
            logger.info("Successfully stopped time entry ID: {}. Duration: {} minutes",
                    timeEntryId, timeEntry.getDuration());
            return ResponseEntity.ok(timeEntry);
        } catch (Exception e) {
            logger.error("Error stopping time entry ID: {}. Error: {}", timeEntryId, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping
    public ResponseEntity<List<TimeEntryDTO>> getTimeEntries(
            @PathVariable Long userId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {

        logger.debug("Getting time entries for user {} (from: {}, to: {})", userId, from, to);

        try {
            List<TimeEntryDTO> entries = timeEntryService.getUserTimeEntries(userId, from, to);
            logger.debug("Retrieved {} time entries for user {}", entries.size(), userId);
            return ResponseEntity.ok(entries);
        } catch (Exception e) {
            logger.error("Failed to get time entries for user {}. Error: {}", userId, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/task-durations")
    public ResponseEntity<List<TaskDurationDTO>> getUserTaskDurations(
            @PathVariable Long userId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {

        logger.debug("Getting task durations for user {} (from: {}, to: {})", userId, from, to);

        try {
            List<TaskDurationDTO> durations = timeEntryService.getUserTaskDurations(userId, from, to);
            logger.debug("Retrieved {} task durations for user {}", durations.size(), userId);
            return ResponseEntity.ok(durations);
        } catch (Exception e) {
            logger.error("Failed to get task durations for user {}. Error: {}", userId, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/time-intervals")
    public ResponseEntity<List<TimeIntervalDTO>> getUserTimeIntervals(
            @PathVariable Long userId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {

        logger.debug("Getting time intervals for user {} (from: {}, to: {})", userId, from, to);

        try {
            List<TimeIntervalDTO> intervals = timeEntryService.getUserTimeIntervals(userId, from, to);
            logger.debug("Retrieved {} time intervals for user {}", intervals.size(), userId);
            return ResponseEntity.ok(intervals);
        } catch (Exception e) {
            logger.error("Failed to get time intervals for user {}. Error: {}", userId, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/total-work-duration")
    public ResponseEntity<TotalWorkDurationDTO> getTotalWorkDuration(
            @PathVariable Long userId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {

        logger.debug("Getting total work duration for user {} (from: {}, to: {})", userId, from, to);

        try {
            TotalWorkDurationDTO duration = timeEntryService.getTotalWorkDuration(userId, from, to);
            logger.info("Total work duration for user {}: {} minutes", userId, duration.getTotalDuration());
            return ResponseEntity.ok(duration);
        } catch (Exception e) {
            logger.error("Failed to get total work duration for user {}. Error: {}", userId, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @DeleteMapping("/tracking-data")
    public ResponseEntity<Void> clearTrackingData(@PathVariable Long userId) {
        logger.warn("Clearing tracking data for user {}", userId);

        try {
            timeEntryService.clearUserTrackingData(userId);
            logger.warn("Successfully cleared tracking data for user {}", userId);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            logger.error("Failed to clear tracking data for user {}. Error: {}", userId, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
}