package ru.timetracker.repository;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import ru.timetracker.repository.enums.UserRole;

import java.time.LocalDateTime;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Логин не может быть пустым")
    @Size(min = 3, max = 20, message = "Логин должен быть от 3 до 20 символов")
    @Column(unique = true)
    private String login;

    @NotBlank(message = "Пароль не может быть пустым")
    @Size(min = 8, message = "Пароль должен содержать минимум 8 символов")
    @Pattern(regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z]).*$", message = "Пароль должен содержать цифры, строчные и заглавные буквы")
    private String password;

    @NotBlank(message = "Email не может быть пустым")
    @Email(message = "Некорректный формат email")
    private String email;

    @Enumerated(EnumType.STRING)
    @NotNull(message = "Роль должна быть указана")
    private UserRole role;

    @Column(name = "is_active")
    private Boolean isActive;

    @Column(name = "created_at")
    @PastOrPresent(message = "Дата создания должна быть в прошлом или настоящем")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    @PastOrPresent(message = "Дата создания должна быть в прошлом или настоящем")
    private LocalDateTime updatedAt;

    @Column(name = "first_name")
    @Size(max = 50, message = "Имя не должно превышать 50 символов")
    private String firstName;

    @Column(name = "last_name")
    @Size(max = 50, message = "Фамилия не должна превышать 50 символов")
    private String lastName;
}