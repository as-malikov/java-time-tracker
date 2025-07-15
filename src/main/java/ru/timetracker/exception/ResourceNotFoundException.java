package ru.timetracker.exception;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Исключение, выбрасываемое при попытке доступа к несуществующему ресурсу. Используется для сущностей, которые не найдены в системе
 * (пользователи, задачи и т.д.).
 * <p>Особенности:
 * <ul>
 *   <li>Логирует предупреждение о попытке доступа к несуществующему ресурсу</li>
 *   <li>Содержит кастомное сообщение об ошибке</li>
 * </ul>
 */
public class ResourceNotFoundException extends RuntimeException {
    private static final Logger logger = LogManager.getLogger(ResourceNotFoundException.class);

    /**
     * Создает исключение с указанием причины
     * @param message Описание ошибки (например, "User not found with id: 123")
     */
    public ResourceNotFoundException(String message) {
        super(message);
        logger.warn("Resource not found: {}", message);
    }
}