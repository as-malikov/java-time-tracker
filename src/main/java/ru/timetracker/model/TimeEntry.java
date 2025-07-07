package ru.timetracker.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "time_entries")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Slf4j
public class TimeEntry {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDateTime startTime;

    @Column
    private LocalDateTime endTime;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Task task;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    // Автоматическое вычисление длительности
    public Duration getDuration() {
        LocalDateTime end = endTime != null ? endTime : LocalDateTime.now();
        return Duration.between(startTime, end);
    }

    // Проверка, активна ли запись (ещё не остановлена)
    public boolean isActive() {
        return endTime == null;
    }

    @PrePersist
    @PreUpdate
    public void handleLifecycleEvents() {
        // Логика создания (установка startTime)
        if (this.startTime == null) {
            this.startTime = LocalDateTime.now();
            log.debug("Set start time for new time entry: {}", this.startTime);
        }

        // Логика авто-завершения
        if (this.endTime == null && this.startTime.toLocalDate().isBefore(LocalDate.now())) {
            this.endTime = this.startTime.toLocalDate().atTime(23, 59);
            log.debug("Auto-completed time entry {} at {}",
                    this.getId() != null ? this.getId() : "new",
                    this.endTime);
        }
    }
}
