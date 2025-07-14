package ru.timetracker.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Сущность записи времени работы над задачей. Хранит информацию о временных интервалах работы пользователя над задачами.
 * <p>Основные характеристики:
 * <ul>
 *   <li>Время начала и окончания работы</li>
 *   <li>Связь с пользователем и задачей</li>
 *   <li>Автоматический расчет продолжительности</li>
 *   <li>Определение активной/неактивной записи</li>
 * </ul>
 * <p>Связи:
 * <ul>
 *   <li>Многие-к-одному с {@link User} (каждая запись принадлежит одному пользователю)</li>
 *   <li>Многие-к-одному с {@link Task} (каждая запись относится к одной задаче)</li>
 * </ul>
 * @see User Владелец записи времени
 * @see Task Задача, к которой относится запись
 */
@Entity
@Table(name = "time_entries")
@Data
@AllArgsConstructor
@Builder
public class TimeEntry {
    /**
     * Уникальный идентификатор записи времени
     * @return ID записи
     */
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;

    /**
     * Время начала работы (обязательное поле)
     * @return Время начала
     */
    @Column(nullable = false) private LocalDateTime startTime;

    /**
     * Время окончания работы (null для активных записей)
     * @return Время окончания или null
     */
    @Column private LocalDateTime endTime;

    /**
     * Пользователь, связанный с записью
     * @return Объект пользователя
     */
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "user_id", nullable = false) @ToString.Exclude @EqualsAndHashCode.Exclude
    private User user;

    /**
     * Задача, связанная с записью
     * @return Объект задачи
     */
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "task_id", nullable = false) @ToString.Exclude @EqualsAndHashCode.Exclude
    private Task task;

    /**
     * Дата и время создания записи (устанавливается автоматически)
     * @return Дата создания
     */
    @CreationTimestamp @Column(updatable = false) private LocalDateTime createdAt;

    /**
     * Конструктор по умолчанию, необходимый для Javadoc.
     */
    public TimeEntry() {
    }

    /**
     * Вычисляет продолжительность работы
     * @return Продолжительность между startTime и endTime (или текущим временем для активных записей)
     */
    public Duration getDuration() {
        LocalDateTime end = endTime != null ? endTime : LocalDateTime.now();
        return Duration.between(startTime, end);
    }

    /**
     * Проверяет, является ли запись активной (еще не остановленной)
     * @return true если запись активна (endTime == null), false в противном случае
     */
    public boolean isActive() {
        return endTime == null;
    }

    /**
     * Callback-метод, выполняющийся перед сохранением или обновлением записи. Автоматически устанавливает startTime, если он не задан, и
     * завершает записи за предыдущие дни.
     */
    @PrePersist
    @PreUpdate
    public void handleLifecycleEvents() {
        if (this.startTime == null) {
            this.startTime = LocalDateTime.now();
        }

        if (this.endTime == null && this.startTime.toLocalDate()
                .isBefore(LocalDate.now())) {
            this.endTime = this.startTime.toLocalDate()
                    .atTime(23, 59);
        }
    }
}
