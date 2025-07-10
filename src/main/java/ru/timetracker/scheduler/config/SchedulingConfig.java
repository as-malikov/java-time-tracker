package ru.timetracker.scheduler.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Конфигурационный класс для включения планировщика задач.
 * Активирует механизм Spring Scheduling для выполнения задач по расписанию.
 *
 * <p>Основные функции:
 * <ul>
 *   <li>Включает поддержку аннотации {@code @Scheduled}</li>
 *   <li>Позволяет настраивать периодические задачи</li>
 * </ul>
 */
@Configuration
@EnableScheduling
public class SchedulingConfig {
}
