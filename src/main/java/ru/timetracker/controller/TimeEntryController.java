package ru.timetracker.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.timetracker.dto.TimeEntryRequestDto;
import ru.timetracker.dto.TimeEntryResponseDto;
import ru.timetracker.dto.TimeEntrySummaryDto;
import ru.timetracker.service.TimeEntryService;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@RestController
@RequestMapping("/api/v1/time-entries")
@Data
@Tag(name = "Time Entries", description = "Управление записями времени")
public class TimeEntryController {
    private final TimeEntryService timeEntryService;

    @Operation(summary = "Начать отсчет времени")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Таймер успешно запущен"),
            @ApiResponse(responseCode = "400", description = "Неверные параметры запроса"),
            @ApiResponse(responseCode = "404", description = "Задача или пользователь не найдены")
    })
    @PostMapping("/start")
    public ResponseEntity<TimeEntryResponseDto> startTracking(
            @Valid @RequestBody TimeEntryRequestDto timeEntryRequestDto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(timeEntryService.startTracking(timeEntryRequestDto));
    }

    @PostMapping("/{id}/stop")
    public ResponseEntity<TimeEntryResponseDto> stopTracking(@PathVariable Long id) {
        return ResponseEntity.ok(timeEntryService.stopTracking(id));
    }

    @GetMapping("/task/{taskId}")
    public ResponseEntity<List<TimeEntryResponseDto>> getTimeEntriesByTask(
            @PathVariable Long taskId) {
        return ResponseEntity.ok(timeEntryService.getTimeEntriesByTask(taskId));
    }

    @GetMapping("/summary/{userId}")
    public ResponseEntity<List<TimeEntrySummaryDto>> getTimeSummary(
            @PathVariable Long userId,
            @RequestParam(required = false, defaultValue = "#{T(java.time.LocalDate).now().minusDays(7)}")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam(required = false, defaultValue = "#{T(java.time.LocalDate).now()}")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {
        // Проверка валидности диапазона дат
        if (start.isAfter(end)) {
            throw new IllegalArgumentException("Дата начала не может быть позже даты окончания");
        }
        // Ограничение максимального диапазона (например, не больше 1 года)
        if (ChronoUnit.DAYS.between(start, end) > 365) {
            throw new IllegalArgumentException("Максимальный диапазон - 1 год");
        }

        return ResponseEntity.ok(timeEntryService.getTimeSummary(userId, start, end));
    }
}
