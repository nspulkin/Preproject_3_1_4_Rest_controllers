# Используем официальный образ Maven с OpenJDK 11
FROM maven:3.8.4-openjdk-11-slim

# Устанавливаем рабочую директорию
WORKDIR /app

# Копируем pom.xml
COPY pom.xml .

# Скачиваем зависимости (этот слой будет кешироваться если pom.xml не изменился)
RUN mvn dependency:go-offline -B

# Копируем исходный код
COPY src src

# Собираем приложение
RUN mvn clean package -DskipTests

# Создаем пользователя для безопасности
RUN addgroup --system spring && adduser --system spring --ingroup spring
RUN chown -R spring:spring /app
USER spring:spring

# Открываем порт
EXPOSE 8080

# Запускаем приложение
CMD ["java", "-jar", "target/spring-boot_security-demo-0.0.1-SNAPSHOT.jar"]
