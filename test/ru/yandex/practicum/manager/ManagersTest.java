package ru.yandex.practicum.manager;

import org.junit.jupiter.api.Test;
import ru.yandex.practicum.entity.Task;
import ru.yandex.practicum.entity.TaskType;
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

        // проверка инициализации taskManager
        TaskManager taskManager = Managers.getDefault();
        assertNotNull(taskManager, "Менеджер задач не должен быть null");
        assertInstanceOf(InMemoryTaskManager.class, taskManager,
                "Менеджер задач должен быть InMemoryTaskManager");


        // проверка готовности к работе taskManager
        // создаем задачу и добавляем ее в менеджер
        Task task = taskManager.createTask(TaskType.TASK, "Task 1", "Description", null);
        // проверяем, что задача добавлена
        Optional<Task> optionalTask = taskManager.getTaskById(task.getId());
        assertTrue(optionalTask.isPresent(), "Задача должна быть найдена");
        assertEquals(task, optionalTask.get(), "Задачи должны совпадать");
    }

    @Test
    public void testGetDefaultHistoryManager() {

        // проверка инициализации historyManager
        TaskManager taskManager = Managers.getDefault();
        HistoryManager historyManager = taskManager.getHistoryManager();
        assertNotNull(historyManager, "Менеджер истории не должен быть null");
        assertInstanceOf(InMemoryHistoryManager.class, historyManager,
                "Менеджер истории должен быть InMemoryHistoryManager");


        // проверка готовности к работе historyManager
        Task task = taskManager.createTask(TaskType.TASK, "Task 1", "Description", null);

        // поиск задачи по id - автоматически заполняет список истории просмотренных задач
        taskManager.getTaskById(task.getId());
        taskManager.getHistoryManager().add(task);
        final List<Task> history = historyManager.getHistory();
        assertFalse(history.isEmpty(), "История не пустая.");
        assertEquals(1, history.size(), "История не пустая.");
    }
}
