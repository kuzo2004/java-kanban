package ru.yandex.practicum.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.entity.Epic;
import ru.yandex.practicum.entity.Status;
import ru.yandex.practicum.entity.Task;
import ru.yandex.practicum.entity.Subtask;
import ru.yandex.practicum.entity.TaskType;

import java.util.List;
import java.util.Map;

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
    private void addThreeTasksAndSaveToHistory() {
        task1 = new Task("Task 1", "DescTask 1");
        task2 = new Task("Task 2", "DescTask 2");
        task3 = new Task("Task 3", "DescTask 3");
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
    void testRemoveMiddleTaskFromHistory() {
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
        Task createdTask = new Task("Task 1", "Des №1");
        int id = createdTask.getId();
        taskManager.addTask(createdTask);
        // сохраняем в историю
        taskManager.saveTaskToHistory(id);

        // Обновляем статус этой же задачи
        Task updatedtask = taskManager.updateTask(TaskType.TASK, id, "Task 1",
                "Des_№1", Status.IN_PROGRESS, null, null);
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

    @Test
    void testSubtasksRemovedFromHistoryWhenEpicDeleted() {
        // Создаем эпик и подзадачи
        Epic epic = new Epic("Epic 1", "Description");
        taskManager.addTask(epic);

        Subtask subtask1 = new Subtask("Subtask 1", "Description 1", epic, null, null);
        Subtask subtask2 = new Subtask("Subtask 2", "Description 2", epic, null, null);
        taskManager.addTask(subtask1);
        taskManager.addTask(subtask2);

        // Добавляем в историю эпик и подзадачи
        taskManager.saveTaskToHistory(epic.getId());
        taskManager.saveTaskToHistory(subtask1.getId());
        taskManager.saveTaskToHistory(subtask2.getId());

        // Проверяем, что все три задачи в истории
        List<Task> historyBefore = taskManager.getHistory();
        assertEquals(3, historyBefore.size(), "В истории должны быть эпик и две подзадачи");

        // Удаляем эпик
        taskManager.deleteTask(epic);

        // Проверяем, что в истории ничего не осталось (эпик и его подзадачи удалены)
        List<Task> historyAfter = taskManager.getHistory();
        assertTrue(historyAfter.isEmpty(), "История должна быть пустой после удаления эпика");

        //Количество задач в taskManager должно остаться 0, так как эпик удаляется с подзадачами
        Map<Integer, Task> tasksAfter = taskManager.getAllTasks();
        assertTrue(tasksAfter.isEmpty(), "Количество задач в taskManager должно остаться ноль");
    }
}
