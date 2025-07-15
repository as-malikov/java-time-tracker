package ru.timetracker.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO для создания нового пользователя. Содержит минимально необходимые данные для регистрации пользователя в системе.
 * <p>Валидация полей:
 * <ul>
 *   <li>name: обязательное, длина 2-50 символов</li>
 *   <li>email: обязательное, валидный email, максимум 100 символов</li>
 * </ul>
 */
@Data
@AllArgsConstructor
@Builder
public class UserCreateDTO {
    /**
     * Имя пользователя
     * @return Имя пользователя
     */
    @NotBlank
    @Size(min = 2, max = 50)
    private String name;

    /**
     * Email пользователя (уникальный идентификатор)
     * @return Email пользователя
     */
    @NotBlank
    @Email
    @Size(max = 100)
    private String email;

    /**
     * Конструктор по умолчанию, необходимый для Javadoc.
     */
    public UserCreateDTO() {
    }
}
