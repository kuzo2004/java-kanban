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
    private Task task1;
    private Task task2;
    private Task task3;

    @BeforeEach
    public void beforeEach() {
        taskManager = new InMemoryTaskManager();
        taskManager.clearCounterForId();
    }

    // предварительные действия, но не для всех тестов
    public void addThreeTasksAndSaveToHistory() {
        task1 = new Task("Task 1", "Description 1");
        task2 = new Task("Task 2", "Description 2");
        task3 = new Task("Task 3", "Description 3");
        taskManager.addTask(task1);
        taskManager.addTask(task2);
        taskManager.addTask(task3);

        // задача попадает в историю, только если мы вызываем метод saveTaskToHistory()
        taskManager.saveTaskToHistory(task1.getId());
        taskManager.saveTaskToHistory(task2.getId());
        taskManager.saveTaskToHistory(task3.getId());
    }

    @Test
    void testAddTaskToHistoryByOrder() {
        addThreeTasksAndSaveToHistory();

        List<Task> history = taskManager.getHistory();
        assertEquals(3, history.size());
        assertEquals(task1, history.get(0));
        assertEquals(task2, history.get(1));
        assertEquals(task3, history.get(2));
    }

    @Test
    void testRemoveTaskFromHistory() {
        addThreeTasksAndSaveToHistory();

        taskManager.deleteTask(task2);

        List<Task> history = taskManager.getHistory();
        assertEquals(2, history.size());
        assertEquals(task1, history.get(0));
        assertEquals(task3, history.get(1));
    }

    @Test
    void testRemoveHeadFromHistory() {
        addThreeTasksAndSaveToHistory();

        taskManager.deleteTask(task1);

        List<Task> history = taskManager.getHistory();
        assertEquals(2, history.size());
        assertEquals(task2, history.get(0));
        assertEquals(task3, history.get(1));
    }

    @Test
    void testRemoveTailFromHistory() {
        addThreeTasksAndSaveToHistory();

        taskManager.deleteTask(task3);

        List<Task> history = taskManager.getHistory();
        assertEquals(2, history.size());
        assertEquals(task1, history.get(0));
        assertEquals(task2, history.get(1));
    }

    @Test
    void testClearHistoryWhenClearAllTasks() {
        addThreeTasksAndSaveToHistory();

        taskManager.clearAllTasks();

        assertTrue(taskManager.getHistory().isEmpty());
    }

    @Test
    void testGetEmptyHistory() {
        assertTrue(taskManager.getHistory().isEmpty());
    }

    @Test
    void testSaveTaskHistoryWithoutDuplicates() {

        // Создаем задачу
        Task createdTask = new Task("Task 1", "Description");
        int id = createdTask.getId();
        taskManager.addTask(createdTask);
        // сохраняем в историю
        taskManager.saveTaskToHistory(id);

        // Обновляем статус этой же задачи
        Task updatedtask = taskManager.updateTask(TaskType.TASK, id, "Task 1",
                "Description", Status.IN_PROGRESS);
        // сохраняем в историю обновленную задачу
        taskManager.saveTaskToHistory(updatedtask.getId());

        //Количество задач в taskManager должно остаться 1, так как задачу просто обновили
        int taskSize = taskManager.getAllTasks().size();
        assertEquals(1, taskSize, "Количество задач в taskManager должно остаться 1");

        //Количество задач в history должно остаться 1, так как в истории нет дубликатов
        int actualHistorySize = taskManager.getHistory().size();
        assertEquals(1, actualHistorySize, "Количество задач в history должно остаться 1");

        // Задача из historyList
        Task histiryTask = taskManager.getHistory().getLast();


        // Проверяем по id
        assertEquals(createdTask.getId(), updatedtask.getId(),
                "Задачи созданная и обновленная должны иметь прежний id");
        assertEquals(updatedtask.getId(), histiryTask.getId(),
                "Задачи обновленная и сохраненная в историю должны иметь одинаковый id");
        // Проверяем по статусу
        assertNotEquals(createdTask.getStatus(), histiryTask.getStatus(),
                "Задача созданная и обновленная должны отличаться по статусу");
        assertEquals(updatedtask.getStatus(), histiryTask.getStatus(),
                "Задачи обновленная и сохраненная в историю должны быть равны по статусу");
    }
}
