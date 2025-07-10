package ru.timetracker.service;

import lombok.Data;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.timetracker.dto.mapper.TaskMapper;
import ru.timetracker.dto.task.TaskCreateDTO;
import ru.timetracker.dto.task.TaskDTO;
import ru.timetracker.dto.task.TaskUpdateDTO;
import ru.timetracker.exception.ResourceNotFoundException;
import ru.timetracker.exception.TaskAlreadyExistsException;
import ru.timetracker.model.Task;
import ru.timetracker.model.User;
import ru.timetracker.repository.TaskRepository;
import ru.timetracker.repository.UserRepository;

import java.util.List;

/**
 * Сервис для работы с задачами пользователей.
 * Обеспечивает создание, получение, обновление и удаление задач,
 * а также управление их статусом (активные/неактивные).
 *
 * <p>Основные функции:
 * <ul>
 *   <li>Управление жизненным циклом задач</li>
 *   <li>Валидация данных задач</li>
 *   <li>Фильтрация задач по статусу</li>
 *   <li>Полное удаление задач пользователя</li>
 * </ul>
 *
 * @see TaskRepository Репозиторий для работы с задачами
 * @see TaskMapper Маппер для преобразования DTO/Entity
 */
@Service
@Data
public class TaskService {
    private static final Logger logger = LogManager.getLogger(TaskService.class);

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final TaskMapper taskMapper;
    private final TaskRepository timeEntryRepository;

    /**
     * Получает список задач пользователя
     *
     * @param userId          ID пользователя (обязательный)
     * @param includeInactive включать ли неактивные задачи
     * @return Список DTO задач
     */
    @Transactional(readOnly = true)
    public List<TaskDTO> getUserTasks(Long userId, boolean includeInactive) {
        logger.debug("Fetching tasks for user ID: {}, includeInactive: {}", userId, includeInactive);
        List<TaskDTO> tasks = taskRepository.findByUserId(userId, includeInactive)
                .stream()
                .map(taskMapper::toDTO)
                .toList();
        logger.info("Found {} tasks for user ID: {}", tasks.size(), userId);
        return tasks;
    }

    /**
     * Получает конкретную задачу пользователя
     *
     * @param userId ID пользователя (обязательный)
     * @param taskId ID задачи (обязательный)
     * @return DTO задачи
     * @throws ResourceNotFoundException если задача не найдена
     */
    @Transactional(readOnly = true)
    public TaskDTO getTaskById(Long userId, Long taskId) {
        logger.debug("Looking for task ID: {} for user ID: {}", taskId, userId);
        Task task = taskRepository.findByIdAndUserId(taskId, userId)
                .orElseThrow(() -> {
                    String errorMsg = "Task not found. Task ID: " + taskId + ", User ID: " + userId;
                    logger.error(errorMsg);
                    return new ResourceNotFoundException(errorMsg);
                });
        logger.info("Found task ID: {} for user ID: {}", taskId, userId);
        return taskMapper.toDTO(task);
    }

    /**
     * Создает новую задачу для пользователя
     *
     * @param userId        ID пользователя (обязательный)
     * @param taskCreateDTO DTO с данными для создания задачи
     * @return Созданная DTO задачи
     * @throws ResourceNotFoundException  если пользователь не найден
     * @throws TaskAlreadyExistsException если задача с таким названием уже существует
     */
    @Transactional
    public TaskDTO createTask(Long userId, TaskCreateDTO taskCreateDTO) {
        logger.debug("Creating new task for user ID: {}. Data: {}", userId, taskCreateDTO);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    String errorMsg = "User not found. ID: " + userId;
                    logger.error(errorMsg);
                    return new ResourceNotFoundException(errorMsg);
                });

        if (taskRepository.existsByUserIdAndTitle(userId, taskCreateDTO.getTitle())) {
            String errorMsg = "Task with this title already exists: " + taskCreateDTO.getTitle();
            logger.error(errorMsg);
            throw new TaskAlreadyExistsException(taskCreateDTO.getTitle());
        }

        Task task = taskMapper.toEntity(taskCreateDTO);
        task.setUser(user);
        task = taskRepository.save(task);

        logger.info("Created new task ID: {} for user ID: {}. Title: {}", task.getId(), userId, task.getTitle());
        return taskMapper.toDTO(task);
    }

    /**
     * Обновляет существующую задачу
     *
     * @param taskId        ID задачи (обязательный)
     * @param userId        ID пользователя (обязательный)
     * @param taskUpdateDTO DTO с обновленными данными задачи
     * @return Обновленная DTO задачи
     * @throws ResourceNotFoundException если задача не найдена
     */
    @Transactional
    public TaskDTO updateTask(Long taskId, Long userId, TaskUpdateDTO taskUpdateDTO) {
        logger.debug("Updating task ID: {} for user ID: {}. Data: {}", taskId, userId, taskUpdateDTO);

        Task task = taskRepository.findByIdAndUserId(taskId, userId)
                .orElseThrow(() -> {
                    String errorMsg = "Task not found. Task ID: " + taskId + ", User ID: " + userId;
                    logger.error(errorMsg);
                    return new ResourceNotFoundException(errorMsg);
                });

        taskMapper.updateEntity(taskUpdateDTO, task);

        if (taskUpdateDTO.getCreatedAt() != null) {
            task.setCreatedAt(taskUpdateDTO.getCreatedAt());
            logger.debug("Updated createdAt field for task ID: {}", taskId);
        }

        task = taskRepository.save(task);
        logger.info("Updated task ID: {} for user ID: {}", taskId, userId);
        return taskMapper.toDTO(task);
    }

    /**
     * Изменяет статус задачи (активная/неактивная)
     *
     * @param taskId ID задачи (обязательный)
     * @param userId ID пользователя (обязательный)
     * @return DTO задачи с новым статусом
     * @throws ResourceNotFoundException если задача не найдена
     */
    @Transactional
    public TaskDTO toggleTaskStatus(Long taskId, Long userId) {
        logger.debug("Toggling status for task ID: {} for user ID: {}", taskId, userId);

        Task task = taskRepository.findByIdAndUserId(taskId, userId)
                .orElseThrow(() -> {
                    String errorMsg = "Task not found. Task ID: " + taskId + ", User ID: " + userId;
                    logger.error(errorMsg);
                    return new ResourceNotFoundException(errorMsg);
                });

        boolean newStatus = !task.isActive();
        task.setActive(newStatus);
        task = taskRepository.save(task);

        logger.info("Status changed for task ID: {} for user ID: {}. New status: {}", taskId, userId,
                newStatus ? "active" : "inactive");
        return taskMapper.toDTO(task);
    }

    /**
     * Удаляет конкретную задачу пользователя
     *
     * @param taskId ID задачи (обязательный)
     * @param userId ID пользователя (обязательный)
     * @throws ResourceNotFoundException если задача не найдена
     */
    @Transactional
    public void deleteTask(Long taskId, Long userId) {
        logger.debug("Deleting task ID: {} for user ID: {}", taskId, userId);

        Task task = taskRepository.findByIdAndUserId(taskId, userId)
                .orElseThrow(() -> {
                    String errorMsg = "Task not found. Task ID: " + taskId + ", User ID: " + userId;
                    logger.error(errorMsg);
                    return new ResourceNotFoundException(errorMsg);
                });

        taskRepository.delete(task);
        logger.info("Deleted task ID: {} for user ID: {}", taskId, userId);
    }

    /**
     * Полностью удаляет все задачи пользователя
     *
     * @param userId ID пользователя (обязательный)
     * @throws ResourceNotFoundException если пользователь не найден
     */
    public void deleteTasksCompletely(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    String errorMessage = "User with ID " + userId + " not found";
                    return new ResourceNotFoundException(errorMessage);
                });
        timeEntryRepository.deleteByUser(user);
        taskRepository.deleteByUser(user);
    }
}