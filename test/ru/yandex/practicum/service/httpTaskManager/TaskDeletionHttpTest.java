package ru.yandex.practicum.service.httpTaskManager;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.entity.Epic;
import ru.yandex.practicum.entity.Subtask;
import ru.yandex.practicum.entity.Task;
import ru.yandex.practicum.entity.TaskType;
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
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class TaskDeletionHttpTest {
    private static final String BASE_URL = "http://localhost:8080";
    private TaskManager manager;
    private HttpTaskServer taskServer;
    private Epic epic1;
    private Subtask subtask1;
    private Subtask subtask2;
    private Task task1;
    private Task task2;
    private Epic epic2;
    private Subtask subtask3;

    @BeforeEach
    void setUp() throws IOException {
        manager = new InMemoryTaskManager();
        taskServer = new HttpTaskServer(manager, 8080);

        // Инициализация тестовых данных
        epic1 = new Epic("Переезд", "");
        subtask1 = new Subtask("Собрать коробки", "Положить вещи в коробки", epic1,
                LocalDateTime.of(2025, 5, 14, 9, 30), Duration.ofMinutes(60));
        subtask2 = new Subtask("Упаковать кошку", "Положить кошку в клетку", epic1,
                LocalDateTime.of(2025, 5, 14, 10, 30), Duration.ofMinutes(30));
        task1 = new Task("Включить чайник", "вскипятить воду",
                LocalDateTime.of(2025, 5, 14, 11, 0), Duration.ofMinutes(10));
        task2 = new Task("Заварить чай", "зеленый китайский",
                LocalDateTime.of(2025, 5, 14, 11, 10), Duration.ofMinutes(5));
        epic2 = new Epic("Сделать проект", "прогноз рынка гаджетов");
        subtask3 = new Subtask("Найти информацию", "Продажи гаджетов по годам", epic2,
                LocalDateTime.of(2025, 5, 14, 12, 0), Duration.ofMinutes(120));

        Stream.of(epic1, subtask1, subtask2, task1, task2, epic2, subtask3)
                .forEach(manager::addTask);

        // Добавляем задачи в историю
        manager.getHistoryManager().add(task1);
        manager.getHistoryManager().add(subtask3);

        taskServer.start();
    }

    @AfterEach
    void tearDown() {
        taskServer.stop();
    }

    private int sendDeleteRequest(String endpoint) throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + endpoint))
                .DELETE()
                .build();

        return client.send(request, HttpResponse.BodyHandlers.ofString()).statusCode();
    }

    @Test
    void deleteTaskShouldReturn200AndRemoveTask() throws Exception {
        // Act
        int statusCode = sendDeleteRequest("/tasks/" + task1.getId());

        // Assert
        assertEquals(200, statusCode, "Должен вернуться статус 200");
        assertFalse(manager.getTaskById(task1.getId()).isPresent(), "Задача должна быть удалена");
        assertEquals(1, manager.getAllTasksByType(TaskType.TASK).size(), "Должна остаться одна задача");
    }

    @Test
    void deleteSubtaskShouldReturn200AndRemoveFromEpic() throws Exception {
        // Act
        int statusCode = sendDeleteRequest("/subtasks/" + subtask1.getId());

        // Assert
        assertEquals(200, statusCode, "Должен вернуться статус 200");
        assertFalse(manager.getTaskById(subtask1.getId()).isPresent(), "Подзадача должна быть удалена");
        assertEquals(1, epic1.getSubtasks().size(), "У эпика должна остаться одна подзадача");
        assertFalse(epic1.getSubtasks().containsKey(subtask1.getId()), "Эпик не должен содержать удаленную подзадачу");
    }

    @Test
    void deleteEpicShouldReturn200AndRemoveWithSubtasks() throws Exception {
        // Act
        int statusCode = sendDeleteRequest("/epics/" + epic1.getId());

        // Assert
        assertEquals(200, statusCode, "Должен вернуться статус 200");
        assertFalse(manager.getTaskById(epic1.getId()).isPresent(), "Эпик должен быть удален");
        assertFalse(manager.getTaskById(subtask1.getId()).isPresent(), "Подзадача 1 должна быть удалена");
        assertFalse(manager.getTaskById(subtask2.getId()).isPresent(), "Подзадача 2 должна быть удалена");
        assertEquals(1, manager.getAllTasksByType(TaskType.EPIC).size(), "Должен остаться один эпик");
    }

    @Test
    void deleteTaskFromHistoryShouldAlsoRemoveIt() throws Exception {
        // Проверяем, что задача есть в истории перед удалением
        assertTrue(manager.getHistoryManager().getHistory().stream()
                .anyMatch(t -> t.getId() == task1.getId()));

        // Act
        int statusCode = sendDeleteRequest("/tasks/" + task1.getId());

        // Assert
        assertEquals(200, statusCode);
        assertFalse(manager.getHistoryManager().getHistory().stream()
                        .anyMatch(t -> t.getId() == task1.getId()),
                "Задача должна быть удалена из истории");
    }

    @Test
    void deleteNonExistentTaskShouldReturn404() throws Exception {
        // Act
        int statusCode = sendDeleteRequest("/tasks/999");

        // Assert
        assertEquals(404, statusCode, "Должен вернуться статус 404 для несуществующей задачи");
    }
}
