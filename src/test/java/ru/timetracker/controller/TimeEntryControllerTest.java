package ru.timetracker.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import ru.timetracker.dto.task.TaskDurationDTO;
import ru.timetracker.dto.timeentry.TimeEntryCreateDTO;
import ru.timetracker.dto.timeentry.TimeEntryDTO;
import ru.timetracker.dto.timeentry.TimeIntervalDTO;
import ru.timetracker.dto.timeentry.TotalWorkDurationDTO;
import ru.timetracker.service.TimeEntryService;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Тесты для {@link TimeEntryController}. Проверяют корректность работы с временными записями, включая старт/стоп трекинга, получение
 * статистики и очистку данных.
 * <p>Основные проверяемые сценарии:
 * <ul>
 *   <li>Создание и завершение временных записей</li>
 *   <li>Получение статистики по задачам и временным интервалам</li>
 *   <li>Очистка данных трекинга</li>
 *   <li>Корректность HTTP-статусов в ответах</li>
 * </ul>
 */
@ExtendWith(MockitoExtension.class)
class TimeEntryControllerTest {

    @Mock
    private TimeEntryService timeEntryService;

    @InjectMocks
    private TimeEntryController timeEntryController;

    /**
     * Проверяет успешное начало новой временной записи. Ожидаемое поведение:
     * <ul>
     *   <li>HTTP-статус 201 (Created)</li>
     *   <li>Тело ответа содержит созданную временную запись</li>
     *   <li>Вызов timeEntryService.startTimeEntry() с правильными параметрами</li>
     * </ul>
     */
    @Test
    void startTimeEntry_Success() {
        Long userId = 1L;
        TimeEntryCreateDTO createDTO = new TimeEntryCreateDTO();
        TimeEntryDTO createdEntry = new TimeEntryDTO();
        when(timeEntryService.startTimeEntry(userId, createDTO)).thenReturn(createdEntry);

        ResponseEntity<TimeEntryDTO> response = timeEntryController.startTimeEntry(userId, createDTO);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(createdEntry, response.getBody());
        verify(timeEntryService).startTimeEntry(userId, createDTO);
    }


    /**
     * Проверяет успешное завершение временной записи. Ожидаемое поведение:
     * <ul>
     *   <li>HTTP-статус 200 (OK)</li>
     *   <li>Тело ответа содержит завершенную запись</li>
     *   <li>Вызов timeEntryService.stopTimeEntry() с правильным ID записи</li>
     * </ul>
     */
    @Test
    void stopTimeEntry_Success() {
        Long timeEntryId = 1L;
        TimeEntryDTO stoppedEntry = new TimeEntryDTO();
        when(timeEntryService.stopTimeEntry(timeEntryId)).thenReturn(stoppedEntry);

        ResponseEntity<TimeEntryDTO> response = timeEntryController.stopTimeEntry(timeEntryId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(stoppedEntry, response.getBody());
        verify(timeEntryService).stopTimeEntry(timeEntryId);
    }

    /**
     * Проверяет успешное получение списка временных записей за период. Ожидаемое поведение:
     * <ul>
     *   <li>HTTP-статус 200 (OK)</li>
     *   <li>Тело ответа содержит список записей</li>
     *   <li>Вызов timeEntryService.getUserTimeEntries() с правильными параметрами</li>
     * </ul>
     */
    @Test
    void getTimeEntries_Success() {
        Long userId = 1L;
        LocalDateTime from = LocalDateTime.now()
                .minusDays(1);
        LocalDateTime to = LocalDateTime.now();
        List<TimeEntryDTO> entries = List.of(new TimeEntryDTO());
        when(timeEntryService.getUserTimeEntries(userId, from, to)).thenReturn(entries);

        ResponseEntity<List<TimeEntryDTO>> response = timeEntryController.getTimeEntries(userId, from, to);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(entries, response.getBody());
        verify(timeEntryService).getUserTimeEntries(userId, from, to);
    }

    /**
     * Проверяет успешное получение статистики по задачам за период. Ожидаемое поведение:
     * <ul>
     *   <li>HTTP-статус 200 (OK)</li>
     *   <li>Тело ответа содержит данные о продолжительности задач</li>
     *   <li>Вызов timeEntryService.getUserTaskDurations() с правильными параметрами</li>
     * </ul>
     */
    @Test
    void getUserTaskDurations_Success() {
        Long userId = 1L;
        LocalDateTime from = LocalDateTime.now()
                .minusDays(1);
        LocalDateTime to = LocalDateTime.now();
        List<TaskDurationDTO> durations = List.of(new TaskDurationDTO());
        when(timeEntryService.getUserTaskDurations(userId, from, to)).thenReturn(durations);

        ResponseEntity<List<TaskDurationDTO>> response = timeEntryController.getUserTaskDurations(userId, from, to);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(durations, response.getBody());
        verify(timeEntryService).getUserTaskDurations(userId, from, to);
    }

    /**
     * Проверяет успешное получение временных интервалов за период. Ожидаемое поведение:
     * <ul>
     *   <li>HTTP-статус 200 (OK)</li>
     *   <li>Тело ответа содержит список интервалов</li>
     *   <li>Вызов timeEntryService.getUserTimeIntervals() с правильными параметрами</li>
     * </ul>
     */
    @Test
    void getUserTimeIntervals_Success() {
        Long userId = 1L;
        LocalDateTime from = LocalDateTime.now()
                .minusDays(1);
        LocalDateTime to = LocalDateTime.now();
        List<TimeIntervalDTO> intervals = List.of(new TimeIntervalDTO());
        when(timeEntryService.getUserTimeIntervals(userId, from, to)).thenReturn(intervals);

        ResponseEntity<List<TimeIntervalDTO>> response = timeEntryController.getUserTimeIntervals(userId, from, to);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(intervals, response.getBody());
        verify(timeEntryService).getUserTimeIntervals(userId, from, to);
    }

    /**
     * Проверяет успешное получение общей продолжительности работы за период. Ожидаемое поведение:
     * <ul>
     *   <li>HTTP-статус 200 (OK)</li>
     *   <li>Тело ответа содержит суммарное время работы</li>
     *   <li>Вызов timeEntryService.getTotalWorkDuration() с правильными параметрами</li>
     * </ul>
     */
    @Test
    void getTotalWorkDuration_Success() {
        Long userId = 1L;
        LocalDateTime from = LocalDateTime.now()
                .minusDays(1);
        LocalDateTime to = LocalDateTime.now();
        TotalWorkDurationDTO duration = new TotalWorkDurationDTO();
        when(timeEntryService.getTotalWorkDuration(userId, from, to)).thenReturn(duration);

        ResponseEntity<TotalWorkDurationDTO> response = timeEntryController.getTotalWorkDuration(userId, from, to);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(duration, response.getBody());
        verify(timeEntryService).getTotalWorkDuration(userId, from, to);
    }

    /**
     * Проверяет успешную очистку данных трекинга пользователя. Ожидаемое поведение:
     * <ul>
     *   <li>HTTP-статус 204 (No Content)</li>
     *   <li>Вызов timeEntryService.clearUserTrackingData() с правильным ID пользователя</li>
     * </ul>
     */
    @Test
    void clearTrackingData_Success() {
        Long userId = 1L;

        ResponseEntity<Void> response = timeEntryController.clearTrackingData(userId);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(timeEntryService).clearUserTrackingData(userId);
    }
}