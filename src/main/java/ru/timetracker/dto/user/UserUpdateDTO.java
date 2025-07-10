package ru.timetracker.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO для обновления данных пользователя.
 * Содержит поля, которые могут быть изменены после регистрации.
 *
 * <p>Валидация полей:
 * <ul>
 *   <li>name: обязательное, длина 2-50 символов</li>
 *   <li>email: обязательное, валидный email, максимум 100 символов</li>
 * </ul>
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserUpdateDTO {
    /**
     * Новое имя пользователя
     *
     * @return Имя пользователя
     */
    @NotBlank @Size(min = 2, max = 50) private String name;

    /**
     * Новый email пользователя
     *
     * @return Email пользователя
     */
    @NotBlank @Email @Size(max = 100) private String email;
}
