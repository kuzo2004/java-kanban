package ru.yandex.practicum.service;

import ru.yandex.practicum.entity.*;
import ru.yandex.practicum.manager.Managers;

import java.util.*;

public class InMemoryTaskManager implements TaskManager {

    protected Map<Integer, Task> tasks;
    protected HistoryManager historyManager;


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
    public Optional<Task> getTaskById(int id) {
        if (!tasks.containsKey(id)) {
            return Optional.empty();
        }
        return Optional.of(tasks.get(id));
    }

    @Override
    public void saveTaskToHistory(int id) {
        historyManager.add(tasks.get(id));
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
        historyManager.clear();
        tasks.clear();
        System.out.println("Удалено задач " + tasksSizeBefore + " шт.");
    }

    @Override
    public void clearTasksByType(TaskType taskType) {
        int tasksSizeBefore = tasks.size();

        // stream API будет проще, но пока не проходили эту тему, так написала
        Class<? extends Task> taskClass = taskType.getTaskClass();

        // подготовили список для удаления
        List<Task> tasksToDelete = new ArrayList<>();
        for (Task task : tasks.values()) {
            if (task.getClass() == taskClass) {
                tasksToDelete.add(task);
            }
        }
        // собственно удаление
        for (Task task : tasksToDelete) {
            deleteTask(task);
        }
        System.out.println("Удалено задач " + (tasksSizeBefore - tasks.size()) + " шт.");
    }


    @Override
    public Task createTask(TaskType taskType, String name,
                           String description, Epic parentEpic) {

        Task task = switch (taskType) {
            case TASK -> new Task(name, description);
            case EPIC -> new Epic(name, description);
            case SUBTASK -> new Subtask(name, description, parentEpic);
        };

        addTask(task);
        return task;
    }

    @Override
    public Task updateTask(TaskType taskType, int id, String name,
                           String description, Status status) {

        Task task = switch (taskType) {
            case TASK -> new Task(id, name, description, status);
            case EPIC -> new Epic(id, name, description,
                    ((Epic) tasks.get(id)).getSubtasks());
            case SUBTASK -> new Subtask(id, name, description, status,
                    ((Subtask) tasks.get(id)).getParentEpic());
        };

        addTask(task);
        return task;
    }

    @Override
    public boolean deleteTask(Task task) {

        if (task == null) {
            return false;
        }
        if (task instanceof Subtask subtask) {
            // Если это подзадача, сначала удаляем ссылку из эпика
            Epic epic = subtask.getParentEpic();
            epic.deleteSubtask(subtask);
        } else if (task instanceof Epic epic) {
            // Если это эпик, сначала удаляем все его подзадачи
            for (Task subtask : epic.getSubtasks().values()) {
                historyManager.remove(subtask.getId());
                tasks.remove(subtask.getId());
            }
            epic.getSubtasks().clear();
        }

        // Удаляем саму задачу
        historyManager.remove(task.getId());
        tasks.remove(task.getId());
        return true;
    }


    @Override
    public Optional<Map<Integer, Task>> getSubtasksByEpic(Epic epic) {
        if (epic.getSubtasks().isEmpty()) {
            System.out.println("Для задачи " + epic.getId() + " список пуст.");
            return Optional.empty();
        }
        return Optional.of(epic.getSubtasks());
    }

    @Override
    public void clearCounterForId() {
        Task.counter = 0;
    }
}


