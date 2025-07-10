package ru.timetracker.dto.timeentry;

import lombok.Data;

import java.time.Duration;
import java.util.List;

/**
 * DTO для агрегированной статистики по времени.
 * Содержит суммарные данные по всем задачам за период.
 */
public class TimeSummaryDTO {
    /**
     * Список продолжительностей по задачам
     * @return Коллекция TaskDurationDTO
     */
    private List<TaskDurationDTO> tasks;

    /**
     * Общая продолжительность работы за период
     * @return Суммарное время
     */
    private Duration totalDuration;

    /**
     * Конструктор по умолчанию, необходимый для Javadoc.
     */
    public TimeSummaryDTO() {
    }

    /**
     * Вложенный DTO для продолжительности работы по задаче
     */
    @Data
    public static class TaskDurationDTO {
        /**
         * ID задачи
         * @return Идентификатор задачи
         */
        private Long taskId;

        /**
         * Название задачи
         * @return Название задачи
         */
        private String taskTitle;

        /**
         * Продолжительность работы
         * @return Длительность работы над задачей
         */
        private Duration duration;

        /**
         * Конструктор по умолчанию, необходимый для Javadoc.
         */
        public TaskDurationDTO() {
        }
    }
}

