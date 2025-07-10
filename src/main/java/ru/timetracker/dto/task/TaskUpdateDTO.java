package ru.timetracker.dto.task;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO для обновления данных задачи.
 * Содержит поля, которые могут быть изменены после создания задачи.
 *
 * <p>Валидация полей:
 * <ul>
 *   <li>title: обязательное, длина 3-100 символов</li>
 *   <li>description: необязательное, максимум 1000 символов</li>
 * </ul>
 *
 * <p>Дополнительные поля:
 * <ul>
 *   <li>active: статус активности задачи</li>
 *   <li>createdAt: возможность корректировки даты создания</li>
 * </ul>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaskUpdateDTO {
    /**
     * Новое название задачи
     *
     * @return Название задачи
     */
    @NotBlank @Size(min = 3, max = 100) private String title;

    /**
     * Новое описание задачи
     *
     * @return Описание задачи
     */
    @Size(max = 1000) private String description;

    /**
     * Новый статус активности
     *
     * @return true - активная, false - неактивная
     */
    private boolean active;

    /**
     * Дата создания (может быть изменена)
     *
     * @return Дата создания задачи
     */
    private LocalDateTime createdAt;
}
