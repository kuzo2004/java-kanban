package ru.yandex.practicum.service.httpTaskManager;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.entity.*;
import ru.yandex.practicum.service.HttpTaskServer;
import ru.yandex.practicum.service.InMemoryTaskManager;
import ru.yandex.practicum.service.TaskManager;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Тестирование обновления задач через POST-запросы:
 * POST /tasks/{id}
 * POST /subtasks/{id}
 */
class TaskUpdateHttpTest {
    private static final String BASE_URL = "http://localhost:8080";
    private TaskManager manager;
    private HttpTaskServer taskServer;
    private Subtask existingSubtask;
    private Task existingTask;
    private Epic existingEpic; // Оставляем для создания подзадач

    @BeforeEach
    void setUp() throws IOException {
        manager = new InMemoryTaskManager();
        taskServer = new HttpTaskServer(manager, 8080);

        // Инициализация тестовых данных
        existingEpic = new Epic("Переезд", "");
        existingSubtask = new Subtask("Собрать коробки",
                "Положить вещи в коробки",
                existingEpic,
                LocalDateTime.of(2025, 5, 14, 9, 30),
                Duration.ofMinutes(60));
        existingTask = new Task("Включить чайник",
                "вскипятить воду",
                LocalDateTime.of(2025, 5, 14, 11, 0),
                Duration.ofMinutes(10));

        manager.addTask(existingEpic);
        manager.addTask(existingSubtask);
        manager.addTask(existingTask);

        taskServer.start();
    }

    @AfterEach
    void tearDown() {
        taskServer.stop();
    }

    private HttpResponse<String> sendUpdateRequest(String endpoint, TaskDto taskDto)
            throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        String json = taskServer.getGson().toJson(taskDto);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + endpoint))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    @Test
    void updateTaskWithValidDataShouldReturn201() throws Exception {
        // Arrange
        TaskDto updatedTask = new TaskDto(
                existingTask.getId(),
                "Обновленный чайник",
                "вскипятить 2 литра воды",
                "IN_PROGRESS",
                LocalDateTime.of(2025, 5, 14, 11, 30),
                Duration.ofMinutes(5),
                null);

        // Act
        HttpResponse<String> response = sendUpdateRequest(
                "/tasks/" + existingTask.getId(), updatedTask);

        // Assert
        assertEquals(201, response.statusCode());

        Task savedTask = manager.getTaskById(existingTask.getId()).orElseThrow();
        assertAll(
                () -> assertEquals("Обновленный чайник", savedTask.getName()),
                () -> assertEquals(Status.IN_PROGRESS, savedTask.getStatus()),
                () -> assertEquals(Duration.ofMinutes(5), savedTask.getDuration())
        );
    }

    @Test
    void updateSubtaskWithNewTimeShouldReturn201() throws Exception {
        // Arrange
        TaskDto updatedSubtask = new TaskDto(
                existingSubtask.getId(),
                "Упаковать коробки",
                "Аккуратно сложить вещи",
                "DONE",
                LocalDateTime.of(2025, 5, 14, 10, 0),
                Duration.ofMinutes(30),
                existingEpic.getId());

        // Act
        HttpResponse<String> response = sendUpdateRequest(
                "/subtasks/" + existingSubtask.getId(), updatedSubtask);

        // Assert
        assertEquals(201, response.statusCode());

        Subtask savedSubtask = (Subtask) manager.getTaskById(existingSubtask.getId()).orElseThrow();
        assertAll(
                () -> assertEquals("Упаковать коробки", savedSubtask.getName()),
                () -> assertEquals(Status.DONE, savedSubtask.getStatus()),
                () -> assertEquals(LocalDateTime.of(2025, 5, 14, 10, 0), savedSubtask.getStartTime()),
                () -> assertEquals(Duration.ofMinutes(30), savedSubtask.getDuration())
        );
    }

    @Test
    void updateTaskWithInvalidIdShouldReturn404() throws Exception {
        // Arrange
        TaskDto invalidTask = new TaskDto(
                999, "Несуществующая задача", "",
                null, null, null, null);

        // Act
        HttpResponse<String> response = sendUpdateRequest("/tasks/999", invalidTask);

        // Assert
        assertEquals(404, response.statusCode());
    }

    @Test
    void updateSubtaskWithWrongEpicIdShouldReturn404() throws Exception {
        // Arrange
        TaskDto invalidSubtask = new TaskDto(
                existingSubtask.getId(),
                "Подзадача",
                "С неправильным эпиком",
                null, null, null,
                999); // Несуществующий эпик

        // Act
        HttpResponse<String> response = sendUpdateRequest(
                "/subtasks/" + existingSubtask.getId(), invalidSubtask);

        // Assert
        assertEquals(404, response.statusCode());
    }

    @Test
    void updateSubtaskWithTimeConflictShouldReturn406() throws Exception {
        // Arrange - Создаем задачу с временем
        Task task1 = new Task("задача1",
                "Описание задача1",
                LocalDateTime.of(2025, 5, 30, 10, 0),
                Duration.ofMinutes(30));
        // Arrange - Создаем задачу с соседним временем
        Task task2 = new Task("задача2",
                "Описание задача2",
                LocalDateTime.of(2025, 5, 30, 11, 0),
                Duration.ofMinutes(30));

        manager.addTask(task1);
        manager.addTask(task2);

        // Пытаемся обновить подзадачу на конфликтное время
        TaskDto updatedTask1 = new TaskDto(
                task1.getId(),
                "задача1",
                "Обновление задача1",
                null,
                LocalDateTime.of(2025, 5, 30, 10, 0), // Конфликт с 10:00-11:00
                Duration.ofMinutes(100),
                null);

        // Act
        HttpResponse<String> response = sendUpdateRequest(
                "/tasks/" + task1.getId(), updatedTask1);

        // Assert
        assertEquals(406, response.statusCode());
    }
}
