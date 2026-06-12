# === Этап 1: Сборка приложения ===
FROM maven:3.9.6-eclipse-temurin-17-alpine AS builder
WORKDIR /build

# 1. Кэшируем pom.xml и зависимости, чтобы ускорить повторные сборки Docker
COPY pom.xml .
RUN mvn dependency:go-offline -B -Pprod

# 2. Копируем исходный код и собираем JAR под профилем prod (тесты пропускаем для скорости)
COPY src ./src
RUN mvn clean package -DskipTests -Pprod

# === Этап 2: Финальный легковесный образ ===
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Создаем группу и пользователя spring
RUN addgroup -S spring && adduser -S spring -G spring

# СРАЗУ СОЗДАЕМ ПАПКУ ДЛЯ ЛОГОВ И ДАЕМ НА НЕЕ ПРАВА ПОЛЬЗОВАТЕЛЮ SPRING
RUN mkdir -p /app/logs && chown -R spring:spring /app

# Переключаемся на безопасного пользователя
USER spring:spring

# Копируем собранный jar-файл
COPY --from=builder /build/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", \
            "-XX:+UseG1GC", \
            "-Dspring.profiles.active=prod", \
            "-jar", \
            "app.jar"]