package ru.yandex.practicum.service.httpTaskManager;

import com.google.gson.reflect.TypeToken;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.entity.Epic;
import ru.yandex.practicum.entity.Subtask;
import ru.yandex.practicum.entity.Task;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class GetTasksHttpTest {
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

    private static class TaskMapTypeToken extends TypeToken<Map<Integer, Task>> {
    }

    private static class TaskListTypeToken extends TypeToken<List<Task>> {
    }

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

    private HttpResponse<String> sendGetRequestWithStatus(String endpoint)
            throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + endpoint))
                .GET()
                .build();
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    // Тесты для задач
    @Test
    void getAllTasksShouldReturnAllTasks() throws Exception {
        HttpResponse<String> response = sendGetRequestWithStatus("/tasks");
        assertEquals(200, response.statusCode(), "Должен вернуться статус 200");

        // Десериализуем как Map<Integer, Task>
        Map<Integer, Task> tasksMap = taskServer.getGson().fromJson(
                response.body(),
                new TaskMapTypeToken().getType()
        );
        List<Task> tasks = new ArrayList<>(tasksMap.values());

        assertEquals(2, tasks.size());
        assertTrue(tasks.stream().anyMatch(t -> t.getName().equals("Включить чайник")));
        assertTrue(tasks.stream().anyMatch(t -> t.getName().equals("Заварить чай")));
    }

    @Test
    void getTaskByIdShouldReturnCorrectTask() throws Exception {
        HttpResponse<String> response = sendGetRequestWithStatus("/tasks/" + task1.getId());
        assertEquals(200, response.statusCode(), "Должен вернуться статус 200");

        Task task = taskServer.getGson().fromJson(response.body(), Task.class);
        assertEquals(task1.getId(), task.getId());
        assertEquals("Включить чайник", task.getName());
    }

    @Test
    void getAllEpicsShouldReturnAllEpics() throws Exception {
        HttpResponse<String> response = sendGetRequestWithStatus("/epics");
        assertEquals(200, response.statusCode(), "Должен вернуться статус 200");

        Map<Integer, Task> epicsMap = taskServer.getGson().fromJson(
                response.body(),
                new TaskMapTypeToken().getType()
        );
        List<Task> epics = new ArrayList<>(epicsMap.values());

        assertEquals(2, epics.size());
        assertTrue(epics.stream().anyMatch(e -> e.getName().equals("Переезд")));
        assertTrue(epics.stream().anyMatch(e -> e.getName().equals("Сделать проект")));
    }

    @Test
    void getEpicByIdShouldReturnCorrectEpic() throws Exception {
        HttpResponse<String> response = sendGetRequestWithStatus("/epics/" + epic1.getId());
        assertEquals(200, response.statusCode(), "Должен вернуться статус 200");

        Epic epic = taskServer.getGson().fromJson(response.body(), Epic.class);
        assertEquals(epic1.getId(), epic.getId());
        assertEquals("Переезд", epic.getName());
    }

    @Test
    void getEpicSubtasksShouldReturnAllSubtasks() throws Exception {
        HttpResponse<String> response = sendGetRequestWithStatus("/epics/" + epic1.getId() + "/subtasks");
        assertEquals(200, response.statusCode(), "Должен вернуться статус 200");

        Map<Integer, Task> tasksMap = taskServer.getGson().fromJson(
                response.body(),
                new TaskMapTypeToken().getType()
        );
        List<Task> tasks = new ArrayList<>(tasksMap.values());

        assertEquals(3, tasks.size());
        assertTrue(tasks.stream().anyMatch(s -> s.getName().equals("Собрать коробки")));
        assertTrue(tasks.stream().anyMatch(s -> s.getName().equals("Упаковать кошку")));
        assertTrue(tasks.stream().anyMatch(s -> s.getName().equals("Переезд")));
    }

    @Test
    void getAllSubtasksShouldReturnAllSubtasks() throws Exception {
        HttpResponse<String> response = sendGetRequestWithStatus("/subtasks");
        assertEquals(200, response.statusCode(), "Должен вернуться статус 200");

        Map<Integer, Task> subtasksMap = taskServer.getGson().fromJson(
                response.body(),
                new TaskMapTypeToken().getType()
        );
        List<Task> subtasks = new ArrayList<>(subtasksMap.values());

        assertEquals(3, subtasks.size());
        assertTrue(subtasks.stream().anyMatch(s -> s.getName().equals("Собрать коробки")));
        assertTrue(subtasks.stream().anyMatch(s -> s.getName().equals("Упаковать кошку")));
        assertTrue(subtasks.stream().anyMatch(s -> s.getName().equals("Найти информацию")));
    }

    @Test
    void getSubtaskByIdShouldReturnCorrectSubtask() throws Exception {
        HttpResponse<String> response = sendGetRequestWithStatus("/subtasks/" + subtask1.getId());
        assertEquals(200, response.statusCode(), "Должен вернуться статус 200");

        Subtask subtask = taskServer.getGson().fromJson(response.body(), Subtask.class);
        assertEquals(subtask1.getId(), subtask.getId());
        assertEquals("Собрать коробки", subtask.getName());
        assertEquals(epic1.getId(), subtask.getParentEpic().getId());
    }

    @Test
    void getHistoryShouldReturnViewedTasks() throws Exception {
        HttpResponse<String> response = sendGetRequestWithStatus("/history");
        assertEquals(200, response.statusCode(), "Должен вернуться статус 200");

        List<Task> history = taskServer.getGson().fromJson(
                response.body(),
                new TaskListTypeToken().getType()
        );

        assertEquals(2, history.size(), "История должна содержать 2 задачи");
        Set<Integer> historyIds = history.stream()
                .map(Task::getId)
                .collect(Collectors.toSet());
        assertTrue(historyIds.contains(task1.getId()));
        assertTrue(historyIds.contains(subtask3.getId()));
    }

    @Test
    void getPrioritizedShouldReturnSortedTasks() throws Exception {
        HttpResponse<String> response = sendGetRequestWithStatus("/prioritized");
        assertEquals(200, response.statusCode(), "Должен вернуться статус 200");

        List<Task> prioritized = taskServer.getGson().fromJson(
                response.body(),
                new TaskListTypeToken().getType()
        );

        assertEquals(5, prioritized.size(), "Должно быть 5 приоритизированных задач");
        assertAll("Проверка порядка задач",
                () -> assertEquals("Собрать коробки", prioritized.get(0).getName()),
                () -> assertEquals("Упаковать кошку", prioritized.get(1).getName()),
                () -> assertEquals("Включить чайник", prioritized.get(2).getName()),
                () -> assertEquals("Заварить чай", prioritized.get(3).getName()),
                () -> assertEquals("Найти информацию", prioritized.get(4).getName())
        );
    }

    @Test
    void getNonExistentTaskShouldReturn404() throws Exception {
        HttpResponse<String> response = sendGetRequestWithStatus("/tasks/999");
        assertEquals(404, response.statusCode(), "Должен вернуться статус 404");
    }
}