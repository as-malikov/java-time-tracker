# TimeTracker API

## Оглавление

1. [Общее описание](#общее-описание)
2. [Требования](#требования)
     - [Основные требования](#основные-требования)
     - [Технологии и фреймворки](#технологии-и-фреймворки)
     - [Тестирование](#тестирование)
3. [Установка и запуск](#установка-и-запуск)
    - [3.1 Клонирование репозитория](#1-клонирование-репозитория)
    - [3.2 Настройка базы данных](#2-настройка-базы-данных-)
    - [3.3 Сборка и запуск приложения](#3-сборка-и-запуск-приложения)
    - [3.4 Тестирование и покрытие кода](#4-тестирование-и-покрытие-кода)
    - [3.5 Генерация документации Javadoc](#5-генерация-документации)
    - [3.6 Настройка логирования](#6-настройка-логирования)
4. [Форматы данных](#форматы-данных)
5. [Документация API](#документация-api)
    - [5.1 Доступ к Swagger UI](#Доступ к Swagger UI)
6. [Примеры запросов](#примеры-запросов)
   - [6.1 API User (Пользователь)](#61-api-user-пользователь)
   - [6.2 API Task (Задача)](#62-api-task-задача)
   - [6.3 API TimeEntry (Запись времени)](#63-api-timeentry-запись-времени)
7. [Коды ответов](#коды-ответов)
8. [Полная таблица endpoint-ов](#полная-таблица-endpointов)

## 1. Общее описание

REST API для учета рабочего времени, управления задачами и пользователями. Не требует аутентификации.

## 2. Требования
### 2.1 Основные требования
* Java 21 (указано в <java.version>21</java.version>)
* Maven 3.6+ (рекомендуется последняя версия)
* PostgreSQL 14+ (основная СУБД) или H2 Database (для тестирования/разработки)
### 2.2 Технологии и фреймворки
* Spring Boot 3.5.3 (родительский POM)
* Spring Data JPA (для работы с БД)
* Spring Web MVC (REST API)
* Spring Validation (валидация данных)
* Lombok 1.18.38 (генерация boilerplate-кода)
* MapStruct 1.6.3 (маппинг DTO ↔ Entity)
* Hibernate Validator 8.0.1 (валидация сущностей)
* Springdoc OpenAPI 2.5.0 (документация API)
* Log4j2 2.20.0 (логирование)
### 2.3 Тестирование
* JUnit 5 (основной фреймворк тестирования)
* Mockito 5.18.0 (мокирование зависимостей)
* JaCoCo 0.8.11 (анализ покрытия кода)

## 3. Установка и запуск

### 3.1 Клонирование репозитория
```bash
git clone https://github.com/as-malikov/java-time-tracker.git
cd java-time-tracker
```

### 3.2 Настройка базы данных  
```src/main/resources/application.properties:```

**По умолчанию очистка данных производится каждый день в 1:00 для данных больше 30 дней.**

#### Для PostgreSQL
```properties
# Основные настройки
spring.datasource.url=jdbc:postgresql://localhost:5432/timetracker
spring.datasource.username=ваш_логин
spring.datasource.password=ваш_пароль
spring.jpa.hibernate.ddl-auto=update

# Дополнительные настройки
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true

# Настройка срока хранения данных (30 дней)
timetracker.data.retention.days=30
# Настройка расписания очистки (каждый день в 1:00)
app.cleanup.cron=0 0 1 * * ?
```

#### Для H2 (встроенная БД)
```properties
# Настройки H2
spring.datasource.url=jdbc:h2:mem:testdb
spring.datasource.driver-class-name=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=password

# Консоль H2 (доступна по http://localhost:8080/h2-console)
spring.h2.console.enabled=true
spring.h2.console.path=/h2-console

# Настройки JPA
spring.jpa.show-sql=true
spring.jpa.hibernate.ddl-auto=update
spring.jpa.properties.hibernate.format_sql=true
```

### 3.3 Сборка и запуск приложения
#### Сборка проекта:
```bash
mvn clean install
```
#### Запуск приложения:
```bash
java -jar time-tracker-0.0.1-SNAPSHOT.jar
```
Приложение будет доступно по адресу:  
```http://localhost:8080```

### 3.4 Тестирование и покрытие кода
#### Запуск unit-тестов:
```bash 
mvn clean test
```
#### Генерация отчета о покрытии кода (JaCoCo):
```bash
mvn jacoco:report
```
Отчет будет доступен по пути:  
```target/site/jacoco/index.html```

### 3.5 Генерация документации Javadoc
#### Создание Javadoc:
```bash
mvn javadoc:javadoc
```
Документация будет доступна по пути:  
```target/site/apidocs/index.html```
### 3.6 Настройка логирования
Добавьте в ```application.yml```:
```yaml
logging:
  file:
    name: logs/app.log
  level:
    root: INFO
    org.springframework: WARN
    ru.timetracker: DEBUG
```

## 4. Форматы данных

* Дата: **YYYY-MM-DD (2023-12-31)**
* Дата и время: **YYYY-MM-DDTHH:MM:SS (2023-12-31T23:59:59)**
* Продолжительность: **HH:MM (02:30)**

## 5. Документация API
Приложение предоставляет интерактивную документацию API с использованием Swagger UI. С её помощью вы можете:

- Изучить все доступные эндпоинты
- Тестировать API-запросы прямо из браузера
- Просматривать модели запросов и ответов
- Видеть примеры значений и коды статусов

### 5.1 Доступ к Swagger UI

Интерфейс Swagger UI доступен по адресу: http://localhost:8080/swagger-ui/index.html#/

## 6. Примеры запросов
### 6.1 API User (Пользователь)

**6.1.1 Создание нового пользователя**  
**POST**  ```/api/v1/users```

**Пример запроса:**
```bash
curl --location 'http://localhost:8080/api/v1/users' \
--header 'Content-Type: application/json' \
--data-raw '{
    "name": "Иван Иванов",
    "email": "ivan@example.com"
}'
```

**Пример ответа:**
```json
{
    "id": 1,
    "name": "Иван Иванов",
    "email": "ivan@example.com",
    "createdAt": "2025-07-14T16:38:42.565329638"
}
```

**6.1.2 Получение всех пользователей:**
**GET** ```/api/v1/users```

**Пример запроса:**
```bash
curl --location 'http://localhost:8080/api/v1/users'
```

**Пример ответа:**
```json
[
   {
      "id": 1,
      "name": "Иван Иванов",
      "email": "ivan@example.com",
      "createdAt": "2025-07-14T16:38:42.56533"
   }
]
```

**6.1.3 Получение пользователя по ID:**  
**GET** ```/api/v1/users/{id}```  

**Пример запроса:**
```bash
curl --location 'http://localhost:8080/api/v1/users/1'
```
**Пример ответа:**
```json
{
   "id": 1,
   "name": "Иван Иванов",
   "email": "ivan@example.com",
   "createdAt": "2025-07-14T16:38:42.56533"
}
```

**6.1.4 Обновление пользователя**  
**PUT**  ```/api/v1/users/{id}```

**Пример запроса:**
```bash
curl --location --request PUT 'http://localhost:8080/api/v1/users/1' \
--header 'Content-Type: application/json' \
--data-raw '{
    "name": "Петр Петров",
    "email": "petr@mail.ru"
}'
```

**Пример ответа:**
```json
{
   "id": 1,
   "name": "Петр Петров",
   "email": "petr@mail.ru",
   "createdAt": "2025-07-14T16:38:42.56533"
}
```

**6.1.5 Удаление пользователя:**  
**DELETE** ```/api/v1/users/{id}```

**Пример запроса:**
```bash
curl --location --request DELETE 'http://localhost:8080/api/v1/users/1'
```
**Пример ответа: 204 No Content**


### 6.2 API Task (Задача)

**6.2.1 Создание новой задачи**  
**POST**  ```/api/v1/users/{userId}/tasks```

**Пример запроса:**
```bash
curl --location 'http://localhost:8080/api/v1/users/1/tasks' \
--header 'Content-Type: application/json' \
--data '{
    "title": "Разработка API",
    "description": "Создание приложения учета времени"
}'
```

**Пример ответа:**
```json
{
   "id": 1,
   "title": "Разработка API",
   "description": "Создание приложения учета времени",
   "createdAt": "2025-07-14T17:05:35.670944347",
   "userId": 1,
   "active": true
}
```

**6.2.2 Получение всех задач пользователя:**
**GET** ```/api/v1/users/1/tasks```

**Пример запроса:**
```bash
curl --location 'http://localhost:8080/api/v1/users/1/tasks?includeInactive=false'
```

**Пример ответа:**
```json
[
   {
      "id": 1,
      "title": "Разработка API",
      "description": "Создание приложения учета времени",
      "createdAt": "2025-07-14T17:05:35.670944",
      "userId": 1,
      "active": true
   }
]
```

**6.2.3 Получение конкретной задачи пользователя:**  
**GET** ```/api/v1/users/{userId}/tasks/{taskId}```

**Пример запроса:**
```bash
curl --location 'http://localhost:8080/api/v1/users/1/tasks/1'
```
**Пример ответа:**
```json
{
   "id": 1,
   "title": "Разработка API",
   "description": "Создание приложения учета времени",
   "createdAt": "2025-07-14T17:05:35.670944",
   "userId": 1,
   "active": true
}
```

**6.2.4 Обновление задачи**  
**PUT**  ```/api/v1/users/{userId}/tasks/{taskId}```

**Пример запроса:**
```bash
curl --location --request PUT 'http://localhost:8080/api/v1/users/1/tasks/1' \
--header 'Content-Type: application/json' \
--data '{
    "title": "Разработка API v2.0",
    "description": "Создание приложения учета времени v2.0"
}'
```

**Пример ответа:**
```json
{
   "id": 1,
   "title": "Разработка API v2.0",
   "description": "Создание приложения учета времени v2.0",
   "createdAt": null,
   "userId": 1,
   "active": false
}
```

**6.2.5 Переключение статуса задачи**  
**PATCH**  ```/api/v1/users/{userId}/tasks/{taskId}/toggle-status```

**Пример запроса:**
```bash
curl --location --request PATCH 'http://localhost:8080/api/v1/users/1/tasks/1/toggle-status'
```

**Пример ответа:**
```json
{
   "id": 1,
   "title": "Разработка API v2.0",
   "description": "Создание приложения учета времени v2.0",
   "createdAt": "2025-07-14T17:05:35.670944",
   "userId": 1,
   "active": true
}
```

**6.2.6 Удаление задачи по ID:**  
**DELETE** ```/api/v1/users/{userId}/tasks/{taskId}```

**Пример запроса:**
```bash
curl --location --request DELETE 'http://localhost:8080/api/v1/users/1/tasks/1'
```
**Пример ответа: 204 No Content**

**6.2.7 Удаление всех задач пользователя:**  
**DELETE** ```/api/v1/users/{userId}/tasks```

**Пример запроса:**
```bash
curl --location --request DELETE 'http://localhost:8080/api/v1/users/1/tasks'
```
**Пример ответа: 204 No Content**

### 6.3 API TimeEntry (Запись времени)

**6.3.1 Начать запись времени:**  
**POST**  ```/api/v1/users/{userId}/time-entries/start```

**Пример запроса:**
```bash
curl --location 'http://localhost:8080/api/v1/users/1/time-entries/start' \
--header 'Content-Type: application/json' \
--data '{
    "taskId": 1
}'
```

**Пример ответа:**
```json
{
   "id": 1,
   "startTime": "2025-07-14T17:32:45.903323303",
   "endTime": null,
   "duration": "PT0.001516114S",
   "userId": 1,
   "taskId": 1,
   "taskTitle": "Разработка API",
   "active": true
}
```

**6.3.2 Остановить запись времени:**  
**POST**  ```/api/v1/users/{userId}/time-entries/stop```

**Пример запроса:**
```bash
curl --location 'http://localhost:8080/api/v1/users/1/time-entries/stop' \
--header 'Content-Type: application/json' \
--data '{
    "taskId": 1
}'
```

**Пример ответа:**
```json
{
   "id": 1,
   "startTime": "2025-07-14T17:32:45.903323",
   "endTime": "2025-07-14T17:39:23.21481691",
   "duration": "PT6M37.31149391S",
   "userId": 1,
   "taskId": 1,
   "taskTitle": "Разработка API",
   "active": false
}
```

**6.3.3 Получить записей времени за период:**  
**GET** ```/api/v1/users/{userId}/time-entries```

**Параметры:**
* ```from``` - начало периода (необязательный)
* ```to``` - конец периода (необязательный)

**Пример запроса:**
```bash
curl --location 'http://localhost:8080/api/v1/users/1/time-entries?from=2025-07-14T00:00:00&to=2025-07-14T23:59:59'
```
**Пример ответа:**
```json
[
   {
      "id": 1,
      "startTime": "2025-07-14T17:32:45.903323",
      "endTime": "2025-07-14T17:39:23.214817",
      "duration": "PT6M37.311494S",
      "userId": 1,
      "taskId": 1,
      "taskTitle": "Разработка API",
      "active": false
   },
   {
      "id": 2,
      "startTime": "2025-07-14T17:43:43.549708",
      "endTime": null,
      "duration": "PT52.88758035S",
      "userId": 1,
      "taskId": 1,
      "taskTitle": "Разработка API",
      "active": true
   }
]
```

**6.3.4 Получить продолжительность по задачам:**  
**GET** ```/api/v1/users/{userId}/time-entries/task-durations```

**Параметры:**
* ```from``` - начало периода (необязательный)
* ```to``` - конец периода (необязательный)

**Пример запроса:**
```bash
curl --location 'http://localhost:8080/api/v1/users/1/time-entries/task-durations?from=2025-07-14T00:00&to=2025-07-14T23:59'
```
**Пример ответа:**
```json
[
   {
      "taskId": 1,
      "taskTitle": "Разработка API",
      "duration": "00:09",
      "firstEntryTime": "2025-07-14T17:32:45.903323"
   }
]
```

**6.3.5 Получить временные интервалы:**  
**GET** ```/api/v1/users/{userId}/time-entries/time-intervals```

**Параметры:**
* ```from``` - начало периода (необязательный)
* ```to``` - конец периода (необязательный)

**Пример запроса:**
```bash
curl --location 'http://localhost:8080/api/v1/users/1/time-entries/time-intervals?from=2025-07-14T00:00&to=2025-07-14T23:59'
```
**Пример ответа:**
```json
[
   {
      "period": "17:32",
      "taskTitle": "Неактивность",
      "startTime": "2025-07-14T00:00:00",
      "endTime": "2025-07-14T17:32:45.903323",
      "workInterval": false
   },
   {
      "period": "00:06",
      "taskTitle": "Разработка API",
      "startTime": "2025-07-14T17:32:45.903323",
      "endTime": "2025-07-14T17:39:23.214817",
      "workInterval": true
   },
   {
      "period": "00:04",
      "taskTitle": "Неактивность",
      "startTime": "2025-07-14T17:39:23.214817",
      "endTime": "2025-07-14T17:43:43.549708",
      "workInterval": false
   },
   {
      "period": "00:05",
      "taskTitle": "Разработка API",
      "startTime": "2025-07-14T17:43:43.549708",
      "endTime": "2025-07-14T17:49:12.33688629",
      "workInterval": true
   },
   {
      "period": "06:09",
      "taskTitle": "Неактивность",
      "startTime": "2025-07-14T17:49:12.33688629",
      "endTime": "2025-07-14T23:59:00",
      "workInterval": false
   }
]
```

**6.3.6 Получить общее время работы:**  
**GET** ```/api/v1/users/{userId}/time-entries/total-work-duration```

**Параметры:**
* ```from``` - начало периода (необязательный)
* ```to``` - конец периода (необязательный)

**Пример запроса:**
```bash
curl --location 'http://localhost:8080/api/v1/users/1/time-entries/total-work-duration?from=2025-07-14T00:00&to=2025-07-14T23:59'
```
**Пример ответа:**
```json
{
   "totalDuration": "00:14",
   "totalSeconds": 847,
   "days": 1,
   "periodStart": "2025-07-14T00:00:00",
   "periodEnd": "2025-07-14T23:59:00"
}
```

**6.3.7 Очистить данные трекинга пользователя:**  
**DELETE** ```/api/v1/users/{userId}/time-entries/tracking-data```

**Пример запроса:**
```bash
curl --location --request DELETE 'http://localhost:8080/api/v1/users/1/time-entries/tracking-data'
```
**Пример ответа: 204 No Content**

## 7. Коды ответов

| Код | Описание        |
|-----|-----------------|
| 200 | Успешный запрос |
| 201 | Создано         |
| 204 | Нет содержимого |
| 400 | Ошибка запроса  |
| 404 | Не найдено      |
| 500 | Ошибка сервера  |

## 8.Полная таблица endpoint'ов

| Метод  | 	Endpoint                                  | 	Описание                              |
|--------|--------------------------------------------|----------------------------------------|
| GET    | 	/api/v1/users                             | 	Получить всех пользователей           |
| POST   | 	/api/v1/users                             | 	Создать пользователя                  |
| GET    | 	/api/v1/users/{id}                        | 	Получить пользователя по ID           |
| PUT    | 	/api/v1/users/{id}                        | 	Обновить пользователя                 |
| DELETE | 	/api/v1/users/{userId}                    | 	Удалить пользователя                  |
| GET    | 	/api/v1/users/{userId}/tasks              | 	Получить задачи пользователя          |
| POST   | 	/api/v1/users/{userId}/tasks              | 	Создать задачу                        |
| GET    | 	/api/v1/users/{userId}/tasks/{taskId}     | 	Получить задачу по ID                 |
| PUT    | 	/api/v1/users/{userId}/tasks/{taskId}     | 	Обновить задачу                       |
| PATCH  | 	/api/v1/users/{userId}/tasks/{taskId}/toggle-status | 	Переключить статус задачи             |
| DELETE | 	/api/v1/users/{userId}/tasks/{taskId}     | 	Удалить задачу                        |
| DELETE | 	/api/v1/users/{userId}/tasks              | 	Удалить все задачи пользователя       |
| POST   | 	/api/v1/users/{userId}/time-entries/start | 	Начать учет времени                   |
| POST   | 	/api/v1/users/{userId}/time-entries/stop  | 	Остановить учет времени               |
| GET    | 	/api/v1/users/{userId}/time-entries       | 	Получить записи времени               |
| GET    | 	/api/v1/users/{userId}/time-entries/task-durations | 	Получить продолжительность по задачам |
| GET    | 	/api/v1/users/{userId}/time-entries/time-intervals | 	Получить временные интервалы          |
| GET    | 	/api/v1/users/{userId}/time-entries/total-work-duration | 	Получить суммарное время работы       |
| DELETE | 	/api/v1/users/{userId}/time-entries/tracking-data | 	Очистить данные учета времени         |












