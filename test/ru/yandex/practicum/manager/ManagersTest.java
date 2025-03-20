package ru.yandex.practicum.manager;

import org.junit.jupiter.api.Test;
import ru.yandex.practicum.entity.Task;
import ru.yandex.practicum.entity.TaskType;
import ru.yandex.practicum.service.HistoryManager;
import ru.yandex.practicum.service.InMemoryHistoryManager;
import ru.yandex.practicum.service.InMemoryTaskManager;
import ru.yandex.practicum.service.TaskManager;

import java.util.List;

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
        // cоздаем задачу и добавляем ее в менеджер
        Task task = taskManager.createTask(TaskType.TASK, "Task 1", "Description", null);
        // проверяем, что задача добавлена
        Task actualTask = taskManager.getTaskById(task.getId());
        assertNotNull(actualTask, "Задача должна быть найдена");
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
        final List<Task> history = historyManager.getHistory();
        assertNotNull(history, "История не пустая.");
        assertEquals(1, history.size(), "История не пустая.");
    }
}