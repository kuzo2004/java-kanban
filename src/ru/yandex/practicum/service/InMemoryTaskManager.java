package ru.yandex.practicum.service;

import ru.yandex.practicum.entity.*;
import ru.yandex.practicum.manager.Managers;

import java.util.*;

public class InMemoryTaskManager implements TaskManager {

    private Map<Integer, Task> tasks;
    private HistoryManager historyManager;


    public InMemoryTaskManager() {
        this.tasks = new HashMap<>();
        this.historyManager = Managers.getDefaultHistory();
    }

    @Override
    public HistoryManager getHistoryManager() {
        return historyManager;
    }

    @Override
    public void addTask(Task task) {
        tasks.put(task.getId(), task);
    }

    @Override
    public Map<Integer, Task> getAllTasks() {
        return tasks;
    }

    @Override
    public Task getTaskById(int id) {
        Task task = tasks.get(id);
        historyManager.add(task);
        return task;
    }

    @Override
    public List<Task> getHistory() {
        return historyManager.getHistory();
    }

    @Override
    public Map<Integer, Task> getAllTasksByType(TaskType taskType) {

        Map<Integer, Task> tasksByType = new HashMap<>();

        // stream API будет проще, но пока не проходили эту тему, так написала
        Class<? extends Task> taskClass = taskType.getTaskClass();

        for (Task task : tasks.values()) {
            if (task.getClass() == taskClass) {
                tasksByType.put(task.getId(), task);
            }
        }
        return tasksByType;
    }

    @Override
    public void clearAllTasks() {
        int tasksSizeBefore = tasks.size();
        tasks.clear();
        System.out.println("Удалено задач " + tasksSizeBefore + " шт.");
    }

    @Override
    public void clearTasksByType(TaskType taskType) {
        int tasksSizeBefore = tasks.size();

        // stream API будет проще, но пока не проходили эту тему, так написала
        Class<? extends Task> taskClass = taskType.getTaskClass();

        Iterator<Map.Entry<Integer, Task>> iterator = tasks.entrySet().iterator();

        while (iterator.hasNext()) {
            Map.Entry<Integer, Task> entry = iterator.next();
            if (entry.getValue().getClass() == taskClass &&
                    entry.getValue().checkBeforeDelete()) {
                System.out.println("Удаление " + entry.getValue());
                entry.getValue().doBeforeDelete();
                iterator.remove();
            }
        }
        System.out.println("Удалено задач " + (tasksSizeBefore - tasks.size()) + " шт.");
    }


    @Override
    public Task createTask(TaskType taskType, String name, String description, Epic parentEpic) {

        Task task = switch (taskType) {
            case TASK -> new Task(name, description);
            case EPIC -> new Epic(name, description);
            case SUBTASK -> new Subtask(name, description, parentEpic);
        };

        tasks.put(task.getId(), task);
        return task;
    }

    @Override
    public Task updateTask(TaskType taskType, int uniqueID, String name,
                           String description, Status status) {

        Task task = switch (taskType) {
            case TASK -> new Task(uniqueID, name, description, status);
            case EPIC -> new Epic(uniqueID, name, description,
                    ((Epic) tasks.get(uniqueID)).getSubtasks());
            case SUBTASK -> new Subtask(uniqueID, name, description, status,
                    ((Subtask) tasks.get(uniqueID)).getParentEpic());
        };

        tasks.put(task.getId(), task);
        return task;
    }

    @Override
    public boolean deleteTask(Task task) {
        if (!task.checkBeforeDelete()) {
            return false;
        }

        task.doBeforeDelete();
        tasks.remove(task.getId());
        return true;
    }

    @Override
    public Map<Integer, Task> getSubtasksByEpic(Epic epic) {

        if (epic.getSubtasks().isEmpty()) {
            System.out.println("Для задачи " + epic.getId() + " список пуст.");
            return null;
        }
        return epic.getSubtasks();
    }

    @Override
    public void clearCounterForId() {
        Task.counter = 0;
    }
}

