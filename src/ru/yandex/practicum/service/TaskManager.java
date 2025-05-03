package ru.yandex.practicum.service;

import ru.yandex.practicum.entity.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface TaskManager {

    void addTask(Task task);

    Map<Integer, Task> getAllTasks();

    Map<Integer, Task> getAllTasksByType(TaskType taskType);

    void clearAllTasks();

    void clearTasksByType(TaskType taskType);

    Optional<Task> getTaskById(int id);

    void saveTaskToHistory(int id);

    Task createTask(TaskType taskType, String name, String description, Epic parentEpic);

    Task updateTask(TaskType taskType, int uniqueID, String name, String description, Status status);

    boolean deleteTask(Task task);

    Optional<Map<Integer, Task>> getSubtasksByEpic(Epic epic);

    List<Task> getHistory();

    HistoryManager getHistoryManager();

    void clearCounterForId();
}
