package ru.yandex.practicum.manager;

import org.junit.jupiter.api.Test;
import ru.yandex.practicum.entity.Task;
import ru.yandex.practicum.service.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class ManagersTest {

    @Test
    public void testGetDefaultTaskManager() throws IOException {
        // Подменяем стандартный файл на временный
        Path testFile = Files.createTempFile("test_tasks", ".csv");

        Managers.setDefaultTasksFile(testFile.toFile());

        TaskManager taskManager = Managers.getDefault();

        assertNotNull(taskManager, "Менеджер задач не должен быть null");
        assertInstanceOf(FileBackedTaskManager.class, taskManager,
                "Должен возвращаться FileBackedTaskManager");

        // Проверка базовой функциональности
        Task task = new Task("Test", "Description");
        taskManager.addTask(task);

        Optional<Task> foundTask = taskManager.getTaskById(task.getId());
        assertTrue(foundTask.isPresent(), "Задача должна быть найдена");
        assertEquals(task.getName(), foundTask.get().getName(), "Имена задач должны совпадать");

        Files.deleteIfExists(testFile);
    }

    @Test
    public void testGetDefaultHistoryManager() throws IOException {
        Path testFile = Files.createTempFile("test_tasks", ".csv");
        Managers.setDefaultTasksFile(testFile.toFile());
        TaskManager taskManager = Managers.getDefault();
        HistoryManager historyManager = taskManager.getHistoryManager();


        assertNotNull(historyManager, "Менеджер истории не должен быть null");
        assertInstanceOf(InMemoryHistoryManager.class, historyManager,
                "Должен возвращаться InMemoryHistoryManager");

        // Проверка базовой функциональности
        Task task = new Task("Test", "Description");
        taskManager.addTask(task);
        taskManager.saveTaskToHistory(task.getId());
        List<Task> history = historyManager.getHistory();


        assertEquals(1, history.size(), "История должна содержать 1 задачу");
        assertEquals(task.getName(), history.get(0).getName(), "Имена задач должны совпадать");

        Files.deleteIfExists(testFile);
    }
}
