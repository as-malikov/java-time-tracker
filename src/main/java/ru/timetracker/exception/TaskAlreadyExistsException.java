package ru.timetracker.exception;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Исключение, выбрасываемое при попытке создания задачи с уже существующим названием.
 *
 * <p>Особенности:
 * <ul>
 *   <li>Логирует предупреждение о дубликате задачи</li>
 *   <li>В debug-режиме сохраняет stack trace</li>
 *   <li>Содержит понятное сообщение об ошибке с указанием проблемного названия</li>
 * </ul>
 */
public class TaskAlreadyExistsException extends RuntimeException {
    private static final Logger logger = LogManager.getLogger(TaskAlreadyExistsException.class);

    /**
     * Создает исключение с указанием дублирующегося названия задачи
     *
     * @param title Название задачи, которое уже существует
     */
    public TaskAlreadyExistsException(String title) {
        super("Task with title '" + title + "' already exists");
        logError(title);
    }

    /**
     * Логирует информацию о попытке создания дубликата задачи
     *
     * @param identifier Название или идентификатор задачи
     */
    private void logError(String identifier) {
        logger.warn("Attempt to create duplicate task: {}", identifier);

        if (logger.isDebugEnabled()) {
            logger.debug("Duplicate task creation attempt details:", this);
        }
    }
}