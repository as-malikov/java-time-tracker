package ru.timetracker.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import ru.timetracker.model.User;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Сущность задачи пользователя. Хранит информацию о задаче, её статусе и связанных записях времени.
 * <p>Основные характеристики:
 * <ul>
 *   <li>Уникальный идентификатор</li>
 *   <li>Название и описание задачи</li>
 *   <li>Дата создания</li>
 *   <li>Статус активности (активная/неактивная)</li>
 *   <li>Связь с пользователем-владельцем</li>
 *   <li>Список связанных записей времени</li>
 * </ul>
 * <p>Связи:
 * <ul>
 *   <li>Многие-к-одному с {@link User} (каждая задача принадлежит одному пользователю)</li>
 *   <li>Один-ко-многим с {@link TimeEntry} (задача может иметь множество записей времени)</li>
 * </ul>
 * @see User Владелец задачи
 * @see TimeEntry Записи времени, связанные с задачей
 */
@Entity
@Table(name = "tasks")
@Data
@Builder
@AllArgsConstructor
public class Task {
    /**
     * Уникальный идентификатор задачи
     * @return ID задачи
     */
    @Id @GeneratedValue private Long id;

    /**
     * Название задачи (обязательное поле)
     * @return Название задачи
     */
    @Column(nullable = false) private String title;

    /**
     * Описание задачи (необязательное поле)
     * @return Описание задачи
     */
    @Column private String description;

    /**
     * Дата и время создания задачи (устанавливается автоматически)
     * @return Дата создания
     */
    @Column(nullable = false, updatable = false) private LocalDateTime createdAt;

    /**
     * Статус активности задачи (true - активная, false - неактивная)
     * @return Статус активности
     */
    @Builder.Default @Column(nullable = false) private boolean active = true;

    /**
     * Пользователь-владелец задачи
     * @return Объект пользователя
     */
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "user_id", nullable = false) @ToString.Exclude @EqualsAndHashCode.Exclude
    private User user;

    /**
     * Список записей времени, связанных с задачей
     * @return Список записей времени
     */
    @OneToMany(mappedBy = "task", cascade = CascadeType.ALL, orphanRemoval = true) @ToString.Exclude @EqualsAndHashCode.Exclude
    private List<TimeEntry> timeEntries;

    /**
     * Конструктор по умолчанию, необходимый для Javadoc.
     */
    public Task() {
    }

    /**
     * Callback-метод, устанавливающий дату создания перед сохранением
     */
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
