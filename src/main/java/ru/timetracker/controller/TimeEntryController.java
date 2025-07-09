package ru.timetracker.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.Data;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.timetracker.dto.task.TaskDurationDTO;
import ru.timetracker.dto.timeentry.TimeEntryCreateDTO;
import ru.timetracker.dto.timeentry.TimeEntryDTO;
import ru.timetracker.dto.timeentry.TimeIntervalDTO;
import ru.timetracker.dto.timeentry.TotalWorkDurationDTO;
import ru.timetracker.service.TimeEntryService;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/users/{userId}/time-entries")
@Data
@Tag(name = "Time Entries", description = "API для управления записями времени и трекингом рабочего времени")
public class TimeEntryController {
    private static final Logger logger = LogManager.getLogger(TimeEntryController.class);
    private final TimeEntryService timeEntryService;

    @Operation(summary = "Начать запись времени",
               description = "Создает новую запись времени с указанием задачи и времени начала")
    @ApiResponses(value = {@ApiResponse(responseCode = "201", description = "Запись времени успешно создана",
                                        content = @Content(mediaType = "application/json",
                                                           schema = @Schema(implementation = TimeEntryDTO.class))),
            @ApiResponse(responseCode = "400", description = "Некорректные входные данные"),
            @ApiResponse(responseCode = "404", description = "Пользователь или задача не найдены"),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера")})
    @PostMapping("/start")
    public ResponseEntity<TimeEntryDTO> startTimeEntry(
            @Parameter(description = "ID пользователя", required = true) @PathVariable Long userId,
            @Parameter(description = "Данные для создания записи времени", required = true) @RequestBody @Valid
            TimeEntryCreateDTO dto) {

        logger.info("Starting time entry for user {} with data: {}", userId, dto);

        try {
            TimeEntryDTO createdEntry = timeEntryService.startTimeEntry(userId, dto);
            logger.info("Successfully started time entry. ID: {}, Task: {}, Start: {}", createdEntry.getId(),
                    createdEntry.getTaskId(), createdEntry.getStartTime());
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(createdEntry);
        } catch (Exception e) {
            logger.error("Failed to start time entry for user {}. Error: {}", userId, e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .build();
        }
    }

    @Operation(summary = "Остановить запись времени",
               description = "Останавливает активную запись времени и фиксирует продолжительность")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Запись времени успешно остановлена",
                                        content = @Content(mediaType = "application/json",
                                                           schema = @Schema(implementation = TimeEntryDTO.class))),
            @ApiResponse(responseCode = "404", description = "Запись времени не найдена"),
            @ApiResponse(responseCode = "409", description = "Запись времени уже остановлена"),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера")})
    @PostMapping("/{timeEntryId}/stop")
    public ResponseEntity<TimeEntryDTO> stopTimeEntry(
            @Parameter(description = "ID записи времени для остановки", required = true) @PathVariable
            Long timeEntryId) {
        logger.info("Stopping time entry ID: {}", timeEntryId);

        try {
            TimeEntryDTO timeEntry = timeEntryService.stopTimeEntry(timeEntryId);
            logger.info("Successfully stopped time entry ID: {}. Duration: {} minutes", timeEntryId,
                    timeEntry.getDuration());
            return ResponseEntity.ok(timeEntry);
        } catch (Exception e) {
            logger.error("Error stopping time entry ID: {}. Error: {}", timeEntryId, e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .build();
        }
    }

    @Operation(summary = "Получить записи времени",
               description = "Возвращает список записей времени пользователя за указанный период")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Список записей времени успешно получен",
                                        content = @Content(mediaType = "application/json",
                                                           schema = @Schema(implementation = TimeEntryDTO.class,
                                                                            type = "array"))),
            @ApiResponse(responseCode = "400", description = "Некорректные параметры даты"),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера")})
    @GetMapping
    public ResponseEntity<List<TimeEntryDTO>> getTimeEntries(
            @Parameter(description = "ID пользователя", required = true) @PathVariable Long userId,
            @Parameter(description = "Начальная дата периода (формат ISO 8601)", example = "2023-01-01T00:00:00")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @Parameter(description = "Конечная дата периода (формат ISO 8601)", example = "2023-12-31T23:59:59")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {

        logger.debug("Getting time entries for user {} (from: {}, to: {})", userId, from, to);

        try {
            List<TimeEntryDTO> entries = timeEntryService.getUserTimeEntries(userId, from, to);
            logger.debug("Retrieved {} time entries for user {}", entries.size(), userId);
            return ResponseEntity.ok(entries);
        } catch (Exception e) {
            logger.error("Failed to get time entries for user {}. Error: {}", userId, e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .build();
        }
    }

    @Operation(summary = "Получить продолжительность по задачам",
               description = "Возвращает суммарную продолжительность работы по каждой задаче за указанный период")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Данные о продолжительности успешно получены",
                         content = @Content(mediaType = "application/json",
                                            schema = @Schema(implementation = TaskDurationDTO.class, type = "array"))),
            @ApiResponse(responseCode = "400", description = "Некорректные параметры даты"),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера")})
    @GetMapping("/task-durations")
    public ResponseEntity<List<TaskDurationDTO>> getUserTaskDurations(
            @Parameter(description = "ID пользователя", required = true) @PathVariable Long userId,
            @Parameter(description = "Начальная дата периода (формат ISO 8601)", example = "2023-01-01T00:00:00")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @Parameter(description = "Конечная дата периода (формат ISO 8601)", example = "2023-12-31T23:59:59")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {

        logger.debug("Getting task durations for user {} (from: {}, to: {})", userId, from, to);

        try {
            List<TaskDurationDTO> durations = timeEntryService.getUserTaskDurations(userId, from, to);
            logger.debug("Retrieved {} task durations for user {}", durations.size(), userId);
            return ResponseEntity.ok(durations);
        } catch (Exception e) {
            logger.error("Failed to get task durations for user {}. Error: {}", userId, e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .build();
        }
    }

    @Operation(summary = "Получить временные интервалы",
               description = "Возвращает временные интервалы работы пользователя за указанный период")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Временные интервалы успешно получены",
                                        content = @Content(mediaType = "application/json",
                                                           schema = @Schema(implementation = TimeIntervalDTO.class,
                                                                            type = "array"))),
            @ApiResponse(responseCode = "400", description = "Некорректные параметры даты"),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера")})
    @GetMapping("/time-intervals")
    public ResponseEntity<List<TimeIntervalDTO>> getUserTimeIntervals(
            @Parameter(description = "ID пользователя", required = true) @PathVariable Long userId,
            @Parameter(description = "Начальная дата периода (формат ISO 8601)", example = "2023-01-01T00:00:00")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @Parameter(description = "Конечная дата периода (формат ISO 8601)", example = "2023-12-31T23:59:59")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {

        logger.debug("Getting time intervals for user {} (from: {}, to: {})", userId, from, to);

        try {
            List<TimeIntervalDTO> intervals = timeEntryService.getUserTimeIntervals(userId, from, to);
            logger.debug("Retrieved {} time intervals for user {}", intervals.size(), userId);
            return ResponseEntity.ok(intervals);
        } catch (Exception e) {
            logger.error("Failed to get time intervals for user {}. Error: {}", userId, e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .build();
        }
    }

    @Operation(summary = "Получить общую продолжительность работы",
               description = "Возвращает общую продолжительность работы пользователя за указанный период")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Общая продолжительность успешно получена",
                                        content = @Content(mediaType = "application/json", schema = @Schema(
                                                implementation = TotalWorkDurationDTO.class))),
            @ApiResponse(responseCode = "400", description = "Некорректные параметры даты"),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера")})
    @GetMapping("/total-work-duration")
    public ResponseEntity<TotalWorkDurationDTO> getTotalWorkDuration(
            @Parameter(description = "ID пользователя", required = true) @PathVariable Long userId,
            @Parameter(description = "Начальная дата периода (формат ISO 8601)", example = "2023-01-01T00:00:00")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @Parameter(description = "Конечная дата периода (формат ISO 8601)", example = "2023-12-31T23:59:59")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {

        logger.debug("Getting total work duration for user {} (from: {}, to: {})", userId, from, to);

        try {
            TotalWorkDurationDTO duration = timeEntryService.getTotalWorkDuration(userId, from, to);
            logger.info("Total work duration for user {}: {} minutes", userId, duration.getTotalDuration());
            return ResponseEntity.ok(duration);
        } catch (Exception e) {
            logger.error("Failed to get total work duration for user {}. Error: {}", userId, e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .build();
        }
    }

    @Operation(summary = "Очистить данные трекинга",
               description = "Удаляет все записи времени и связанные данные для указанного пользователя")
    @ApiResponses(value = {@ApiResponse(responseCode = "204", description = "Данные трекинга успешно очищены"),
            @ApiResponse(responseCode = "404", description = "Пользователь не найден"),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера")})
    @DeleteMapping("/tracking-data")
    public ResponseEntity<Void> clearTrackingData(
            @Parameter(description = "ID пользователя", required = true) @PathVariable Long userId) {
        logger.warn("Clearing tracking data for user {}", userId);

        try {
            timeEntryService.clearUserTrackingData(userId);
            logger.warn("Successfully cleared tracking data for user {}", userId);
            return ResponseEntity.noContent()
                    .build();
        } catch (Exception e) {
            logger.error("Failed to clear tracking data for user {}. Error: {}", userId, e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .build();
        }
    }
}