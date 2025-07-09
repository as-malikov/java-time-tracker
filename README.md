# TimeTracker API

## Оглавление
1. [Общее описание](#общее-описание)
2. [Требования](#требования)
3. [Установка и запуск](#установка-и-запуск)
4. [Форматы данных](#форматы-данных)
5. [Документация API](#документация-api)
    - [Пользователи](#пользователи)
    - [Задачи](#задачи)
    - [Учет времени](#учет-времени)
6. [Примеры запросов](#примеры-запросов)
7. [Коды ответов](#коды-ответов)
8. [Полная таблица endpoint-ов](#полная-таблица-endpointов)


## Общее описание
REST API для учета рабочего времени, управления задачами и пользователями. Не требует аутентификации.

## Требования
- Java 17+
- Maven 3.6+
- PostgreSQL 12+

## Установка и запуск
1. Клонировать репозиторий
2. Настроить подключение к БД в `application.properties`:
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/timetracker
spring.datasource.username=ваш_логин
spring.datasource.password=ваш_пароль
```

## Форматы данных
* Дата: **YYYY-MM-DD (2023-12-31)**
* Дата и время: **YYYY-MM-DDTHH:MM:SS (2023-12-31T23:59:59)**
* Продолжительность: **HH:MM (02:30)**

## Документация API

### Пользователи (User)

**Создать пользователя**

```bash
POST /api/v1/users
```
Тело запроса:
```json
{
"name": "Иван Иванов",
"email": "ivan@example.com"
}
```
Ответ:

```json
{
  "id": 1,
  "name": "Иван Иванов",
  "email": "ivan@example.com",
  "createdAt": "2023-01-15T14:30:00"
}
```
**Получить всех пользователей**
```bash
GET /api/v1/users
```
Ответ:
```json
[
  {
    "id": 1,
    "name": "Иван Иванов",
    "email": "ivan@example.com",
    "createdAt": "2023-01-15T14:30:00"
  }
]
```
**Изменить пользователя**
```bash
PUT /api/v1/users/{userId}
```
Тело запроса:
```json
{
    "name": "Иван Иванов",
    "email": "ivan@example.com"
}
```
Ответ:
```json
{
    "id": 1,
    "name": "Иван Иванов",
    "email": "ivan@example.com",
    "createdAt": "2023-01-15T14:30:00"
}
```

**Удалить пользователя и всю информацию о пользователе**
```bash
DELETE /api/v1/users/{userId}
```
Ответ: **204 No Content**

### Задачи (Task)

**Создать задачу**
```bash
POST /api/v1/users/{userId}/tasks
```
**Тело запроса:**
```json
{
  "title": "Разработка API",
  "description": "Создание методов для учета времени"
}
```
**Ответ:**
```json
{
  "id": 1,
  "title": "Разработка API",
  "description": "Создание методов для учета времени",
  "createdAt": "2023-01-15T14:30:00",
  "userId": 1,
  "active": true
}
```
**Получить задачи пользователя**
```bash
GET /api/v1/users/{userId}/tasks?includeInactive=false
```
**Ответ:**
```json
[
  {
    "id": 1,
    "title": "Разработка API",
    "description": "Создание методов для учета времени",
    "createdAt": "2023-01-15T14:30:00",
    "userId": 1,
    "active": true
  }
]
```
### Учет времени (TimeEntry)
**Начать учет времени**
```bash
POST /api/v1/users/{userId}/time-entries/start
```
**Тело запроса:**
```json
{
  "taskId": 1
}
```
**Ответ:**
```json
{
  "id": 1,
  "startTime": "2023-01-15T14:30:00",
  "userId": 1,
  "taskId": 1,
  "taskTitle": "Разработка API",
  "active": true
}
```
**Остановить учет времени**
```bash
POST /api/v1/users/{userId}/time-entries/{timeEntryId}/stop
```
**Ответ:**
```json
{
  "id": 1,
  "startTime": "2023-01-15T14:30:00",
  "endTime": "2023-01-15T15:30:00",
  "duration": "01:00",
  "userId": 1,
  "taskId": 1,
  "taskTitle": "Разработка API",
  "active": false
}
```
**Показать все трудозатраты пользователя за период (Задача-Сумма)**
```bash
GET /api/v1/users/{userId}/time-entries/task-durations?from=2025-07-09T00:00&to=2025-07-09T23:59
```
Ответ:
```json
[
    {
        "taskId": 1,
        "taskTitle": "Разработка API",
        "duration": "28:01",
        "firstEntryTime": "2023-01-15T14:30:00"
    }
]
```
**Показать все временные интервалы занятые работой за период (Временной интервал - Задача)**
```bash
GET /api/v1/users/{userId}/time-entries/time-intervals?from=2025-07-09T00:00&to=2025-07-09T23:59
```
Ответ:
```json
[
    {
        "period": "13:36",
        "taskTitle": "Неактивность",
        "startTime": "2025-07-09T00:00:00",
        "endTime": "2025-07-09T13:36:05.996152",
        "workInterval": false
    },
    {
        "period": "00:06",
        "taskTitle": "Разработка API",
        "startTime": "2025-07-09T13:36:05.996152",
        "endTime": "2025-07-09T13:42:29.612918449",
        "workInterval": true
    },
    {
        "period": "10:16",
        "taskTitle": "Неактивность",
        "startTime": "2025-07-09T13:42:29.612918449",
        "endTime": "2025-07-09T23:59:00",
        "workInterval": false
    }
]
```

**Показать сумму трудозатрат по всем задачам пользователя за период**
```bash
GET /api/v1/users/{userId}/time-entries/total-work-duration?from=2025-07-09T00:00&to=2025-07-09T23:59
```
**Ответ:**
```json
{
   "totalDuration": "01:56",
   "totalSeconds": 6989,
   "days": 1,
   "periodStart": "2025-07-09T00:00:00",
   "periodEnd": "2025-07-09T23:59:00"
}
```
**Очистить данные трекинга пользователя**
```bash
DELETE /api/v1/users/{userId}/time-entries/tracking-data
```
Ответ: **204 No Content**

## Примеры запросов
**1. Получить временные интервалы:**
```bash
curl -X GET "http://localhost:8080/api/v1/users/1/time-entries/time-intervals?from=2023-01-01&to=2023-01-31"
```
**2. Получить продолжительность по задачам:**
```bash
curl -X GET "http://localhost:8080/api/v1/users/1/time-entries/task-durations"
```
**3. Переключить статус задачи:**
```bash
curl -X PATCH http://localhost:8080/api/v1/users/1/tasks/1/toggle-status
```

## Коды ответов

|Код|Описание|
|-|---------|
|200|Успешный запрос |
|201|Создано|
|204|Нет содержимого|
|400|Ошибка запроса|
|404|Не найдено|
|500|Ошибка сервера|

## Полная таблица endpoint'ов

|Метод|	Endpoint|	Описание|
|-|-|-|
|GET|	/api/v1/users|	Получить всех пользователей|
|POST|	/api/v1/users|	Создать пользователя|
|GET|	/api/v1/users/{id}|	Получить пользователя по ID|
|PUT|	/api/v1/users/{id}|	Обновить пользователя|
|DELETE|	/api/v1/users/{userId}|	Удалить пользователя|
|GET|	/api/v1/users/{userId}/tasks|	Получить задачи пользователя|
|POST|	/api/v1/users/{userId}/tasks|	Создать задачу|
|GET|	/api/v1/users/{userId}/tasks/{taskId}|	Получить задачу по ID|
|PUT|	/api/v1/users/{userId}/tasks/{taskId}|	Обновить задачу|
|PATCH|	/api/v1/users/{userId}/tasks/{taskId}/toggle-status|	Переключить статус задачи|
|DELETE|	/api/v1/users/{userId}/tasks/{taskId}|	Удалить задачу|
|POST|	/api/v1/users/{userId}/time-entries/start|	Начать учет времени|
|POST|	/api/v1/users/{userId}/time-entries/{timeEntryId}/stop|	Остановить учет времени|
|GET|	/api/v1/users/{userId}/time-entries|	Получить записи времени|
|GET|	/api/v1/users/{userId}/time-entries/task-durations|	Получить продолжительность по задачам|
|GET|	/api/v1/users/{userId}/time-entries/time-intervals|	Получить временные интервалы|
|GET|	/api/v1/users/{userId}/time-entries/total-work-duration|	Получить суммарное время работы|
|DELETE|	/api/v1/users/{userId}/time-entries/tracking-data|	Очистить данные учета времени|












