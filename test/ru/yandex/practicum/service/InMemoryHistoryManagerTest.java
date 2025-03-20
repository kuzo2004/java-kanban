package ru.yandex.practicum.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.entity.Status;
import ru.yandex.practicum.entity.Task;
import ru.yandex.practicum.entity.TaskType;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

class InMemoryHistoryManagerTest {

    private InMemoryTaskManager taskManager;

    @BeforeEach
    public void beforeEach() {
        taskManager = new InMemoryTaskManager();
        taskManager.clearCounterForId();
    }


    @Test
    void testHistoryPreservesPreviousVersionOfTask() {

        // Создаем задачу
        Task task = new Task("Task 1", "Description");
        taskManager.addTask(task);
        taskManager.getTaskById(task.getId());
        // Обновляем статус этой же задачи
        taskManager.updateTask(TaskType.TASK, 1, "Task 1", "Description", Status.IN_PROGRESS);
        taskManager.getTaskById(task.getId());

        //Количество задач в taskManager должно остаться 1, так как задачу просто обновили
        int taskSize = taskManager.getAllTasks().size();
        assertEquals(1, taskSize, "Количество задач в taskManager должно остаться 1");

        //Количество задач в history должно остаться 2, так как в историю запоминаем текущий слепок задачи
        int actualHistorySize = taskManager.getHistory().size();
        assertEquals(2, actualHistorySize, "Количество задач в history должно остаться 2");

        // Задачи в historyList
        Task firstTask = taskManager.getHistory().get(0);
        Task secondTask = taskManager.getHistory().get(1);

        // Проверяем по id
        assertEquals(firstTask.getId(), secondTask.getId(), "Задачи должны быть равны по id");
        // Проверяем по статусу
        assertNotEquals(firstTask.getStatus(), secondTask.getStatus(), "Задачи должны отличаться по статусу");
    }

    @Test
    public void testHistorySizeLimit() {

        // Добавляем 11 задач в историю
        for (int i = 1; i <= 11; i++) {
            Task task = new Task("Task " + i, "Description " + i);
            taskManager.addTask(task);
            taskManager.getTaskById(task.getId());  // неявное заполнение истории
        }

        // Получаем историю
        List<Task> history = taskManager.getHistory();

        // Проверяем, что размер истории не превышает 10
        assertEquals(10, history.size(), "История должна содержать не более 10 задач");

        // Проверяем, что первая добавленная задача удалена из истории
        assertNotEquals("Task 1", history.getFirst().getName(),
                "Первая задача должна быть удалена из истории");

        // Проверяем, что последняя добавленная задача находится в истории
        assertEquals("Task 11", history.getLast().getName(), "Последняя задача должна быть в истории");

    }
}