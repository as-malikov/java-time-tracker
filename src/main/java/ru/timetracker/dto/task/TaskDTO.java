package ru.timetracker.dto.task;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * DTO для передачи полных данных о задаче.
 * Содержит всю информацию о задаче для отображения клиенту.
 * <p>Содержит:
 * <ul>
 *   <li>Идентификатор и основные данные задачи</li>
 *   <li>Статус активности (активная/неактивная)</li>
 *   <li>Ссылку на владельца задачи</li>
 *   <li>Дату создания</li>
 * </ul>
 */
@Data
public class TaskDTO {
    /**
     * Уникальный идентификатор задачи
     * @return ID задачи
     */
    private Long id;

    /**
     * Название задачи
     * @return Название задачи
     */
    private String title;

    /**
     * Описание задачи
     * @return Описание задачи
     */
    private String description;

    /**
     * Дата и время создания задачи
     * @return Дата создания
     */
    private LocalDateTime createdAt;

    /**
     * Идентификатор владельца задачи
     * @return ID пользователя-владельца
     */
    private Long userId;

    /**
     * Статус активности задачи
     * @return true - активная, false - неактивная
     */
    private boolean active;

    /**
     * Конструктор по умолчанию, необходимый для Javadoc.
     */
    public TaskDTO() {
    }
}
