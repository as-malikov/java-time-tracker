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

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Тесты для сервиса {@link TimeEntryService}, проверяющие корректность работы с временными записями.
 * <p>Включает проверки:
 * <ul>
 *   <li>Старта и остановки временных записей</li>
 *   <li>Получения списка записей за период</li>
 *   <li>Расчета продолжительности работы по задачам</li>
 *   <li>Формирования временных интервалов</li>
 *   <li>Очистки данных трекинга</li>
 *   <li>Обработки ошибочных сценариев (несуществующие пользователи/задачи)</li>
 * </ul>
 */
@ExtendWith(MockitoExtension.class)
class TimeEntryServiceTest {

    private final Long userId = 1L;
    private final Long taskId = 1L;
    private final Long entryId = 1L;
    private final LocalDateTime now = LocalDateTime.now();
    private final LocalDateTime startTime = now.minusHours(1);
    private final LocalDateTime endTime = now;
    @Mock private TimeEntryRepository timeEntryRepository;
    @Mock private UserRepository userRepository;
    @Mock private TaskRepository taskRepository;
    @Mock private TimeEntryMapper timeEntryMapper;
    @InjectMocks private TimeEntryService timeEntryService;

    /**
     * Проверяет создание новой временной записи.
     * <p>Ожидаемое поведение:
     * <ul>
     *   <li>Создает новую запись с текущим временем начала</li>
     *   <li>Привязывает запись к пользователю и задаче</li>
     *   <li>Возвращает DTO созданной записи</li>
     * </ul>
     */
    @Test
    void startTimeEntry_ShouldCreateNewEntry() {
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

        TimeEntryDTO result = timeEntryService.startTimeEntry(userId, dto);

        assertNotNull(result);
        verify(timeEntryRepository).save(any(TimeEntry.class));
    }

    /**
     * Проверяет автоматическую остановку активной записи при создании новой.
     * <p>Ожидаемое поведение:
     * <ul>
     *   <li>Находит активную запись пользователя</li>
     *   <li>Устанавливает время окончания для активной записи</li>
     *   <li>Создает новую запись</li>
     *   <li>Возвращает DTO новой записи</li>
     * </ul>
     */
    @Test
    void startTimeEntry_ShouldStopPreviousActiveEntry() {
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

        TimeEntryDTO result = timeEntryService.startTimeEntry(userId, dto);

        assertNotNull(result);
        assertNotNull(activeEntry.getEndTime());
        verify(timeEntryRepository, times(2)).save(any(TimeEntry.class));
    }

    /**
     * Проверяет обработку случая, когда задача не принадлежит пользователю.
     * <p>Ожидаемое поведение:
     * <ul>
     *   <li>Генерирует ResourceNotFoundException</li>
     *   <li>Не создает новую запись</li>
     * </ul>
     */
    @Test
    void startTimeEntry_ShouldThrowException_WhenTaskNotBelongsToUser() {
        TimeEntryCreateDTO dto = new TimeEntryCreateDTO(taskId);
        User user = new User();
        user.setId(userId);
        Task task = new Task();
        task.setId(taskId);
        task.setUser(User.builder()
                .id(2L)
                .build()); // Другой пользователь

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));

        assertThrows(ResourceNotFoundException.class, () -> timeEntryService.startTimeEntry(userId, dto));
    }

    /**
     * Проверяет остановку активной временной записи.
     * <p>Ожидаемое поведение:
     * <ul>
     *   <li>Находит активную запись пользователя</li>
     *   <li>Устанавливает время окончания записи</li>
     *   <li>Возвращает DTO обновленной записи</li>
     * </ul>
     */
    @Test
    void stopTimeEntry_ShouldStopActiveEntry() {
        User user = new User();
        TimeEntry activeEntry = new TimeEntry();
        TimeEntryDTO entryDTO = new TimeEntryDTO();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(timeEntryRepository.findByUserAndEndTimeIsNull(user)).thenReturn(Optional.of(activeEntry));
        when(timeEntryRepository.save(activeEntry)).thenReturn(activeEntry);
        when(timeEntryMapper.toDTO(activeEntry)).thenReturn(entryDTO);

        TimeEntryDTO result = timeEntryService.stopTimeEntry(userId);

        assertNotNull(result);
        assertNotNull(activeEntry.getEndTime());
        verify(timeEntryRepository).save(activeEntry);
    }

    /**
     * Проверяет обработку случая отсутствия активной записи при остановке.
     * <p>Ожидаемое поведение:
     * <ul>
     *   <li>Генерирует IllegalStateException</li>
     *   <li>Не выполняет сохранение</li>
     * </ul>
     */
    @Test
    void stopTimeEntry_ShouldThrowException_WhenNoActiveEntry() {
        User user = new User();
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(timeEntryRepository.findByUserAndEndTimeIsNull(user)).thenReturn(Optional.empty());

        assertThrows(IllegalStateException.class, () -> timeEntryService.stopTimeEntry(userId));
    }

    /**
     * Проверяет получение временных записей пользователя за указанный период.
     * <p>Ожидаемое поведение:
     * <ul>
     *   <li>Возвращает список DTO записей</li>
     *   <li>Фильтрует записи по дате начала</li>
     *   <li>Сортирует записи по времени начала</li>
     * </ul>
     */
    @Test
    void getUserTimeEntries_ShouldReturnEntriesForPeriod() {
        User user = new User();
        TimeEntry entry = new TimeEntry();
        TimeEntryDTO entryDTO = new TimeEntryDTO();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(timeEntryRepository.findByUserAndStartTimeBetweenOrderByStartTime(user, startTime, endTime)).thenReturn(
                List.of(entry));
        when(timeEntryMapper.toDTO(entry)).thenReturn(entryDTO);

        List<TimeEntryDTO> result = timeEntryService.getUserTimeEntries(userId, startTime, endTime);

        assertEquals(1, result.size());
        assertEquals(entryDTO, result.get(0));
    }

    /**
     * Проверяет использование периода по умолчанию при отсутствии параметров.
     * <p>Ожидаемое поведение:
     * <ul>
     *   <li>Устанавливает период по умолчанию</li>
     *   <li>Вызывает репозиторий с корректными параметрами</li>
     *   <li>Не генерирует исключений</li>
     * </ul>
     */
    @Test
    void getUserTimeEntries_ShouldUseDefaultPeriod_WhenNull() {
        User user = new User();
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        timeEntryService.getUserTimeEntries(userId, null, null);

        verify(timeEntryRepository).findByUserAndStartTimeBetweenOrderByStartTime(any(User.class),
                any(LocalDateTime.class), any(LocalDateTime.class));
    }

    /**
     * Проверяет расчет продолжительности работы по задачам.
     * <p>Ожидаемое поведение:
     * <ul>
     *   <li>Группирует записи по задачам</li>
     *   <li>Суммирует продолжительность для каждой задачи</li>
     *   <li>Форматирует продолжительность в читаемый вид</li>
     *   <li>Возвращает список DTO с результатами</li>
     * </ul>
     */
    @Test
    void getUserTaskDurations_ShouldCalculateDurations() {
        Object[] dbRow = new Object[]{taskId, "Task 1", 3600L}; // 1 hour
        TaskDurationDTO durationDTO = new TaskDurationDTO(taskId, "Task 1", "01:00", startTime);

        when(timeEntryRepository.findTaskDurationsByUserAndPeriod(userId, startTime, endTime)).thenReturn(
                List.<Object[]>of(dbRow));
        when(timeEntryRepository.findFirstByUserIdAndTaskIdOrderByStartTimeAsc(userId, taskId)).thenReturn(
                Optional.of(new TimeEntry()));

        List<TaskDurationDTO> result = timeEntryService.getUserTaskDurations(userId, startTime, endTime);

        assertEquals(1, result.size());
        assertEquals("01:00", result.get(0)
                .getDuration());
    }

    /**
     * Проверяет формирование временных интервалов работы.
     * <p>Ожидаемое поведение:
     * <ul>
     *   <li>Разбивает записи на интервалы работы</li>
     *   <li>Добавляет информацию о задаче</li>
     *   <li>Возвращает список DTO интервалов</li>
     * </ul>
     */
    @Test
    void getUserTimeIntervals_ShouldCalculateIntervals() {
        User user = new User();
        TimeEntry entry = new TimeEntry();
        entry.setStartTime(startTime);
        entry.setEndTime(endTime);
        Task task = new Task();
        task.setTitle("Task 1");
        entry.setTask(task);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(timeEntryRepository.findByUserAndStartTimeBetweenOrderByStartTime(user, startTime, endTime)).thenReturn(
                List.of(entry));

        List<TimeIntervalDTO> result = timeEntryService.getUserTimeIntervals(userId, startTime, endTime);

        assertEquals(1, result.size());
        assertTrue(result.get(0)
                .isWorkInterval());
        assertEquals("Task 1", result.get(0)
                .getTaskTitle());
    }

    /**
     * Проверяет расчет общего времени работы.
     * <p>Ожидаемое поведение:
     * <ul>
     *   <li>Суммирует продолжительность всех записей</li>
     *   <li>Форматирует результат в читаемый вид</li>
     *   <li>Возвращает DTO с общей продолжительностью</li>
     * </ul>
     */
    @Test
    void getTotalWorkDuration_ShouldCalculateTotal() {
        when(timeEntryRepository.sumWorkDurationByUserAndPeriod(userId, startTime, endTime)).thenReturn(
                7200L); // 2 hours

        TotalWorkDurationDTO result = timeEntryService.getTotalWorkDuration(userId, startTime, endTime);

        assertEquals("02:00", result.getTotalDuration());
        assertEquals(7200L, result.getTotalSeconds());
    }

    /**
     * Проверяет очистку данных трекинга пользователя.
     * <p>Ожидаемое поведение:
     * <ul>
     *   <li>Удаляет все временные записи пользователя</li>
     *   <li>Проверяет наличие связанных задач</li>
     *   <li>Не генерирует исключений при успешном выполнении</li>
     * </ul>
     */
    @Test
    void clearUserTrackingData_ShouldDeleteAllUserData() {
        User user = new User();
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        timeEntryService.clearUserTrackingData(userId);

        verify(timeEntryRepository).deleteByUser(user);
        verify(taskRepository).findByUser(user);
    }

    /**
     * Проверяет обработку частично указанного периода (только начало или конец).
     * <p>Ожидаемое поведение:
     * <ul>
     *   <li>Корректно обрабатывает указание только начальной даты</li>
     *   <li>Корректно обрабатывает указание только конечной даты</li>
     *   <li>Использует значения по умолчанию для отсутствующих параметров</li>
     * </ul>
     */
    @Test
    void getUserTimeEntries_ShouldHandleSingleParameter() {
        User user = new User();
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        timeEntryService.getUserTimeEntries(userId, startTime, null);
        verify(timeEntryRepository).findByUserAndStartTimeBetweenOrderByStartTime(eq(user), eq(startTime),
                any(LocalDateTime.class));

        timeEntryService.getUserTimeEntries(userId, null, endTime);
        verify(timeEntryRepository).findByUserAndStartTimeBetweenOrderByStartTime(eq(user), any(LocalDateTime.class),
                eq(endTime));
    }

    /**
     * Проверяет обработку отсутствия данных по продолжительности задач.
     * <p>Ожидаемое поведение:
     * <ul>
     *   <li>Возвращает пустой список</li>
     *   <li>Не генерирует исключений</li>
     * </ul>
     */
    @Test
    void getUserTaskDurations_ShouldHandleEmptyResults() {
        when(timeEntryRepository.findTaskDurationsByUserAndPeriod(userId, startTime, endTime)).thenReturn(List.of());

        List<TaskDurationDTO> result = timeEntryService.getUserTaskDurations(userId, startTime, endTime);
        assertTrue(result.isEmpty());
    }

    /**
     * Проверяет обработку нулевого результата при расчете общего времени.
     * <p>Ожидаемое поведение:
     * <ul>
     *   <li>Возвращает нулевую продолжительность</li>
     *   <li>Форматирует результат как "00:00"</li>
     *   <li>Не генерирует исключений</li>
     * </ul>
     */
    @Test
    void getTotalWorkDuration_ShouldHandleNullResult() {
        when(timeEntryRepository.sumWorkDurationByUserAndPeriod(userId, startTime, endTime)).thenReturn(null);

        TotalWorkDurationDTO result = timeEntryService.getTotalWorkDuration(userId, startTime, endTime);
        assertEquals("00:00", result.getTotalDuration());
    }
}