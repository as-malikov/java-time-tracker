package ru.timetracker.service;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Сервис для работы с записями времени и трекингом рабочего времени. Обеспечивает функциональность старта/останова трекинга, получения
 * статистики и аналитики по рабочему времени пользователей.
 * <p>Основные функции:
 * <ul>
 *   <li>Трекинг времени работы над задачами</li>
 *   <li>Анализ временных интервалов работы</li>
 *   <li>Расчет продолжительности работы по задачам</li>
 *   <li>Получение общей статистики рабочего времени</li>
 *   <li>Очистка данных трекинга</li>
 * </ul>
 * @see TimeEntryRepository Репозиторий для работы с записями времени
 * @see TimeEntryMapper Маппер для преобразования DTO/Entity
 */
@Service
@Data
@Builder
public class TimeEntryService {
    private static final Logger logger = LogManager.getLogger(TimeEntryService.class);

    private final TimeEntryRepository timeEntryRepository;
    private final UserRepository userRepository;
    private final TaskRepository taskRepository;
    private final TimeEntryMapper timeEntryMapper;

    /**
     * Конструктор сервиса временных записей.
     * @param timeEntryRepository репозиторий для работы с временными записями
     * @param userRepository      репозиторий пользователей
     * @param taskRepository      репозиторий задач
     * @param timeEntryMapper     маппер для преобразования DTO
     */
    public TimeEntryService(TimeEntryRepository timeEntryRepository, UserRepository userRepository, TaskRepository taskRepository,
            TimeEntryMapper timeEntryMapper) {
        this.timeEntryRepository = timeEntryRepository;
        this.userRepository = userRepository;
        this.taskRepository = taskRepository;
        this.timeEntryMapper = timeEntryMapper;
    }

    /**
     * Начинает новую запись времени для задачи пользователя
     * @param userId ID пользователя (обязательный)
     * @param dto    DTO с данными для старта трекинга (обязательный)
     * @return Созданная запись времени
     * @throws ResourceNotFoundException если пользователь или задача не найдены
     */
    @Transactional
    public TimeEntryDTO startTimeEntry(Long userId, TimeEntryCreateDTO dto) {
        logger.info("Starting time entry for user {} and task {}", userId, dto.getTaskId());
        User user = getUser(userId);
        Task task = getTask(dto.getTaskId());

        if (!task.getUser()
                .getId()
                .equals(userId)) {
            logger.error("Task {} does not belong to user {}", dto.getTaskId(), userId);
            throw new ResourceNotFoundException("Task not found");
        }

        timeEntryRepository.findByUserAndEndTimeIsNull(user)
                .ifPresent(entry -> {
                    logger.debug("Stopping previous active time entry {}", entry.getId());
                    entry.setEndTime(LocalDateTime.now());
                    timeEntryRepository.save(entry);
                });

        TimeEntry entry = new TimeEntry();
        entry.setUser(user);
        entry.setTask(task);
        entry = timeEntryRepository.save(entry);
        logger.info("Created new time entry with id {}", entry.getId());

        return timeEntryMapper.toDTO(entry);
    }

    /**
     * Останавливает активную запись времени пользователя
     * @param userId ID пользователя (обязательный)
     * @return Остановленная запись времени
     * @throws IllegalStateException если нет активной записи времени
     */
    @Transactional
    public TimeEntryDTO stopTimeEntry(Long userId) {
        logger.info("Stopping time entry for user {}", userId);
        TimeEntry entry = timeEntryRepository.findByUserAndEndTimeIsNull(getUser(userId))
                .orElseThrow(() -> {
                    logger.error("No active time entry found for user {}", userId);
                    return new IllegalStateException("No active time entry");
                });

        entry.setEndTime(LocalDateTime.now());
        entry = timeEntryRepository.save(entry);
        logger.debug("Time entry {} stopped at {}", entry.getId(), entry.getEndTime());

        return timeEntryMapper.toDTO(entry);
    }

    /**
     * Получает записи времени пользователя за период
     * @param userId ID пользователя (обязательный)
     * @param from   Начало периода (необязательный)
     * @param to     Конец периода (необязательный)
     * @return Список записей времени
     */
    @Transactional(readOnly = true)
    public List<TimeEntryDTO> getUserTimeEntries(Long userId, LocalDateTime from, LocalDateTime to) {
        logger.info("Getting time entries for user {} from {} to {}", userId, from, to);

        if (from == null && to == null) {
            LocalDateTime now = LocalDateTime.now();
            from = now.toLocalDate()
                    .atStartOfDay();
            to = now;
            logger.debug("Setting default period: from {} to {}", from, to);
        } else if (from != null && to == null) {
            to = LocalDateTime.now();
            logger.debug("Setting to = current time: {}", to);
        } else if (to != null && from == null) {
            from = to.toLocalDate()
                    .atStartOfDay();
            logger.debug("Setting from = start of day: {}", from);
        }

        List<TimeEntryDTO> result = timeEntryRepository.findByUserAndStartTimeBetweenOrderByStartTime(getUser(userId), from, to)
                .stream()
                .map(timeEntryMapper::toDTO)
                .toList();

        logger.debug("Found {} time entries for user {}", result.size(), userId);
        return result;
    }

    /**
     * Получает суммарное время работы по задачам за период
     * @param userId ID пользователя (обязательный)
     * @param from   Начало периода (необязательный)
     * @param to     Конец периода (необязательный)
     * @return Список продолжительностей по задачам
     * @throws IllegalArgumentException если некорректный период
     */
    public List<TaskDurationDTO> getUserTaskDurations(Long userId, LocalDateTime from, LocalDateTime to) {
        logger.info("Getting task durations for user {} from {} to {}", userId, from, to);

        if (from != null && to != null && from.isAfter(to)) {
            logger.error("Invalid period: from {} is after to {}", from, to);
            throw new IllegalArgumentException("End date must be after start date");
        }

        if (from == null && to == null) {
            LocalDateTime now = LocalDateTime.now();
            from = now.toLocalDate()
                    .atStartOfDay();
            to = now;
            logger.debug("Setting default period: from {} to {}", from, to);
        } else if (from != null && to == null) {
            to = LocalDateTime.now();
            logger.debug("Setting to = current time: {}", to);
        } else if (to != null && from == null) {
            from = to.toLocalDate()
                    .atStartOfDay();
            logger.debug("Setting from = start of day: {}", from);
        }

        try {
            List<Object[]> results = timeEntryRepository.findTaskDurationsByUserAndPeriod(userId, from, to);

            logger.debug("Found {} task duration records", results.size());

            return results.stream()
                    .map(taskDurationData -> {
                        try {
                            Long taskId = ((Number) taskDurationData[0]).longValue();
                            String title = (String) taskDurationData[1];
                            long totalSeconds = ((Number) taskDurationData[2]).longValue();

                            LocalDateTime firstEntry = timeEntryRepository.findFirstByUserIdAndTaskIdOrderByStartTimeAsc(userId, taskId)
                                    .map(TimeEntry::getStartTime)
                                    .orElse(null);

                            return new TaskDurationDTO(taskId, title, formatDuration(totalSeconds), firstEntry);
                        } catch (Exception e) {
                            logger.error("Error processing time entry data", e);
                            throw new IllegalArgumentException("Error processing time entry data", e);
                        }
                    })
                    .sorted(Comparator.comparing(TaskDurationDTO::getFirstEntryTime, Comparator.nullsLast(Comparator.naturalOrder())))
                    .toList();
        } catch (Exception e) {
            logger.error("Failed to get user task durations", e);
            throw new IllegalArgumentException("Failed to get user task durations", e);
        }
    }

    /**
     * Получает временные интервалы работы/неактивности за период
     * @param userId ID пользователя (обязательный)
     * @param from   Начало периода (необязательный)
     * @param to     Конец периода (необязательный)
     * @return Список интервалов
     * @throws IllegalArgumentException если некорректный период
     */
    public List<TimeIntervalDTO> getUserTimeIntervals(Long userId, LocalDateTime from, LocalDateTime to) {
        logger.info("Getting time intervals for user {} from {} to {}", userId, from, to);

        if (from == null && to == null) {
            to = LocalDateTime.now();
            from = to.minusDays(7);
            logger.debug("Setting default period (last 7 days): from {} to {}", from, to);
        }

        if (from.isAfter(to)) {
            logger.error("Invalid period: from {} is after to {}", from, to);
            throw new IllegalArgumentException("Start date must be before end date");
        }

        List<TimeEntry> entries = timeEntryRepository.findByUserAndStartTimeBetweenOrderByStartTime(getUser(userId), from, to);

        logger.debug("Found {} time entries for interval calculation", entries.size());

        List<TimeIntervalDTO> result = new ArrayList<>();
        LocalDateTime previousEnd = from;

        for (TimeEntry entry : entries) {
            LocalDateTime entryStart = entry.getStartTime();
            LocalDateTime entryEnd = entry.getEndTime() != null ? entry.getEndTime() : LocalDateTime.now();

            if (previousEnd.isBefore(entryStart)) {
                addInactiveInterval(result, previousEnd, entryStart);
            }

            addActiveInterval(result, entry, entryStart, entryEnd);

            previousEnd = entryEnd.isAfter(previousEnd) ? entryEnd : previousEnd;
        }

        if (previousEnd.isBefore(to)) {
            addInactiveInterval(result, previousEnd, to);
        }

        logger.debug("Calculated {} time intervals", result.size());
        return result;
    }

    /**
     * Получает общее время работы за период
     * @param userId ID пользователя (обязательный)
     * @param from   Начало периода (необязательный)
     * @param to     Конец периода (необязательный)
     * @return Общая продолжительность работы
     */
    public TotalWorkDurationDTO getTotalWorkDuration(Long userId, LocalDateTime from, LocalDateTime to) {
        logger.info("Getting total work duration for user {} from {} to {}", userId, from, to);

        if (from == null && to == null) {
            to = LocalDateTime.now();
            from = to.minusDays(7);
            logger.debug("Setting default period (last 7 days): from {} to {}", from, to);
        } else if (from == null) {
            from = to.minusDays(7);
            logger.debug("Setting from = to - 7 days: {}", from);
        } else if (to == null) {
            to = from.plusDays(7);
            logger.debug("Setting to = from + 7 days: {}", to);
        }

        Long totalSeconds = timeEntryRepository.sumWorkDurationByUserAndPeriod(userId, from, to);
        if (totalSeconds == null) {
            totalSeconds = 0L;
            logger.debug("No work duration found, setting to 0");
        } else {
            logger.debug("Total work duration in seconds: {}", totalSeconds);
        }

        long daysInPeriod = ChronoUnit.DAYS.between(from.toLocalDate(), to.toLocalDate()) + 1;

        logger.debug("Days in period: {}", daysInPeriod);

        return new TotalWorkDurationDTO(formatDuration(totalSeconds), totalSeconds, (int) daysInPeriod, from, to);
    }

    /**
     * Полностью очищает данные трекинга пользователя
     * @param userId ID пользователя (обязательный)
     * @throws ResourceNotFoundException если пользователь не найден
     */
    @Transactional
    public void clearUserTrackingData(Long userId) {
        logger.info("Clearing tracking data for user {}", userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    logger.error("User not found with id {}", userId);
                    return new ResourceNotFoundException("User not found with id: " + userId);
                });

        timeEntryRepository.deleteByUser(user);
        logger.info("Deleted time entries for user {}", userId);

        List<Task> userTasks = taskRepository.findByUser(user);
        logger.debug("Clearing time entries for {} tasks of user {}", userTasks.size(), userId);

        userTasks.forEach(task -> {
            task.getTimeEntries()
                    .clear();
            taskRepository.save(task);
        });
    }

    private User getUser(Long userId) {
        logger.debug("Getting user with id {}", userId);
        return userRepository.findById(userId)
                .orElseThrow(() -> {
                    logger.error("User not found with id {}", userId);
                    return new ResourceNotFoundException("User not found");
                });
    }

    private Task getTask(Long taskId) {
        logger.debug("Getting task with id {}", taskId);
        return taskRepository.findById(taskId)
                .orElseThrow(() -> {
                    logger.error("Task not found with id {}", taskId);
                    return new ResourceNotFoundException("Task not found");
                });
    }

    private void addActiveInterval(List<TimeIntervalDTO> result, TimeEntry entry, LocalDateTime start, LocalDateTime end) {
        Duration duration = Duration.between(start, end);
        result.add(new TimeIntervalDTO(formatDurationUserTimeIntervals(duration), entry.getTask()
                .getTitle(), true, start, end));
    }

    private void addInactiveInterval(List<TimeIntervalDTO> result, LocalDateTime start, LocalDateTime end) {
        Duration duration = Duration.between(start, end);
        result.add(new TimeIntervalDTO(formatDurationUserTimeIntervals(duration), "Неактивность", false, start, end));
    }

    private String formatDurationUserTimeIntervals(Duration duration) {
        long hours = duration.toHours();
        long minutes = duration.minusHours(hours)
                .toMinutes();
        return String.format("%02d:%02d", hours, minutes);
    }

    private String formatDuration(long totalSeconds) {
        long hours = totalSeconds / 3600;
        long minutes = (totalSeconds % 3600) / 60;
        return String.format("%02d:%02d", hours, minutes);
    }
}