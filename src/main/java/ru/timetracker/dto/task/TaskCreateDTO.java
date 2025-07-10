package ru.timetracker.dto.task;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO для создания новой задачи.
 * Содержит минимально необходимые данные для создания задачи.
 *
 * <p>Валидация полей:
 * <ul>
 *   <li>title: обязательное, длина 3-100 символов</li>
 *   <li>description: необязательное, максимум 1000 символов</li>
 * </ul>
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TaskCreateDTO {
    /**
     * Название задачи (обязательное поле)
     *
     * @return Название задачи
     */
    @NotBlank @Size(min = 3, max = 100) private String title;

    /**
     * Описание задачи (необязательное поле)
     *
     * @return Описание задачи
     */
    @Size(max = 1000) private String description;
}
