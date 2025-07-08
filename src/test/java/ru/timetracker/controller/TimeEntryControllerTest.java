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

@ExtendWith(MockitoExtension.class)
class TimeEntryControllerTest {

    @Mock
    private TimeEntryService timeEntryService;

    @InjectMocks
    private TimeEntryController timeEntryController;

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

    @Test
    void getTimeEntries_Success() {
        Long userId = 1L;
        LocalDateTime from = LocalDateTime.now().minusDays(1);
        LocalDateTime to = LocalDateTime.now();
        List<TimeEntryDTO> entries = List.of(new TimeEntryDTO());
        when(timeEntryService.getUserTimeEntries(userId, from, to)).thenReturn(entries);

        ResponseEntity<List<TimeEntryDTO>> response = timeEntryController.getTimeEntries(userId, from, to);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(entries, response.getBody());
        verify(timeEntryService).getUserTimeEntries(userId, from, to);
    }

    @Test
    void getUserTaskDurations_Success() {
        Long userId = 1L;
        LocalDateTime from = LocalDateTime.now().minusDays(1);
        LocalDateTime to = LocalDateTime.now();
        List<TaskDurationDTO> durations = List.of(new TaskDurationDTO());
        when(timeEntryService.getUserTaskDurations(userId, from, to)).thenReturn(durations);

        ResponseEntity<List<TaskDurationDTO>> response = timeEntryController.getUserTaskDurations(userId, from, to);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(durations, response.getBody());
        verify(timeEntryService).getUserTaskDurations(userId, from, to);
    }

    @Test
    void getUserTimeIntervals_Success() {
        Long userId = 1L;
        LocalDateTime from = LocalDateTime.now().minusDays(1);
        LocalDateTime to = LocalDateTime.now();
        List<TimeIntervalDTO> intervals = List.of(new TimeIntervalDTO());
        when(timeEntryService.getUserTimeIntervals(userId, from, to)).thenReturn(intervals);

        ResponseEntity<List<TimeIntervalDTO>> response = timeEntryController.getUserTimeIntervals(userId, from, to);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(intervals, response.getBody());
        verify(timeEntryService).getUserTimeIntervals(userId, from, to);
    }

    @Test
    void getTotalWorkDuration_Success() {
        Long userId = 1L;
        LocalDateTime from = LocalDateTime.now().minusDays(1);
        LocalDateTime to = LocalDateTime.now();
        TotalWorkDurationDTO duration = new TotalWorkDurationDTO();
        when(timeEntryService.getTotalWorkDuration(userId, from, to)).thenReturn(duration);

        ResponseEntity<TotalWorkDurationDTO> response = timeEntryController.getTotalWorkDuration(userId, from, to);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(duration, response.getBody());
        verify(timeEntryService).getTotalWorkDuration(userId, from, to);
    }

    @Test
    void clearTrackingData_Success() {
        Long userId = 1L;

        ResponseEntity<Void> response = timeEntryController.clearTrackingData(userId);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(timeEntryService).clearUserTrackingData(userId);
    }
}