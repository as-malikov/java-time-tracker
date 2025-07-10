package ru.timetracker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Spring Boot приложение TimeTracker.
 */
@SpringBootApplication
public class TimeTrackerApplication {

    /**
     * Запускает приложение.
     * @param args аргументы командной строки (не обязательные)
     */
    public static void main(String[] args) {
        SpringApplication.run(TimeTrackerApplication.class, args);
    }

    /**
     * Конструктор по умолчанию для Javadoc.
     */
    public TimeTrackerApplication() {
    }
}
