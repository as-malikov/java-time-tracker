package ru.timetracker.dto.user;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * DTO для передачи данных о пользователе.
 * Содержит полную информацию о пользователе для отображения клиенту.
 * <p>Содержит:
 * <ul>
 *   <li>Идентификатор пользователя</li>
 *   <li>Основные данные (имя, email)</li>
 *   <li>Дату создания аккаунта</li>
 * </ul>
 */
@Data
public class UserDTO {
    /**
     * Уникальный идентификатор пользователя
     * @return ID пользователя
     */
    private Long id;

    /**
     * Имя пользователя
     * @return Имя пользователя
     */
    private String name;

    /**
     * Email пользователя
     * @return Email пользователя
     */
    private String email;

    /**
     * Дата и время создания пользователя
     * @return Дата создания
     */
    private LocalDateTime createdAt;

    /**
     * Конструктор по умолчанию, необходимый для Javadoc.
     */
    public UserDTO() {
    }
}
