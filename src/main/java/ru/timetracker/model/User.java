package ru.timetracker.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Сущность пользователя системы. Содержит основные данные пользователя и связи с его задачами и записями времени.
 * <p>Основные атрибуты:
 * <ul>
 *   <li>Уникальный идентификатор</li>
 *   <li>Имя пользователя</li>
 *   <li>Уникальный email</li>
 *   <li>Дата создания</li>
 *   <li>Список задач пользователя</li>
 *   <li>Список записей времени пользователя</li>
 * </ul>
 * <p>Связи:
 * <ul>
 *   <li>Один-ко-многим с {@link Task} (пользователь может иметь множество задач)</li>
 *   <li>Один-ко-многим с {@link TimeEntry} (пользователь может иметь множество записей времени)</li>
 * </ul>
 * @see Task Сущность задачи
 * @see TimeEntry Сущность записи времени
 */
@Builder
@Data
@AllArgsConstructor
@ToString
@Entity
@Table(name = "users")
public class User {

    /**
     * Уникальный идентификатор пользователя
     * @return ID пользователя
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Имя пользователя
     * @return Имя пользователя
     */
    @NotBlank(message = "Username is required")
    @Size(min = 2, max = 50, message = "Name must be between 2 and 50 characters")
    @Pattern(regexp = "^[a-zA-Zа-яА-Я\\s\\-]+$", message = "Name can only contain letters, spaces and hyphens")
    @Column(nullable = false)
    private String name;

    /**
     * Уникальный email пользователя (используется для входа)
     * @return Email пользователя
     */
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    @Column(nullable = false, unique = true)
    private String email;

    /**
     * Дата и время создания пользователя (устанавливается автоматически)
     * @return Дата создания
     */
    @PastOrPresent(message = "Creation date cannot be in the future")
    @Builder.Default
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    /**
     * Список задач пользователя
     * @return Список задач
     */
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @Builder.Default
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Task> tasks = new ArrayList<>();

    /**
     * Список записей времени пользователя
     * @return Список записей времени
     */
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @Builder.Default
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TimeEntry> timeEntries = new ArrayList<>();

    /**
     * Конструктор по умолчанию, необходимый для Javadoc.
     */
    public User() {
    }

    /**
     * Callback-метод, устанавливающий дату создания перед сохранением
     */
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}