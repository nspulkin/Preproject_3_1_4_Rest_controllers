# Spring Boot Security Demo с PostgreSQL и Docker

Этот проект был мигрирован с MySQL на PostgreSQL и настроен для работы с Docker.

## Требования

- Docker
- Docker Compose

## Быстрый запуск

1. Клонируйте репозиторий
2. Перейдите в директорию проекта
3. Запустите приложение с помощью Docker Compose:

```bash
docker-compose up --build
```

Приложение будет доступно по адресу: http://localhost:8080

**Примечание:** Первый запуск может занять несколько минут, так как Docker будет скачивать образы и собирать приложение.

## Структура Docker

### Сервисы

- **postgres**: PostgreSQL база данных (порт 5432)
- **app**: Spring Boot приложение (порт 8080)

### Переменные окружения

- `POSTGRES_DB`: testdb
- `POSTGRES_USER`: postgres  
- `POSTGRES_PASSWORD`: postgres

## Полезные команды

### Запуск в фоновом режиме
```bash
docker-compose up -d
```

### Остановка сервисов
```bash
docker-compose down
```

### Просмотр логов
```bash
docker-compose logs -f app
docker-compose logs -f postgres
```

### Пересборка приложения
```bash
docker-compose up --build app
```

### Подключение к базе данных
```bash
docker exec -it spring_postgres psql -U postgres -d testdb
```

## Изменения в проекте

1. **pom.xml**: Заменен MySQL драйвер на PostgreSQL
2. **application.properties**: Обновлены настройки подключения к PostgreSQL
3. **Dockerfile**: Создан для сборки Spring Boot приложения
4. **docker-compose.yml**: Настроена оркестрация PostgreSQL и приложения
5. **.dockerignore**: Оптимизирована сборка Docker образа

## Безопасность

- Приложение запускается под непривилегированным пользователем
- Используется health check для PostgreSQL
- Настроена изолированная Docker сеть
