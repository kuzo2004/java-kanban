package ru.yandex.practicum.manager;

import org.junit.jupiter.api.Test;
import ru.yandex.practicum.entity.Task;
import ru.yandex.practicum.service.HistoryManager;
import ru.yandex.practicum.service.InMemoryHistoryManager;
import ru.yandex.practicum.service.InMemoryTaskManager;
import ru.yandex.practicum.service.TaskManager;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class ManagersTest {

    @Test
    public void testGetDefaultTaskManager() {
        TaskManager taskManager = Managers.getDefault();

        assertNotNull(taskManager, "Менеджер задач не должен быть null");
        assertInstanceOf(InMemoryTaskManager.class, taskManager,
                "Должен возвращаться InMemoryTaskManager");

        // Проверка базовой функциональности
        Task task = new Task("Test", "Description");
        taskManager.addTask(task);

        Optional<Task> foundTask = taskManager.getTaskById(task.getId());
        assertTrue(foundTask.isPresent(), "Задача должна быть найдена");
        assertEquals(task.getName(), foundTask.get().getName(), "Имена задач должны совпадать");
    }

    @Test
    public void testGetDefaultHistoryManager()  {

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
    }
}
