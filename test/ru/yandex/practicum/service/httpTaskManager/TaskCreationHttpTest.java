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

import static org.junit.jupiter.api.Assertions.*;

/**
 * Тестирование создания задач через POST-запросы:
 * POST /tasks
 * POST /epics
 * POST /subtasks
 */
class TaskCreationHttpTest {

    private static final String BASE_URL = "http://localhost:8080";
    private TaskManager manager;
    private HttpTaskServer taskServer;

    @BeforeEach
    void setUp() throws IOException {
        manager = new InMemoryTaskManager();
        taskServer = new HttpTaskServer(manager, 8080);
        manager.clearAllTasks();
        taskServer.start();
    }

    @AfterEach
    void tearDown() {
        taskServer.stop();
    }

    private HttpResponse<String> sendPostRequest(String endpoint, TaskDto taskDto)
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
    void createTaskShouldReturn201AndSaveTask() throws Exception {
        // Arrange
        TaskDto taskDto = new TaskDto(0, "Включить чайник", "вскипятить воду",
                "NEW",
                LocalDateTime.of(2025, 5, 14, 11, 0),
                Duration.ofMinutes(10),
                null);

        // Act
        HttpResponse<String> response = sendPostRequest("/tasks", taskDto);

        // Assert
        assertEquals(201, response.statusCode());

        Task savedTask = manager.getAllTasksByType(TaskType.TASK).values().iterator().next();
        assertAll(
                () -> assertEquals(taskDto.getName(), savedTask.getName()),
                () -> assertEquals(taskDto.getDescription(), savedTask.getDescription()),
                () -> assertEquals(Status.valueOf(taskDto.getStatus()), savedTask.getStatus()),
                () -> assertEquals(taskDto.getStartTime(), savedTask.getStartTime())
        );
    }

    @Test
    void createEpicShouldReturn201AndSaveEpic() throws Exception {
        // Arrange
        TaskDto epicDto = new TaskDto(0, "Эпик", "Описание",
                null, null, null, null);

        // Act
        HttpResponse<String> response = sendPostRequest("/epics", epicDto);

        // Assert
        assertEquals(201, response.statusCode());

        Epic savedEpic = (Epic) manager.getAllTasksByType(TaskType.EPIC).values().iterator().next();
        assertAll(
                () -> assertEquals(epicDto.getName(), savedEpic.getName()),
                () -> assertEquals(Status.NEW, savedEpic.getStatus()),
                () -> assertTrue(savedEpic.getSubtasks().isEmpty())
        );
    }

    @Test
    void createSubtaskShouldReturn201AndLinkToEpic() throws Exception {
        // Arrange - Create Epic first
        TaskDto epicDto = new TaskDto(0, "Родительский эпик", "",
                null, null, null, null);
        sendPostRequest("/epics", epicDto);
        Epic parentEpic = (Epic) manager.getAllTasksByType(TaskType.EPIC).values().iterator().next();

        // Act - Create Subtask
        TaskDto subtaskDto = new TaskDto(0, "Подзадача", "Описание",
                null, null, null, parentEpic.getId());
        HttpResponse<String> subtaskResponse = sendPostRequest("/subtasks", subtaskDto);

        // Assert
        assertEquals(201, subtaskResponse.statusCode());

        Subtask savedSubtask = (Subtask) manager.getAllTasksByType(TaskType.SUBTASK).values().iterator().next();
        assertAll(
                () -> assertEquals(parentEpic.getId(), savedSubtask.getParentEpic().getId()),
                () -> assertEquals(Status.NEW, savedSubtask.getStatus()),
                () -> assertFalse(parentEpic.getSubtasks().isEmpty())
        );
    }

    @Test
    void createSubtaskWithoutEpicShouldReturn400() throws Exception {
        // Arrange
        TaskDto invalidSubtask = new TaskDto(0, "Подзадача", "Без эпика",
                null, null, null, null);

        // Act
        HttpResponse<String> response = sendPostRequest("/subtasks", invalidSubtask);

        // Assert
        assertEquals(400, response.statusCode());
        assertTrue(manager.getAllTasksByType(TaskType.SUBTASK).isEmpty());
    }
}
