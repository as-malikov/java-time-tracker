package ru.timetracker.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.timetracker.dto.mapper.TimeEntryMapper;
import ru.timetracker.dto.task.TaskDurationDTO;
import ru.timetracker.dto.timeentry.TimeEntryCreateDTO;
import ru.timetracker.dto.timeentry.TimeEntryDTO;
import ru.timetracker.dto.timeentry.TimeIntervalDTO;
import ru.timetracker.dto.timeentry.TotalWorkDurationDTO;
import ru.timetracker.exception.ResourceNotFoundException;
import ru.timetracker.model.Task;
import ru.timetracker.model.TimeEntry;
import ru.timetracker.model.User;
import ru.timetracker.repository.TaskRepository;
import ru.timetracker.repository.TimeEntryRepository;
import ru.timetracker.repository.UserRepository;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TimeEntryServiceTest {

    @Mock
    private TimeEntryRepository timeEntryRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private TimeEntryMapper timeEntryMapper;

    @InjectMocks
    private TimeEntryService timeEntryService;

    private final Long userId = 1L;
    private final Long taskId = 1L;
    private final Long entryId = 1L;
    private final LocalDateTime now = LocalDateTime.now();
    private final LocalDateTime startTime = now.minusHours(1);
    private final LocalDateTime endTime = now;

    @Test
    void startTimeEntry_ShouldCreateNewEntry() {
        // Arrange
        TimeEntryCreateDTO dto = new TimeEntryCreateDTO(taskId);
        User user = new User();
        user.setId(userId);
        Task task = new Task();
        task.setId(taskId);
        task.setUser(user);
        TimeEntry entry = new TimeEntry();
        TimeEntryDTO entryDTO = new TimeEntryDTO();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
        when(timeEntryRepository.findByUserAndEndTimeIsNull(user)).thenReturn(Optional.empty());
        when(timeEntryRepository.save(any(TimeEntry.class))).thenReturn(entry);
        when(timeEntryMapper.toDTO(entry)).thenReturn(entryDTO);

        // Act
        TimeEntryDTO result = timeEntryService.startTimeEntry(userId, dto);

        // Assert
        assertNotNull(result);
        verify(timeEntryRepository).save(any(TimeEntry.class));
    }

    @Test
    void startTimeEntry_ShouldStopPreviousActiveEntry() {
        // Arrange
        TimeEntryCreateDTO dto = new TimeEntryCreateDTO(taskId);
        User user = new User();
        user.setId(userId);
        Task task = new Task();
        task.setId(taskId);
        task.setUser(user);
        TimeEntry activeEntry = new TimeEntry();
        TimeEntry newEntry = new TimeEntry();
        TimeEntryDTO entryDTO = new TimeEntryDTO();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
        when(timeEntryRepository.findByUserAndEndTimeIsNull(user)).thenReturn(Optional.of(activeEntry));
        when(timeEntryRepository.save(any(TimeEntry.class))).thenReturn(newEntry);
        when(timeEntryMapper.toDTO(newEntry)).thenReturn(entryDTO);

        // Act
        TimeEntryDTO result = timeEntryService.startTimeEntry(userId, dto);

        // Assert
        assertNotNull(result);
        assertNotNull(activeEntry.getEndTime());
        verify(timeEntryRepository, times(2)).save(any(TimeEntry.class));
    }

    @Test
    void startTimeEntry_ShouldThrowException_WhenTaskNotBelongsToUser() {
        // Arrange
        TimeEntryCreateDTO dto = new TimeEntryCreateDTO(taskId);
        User user = new User();
        user.setId(userId);
        Task task = new Task();
        task.setId(taskId);
        task.setUser(User.builder().id(2L).build()); // Другой пользователь

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));

        // Act & Assert
        assertThrows(ResourceNotFoundException.class,
                () -> timeEntryService.startTimeEntry(userId, dto));
    }

    @Test
    void stopTimeEntry_ShouldStopActiveEntry() {
        // Arrange
        User user = new User();
        TimeEntry activeEntry = new TimeEntry();
        TimeEntryDTO entryDTO = new TimeEntryDTO();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(timeEntryRepository.findByUserAndEndTimeIsNull(user)).thenReturn(Optional.of(activeEntry));
        when(timeEntryRepository.save(activeEntry)).thenReturn(activeEntry);
        when(timeEntryMapper.toDTO(activeEntry)).thenReturn(entryDTO);

        // Act
        TimeEntryDTO result = timeEntryService.stopTimeEntry(userId);

        // Assert
        assertNotNull(result);
        assertNotNull(activeEntry.getEndTime());
        verify(timeEntryRepository).save(activeEntry);
    }

    @Test
    void stopTimeEntry_ShouldThrowException_WhenNoActiveEntry() {
        // Arrange
        User user = new User();
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(timeEntryRepository.findByUserAndEndTimeIsNull(user)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(IllegalStateException.class,
                () -> timeEntryService.stopTimeEntry(userId));
    }

    @Test
    void getUserTimeEntries_ShouldReturnEntriesForPeriod() {
        // Arrange
        User user = new User();
        TimeEntry entry = new TimeEntry();
        TimeEntryDTO entryDTO = new TimeEntryDTO();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(timeEntryRepository.findByUserAndStartTimeBetweenOrderByStartTime(
                user, startTime, endTime)).thenReturn(List.of(entry));
        when(timeEntryMapper.toDTO(entry)).thenReturn(entryDTO);

        // Act
        List<TimeEntryDTO> result = timeEntryService.getUserTimeEntries(userId, startTime, endTime);

        // Assert
        assertEquals(1, result.size());
        assertEquals(entryDTO, result.get(0));
    }

    @Test
    void getUserTimeEntries_ShouldUseDefaultPeriod_WhenNull() {
        // Arrange
        User user = new User();
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        // Act
        timeEntryService.getUserTimeEntries(userId, null, null);

        // Assert
        verify(timeEntryRepository).findByUserAndStartTimeBetweenOrderByStartTime(
                any(User.class), any(LocalDateTime.class), any(LocalDateTime.class));
    }

    @Test
    void getUserTaskDurations_ShouldCalculateDurations() {
        // Arrange
        Object[] dbRow = new Object[]{taskId, "Task 1", 3600L}; // 1 hour
        TaskDurationDTO durationDTO = new TaskDurationDTO(taskId, "Task 1", "01:00", startTime);

        when(timeEntryRepository.findTaskDurationsByUserAndPeriod(userId, startTime, endTime))
                .thenReturn(List.<Object[]>of(dbRow));
        when(timeEntryRepository.findFirstByUserIdAndTaskIdOrderByStartTimeAsc(userId, taskId))
                .thenReturn(Optional.of(new TimeEntry()));

        // Act
        List<TaskDurationDTO> result = timeEntryService.getUserTaskDurations(userId, startTime, endTime);

        // Assert
        assertEquals(1, result.size());
        assertEquals("01:00", result.get(0).getDuration());
    }

    @Test
    void getUserTimeIntervals_ShouldCalculateIntervals() {
        // Arrange
        User user = new User();
        TimeEntry entry = new TimeEntry();
        entry.setStartTime(startTime);
        entry.setEndTime(endTime);
        Task task = new Task();
        task.setTitle("Task 1");
        entry.setTask(task);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(timeEntryRepository.findByUserAndStartTimeBetweenOrderByStartTime(user, startTime, endTime))
                .thenReturn(List.of(entry));

        // Act
        List<TimeIntervalDTO> result = timeEntryService.getUserTimeIntervals(userId, startTime, endTime);

        // Assert
        assertEquals(1, result.size()); // 1 активный + 1 неактивный интервал
        assertTrue(result.get(0).isWorkInterval());
        assertEquals("Task 1", result.get(0).getTaskTitle());
    }

    @Test
    void getTotalWorkDuration_ShouldCalculateTotal() {
        // Arrange
        when(timeEntryRepository.sumWorkDurationByUserAndPeriod(userId, startTime, endTime))
                .thenReturn(7200L); // 2 hours

        // Act
        TotalWorkDurationDTO result = timeEntryService.getTotalWorkDuration(userId, startTime, endTime);

        // Assert
        assertEquals("02:00", result.getTotalDuration());
        assertEquals(7200L, result.getTotalSeconds());
    }

    @Test
    void clearUserTrackingData_ShouldDeleteAllUserData() {
        // Arrange
        User user = new User();
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        // Act
        timeEntryService.clearUserTrackingData(userId);

        // Assert
        verify(timeEntryRepository).deleteByUser(user);
        verify(taskRepository).findByUser(user);
    }

    // Edge case tests
    @Test
    void getUserTimeEntries_ShouldHandleSingleParameter() {
        // Test with only 'from' parameter
        User user = new User();
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        timeEntryService.getUserTimeEntries(userId, startTime, null);
        verify(timeEntryRepository).findByUserAndStartTimeBetweenOrderByStartTime(
                eq(user), eq(startTime), any(LocalDateTime.class));

        // Test with only 'to' parameter
        timeEntryService.getUserTimeEntries(userId, null, endTime);
        verify(timeEntryRepository).findByUserAndStartTimeBetweenOrderByStartTime(
                eq(user), any(LocalDateTime.class), eq(endTime));
    }

    @Test
    void getUserTaskDurations_ShouldHandleEmptyResults() {
        when(timeEntryRepository.findTaskDurationsByUserAndPeriod(userId, startTime, endTime))
                .thenReturn(List.of());

        List<TaskDurationDTO> result = timeEntryService.getUserTaskDurations(userId, startTime, endTime);
        assertTrue(result.isEmpty());
    }

    @Test
    void getTotalWorkDuration_ShouldHandleNullResult() {
        when(timeEntryRepository.sumWorkDurationByUserAndPeriod(userId, startTime, endTime))
                .thenReturn(null);

        TotalWorkDurationDTO result = timeEntryService.getTotalWorkDuration(userId, startTime, endTime);
        assertEquals("00:00", result.getTotalDuration());
    }
}