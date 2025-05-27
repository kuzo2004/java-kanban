package ru.yandex.practicum.service;

import ru.yandex.practicum.entity.Epic;
import ru.yandex.practicum.entity.Status;
import ru.yandex.practicum.entity.Task;
import ru.yandex.practicum.entity.TaskType;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public interface TaskManager {

    void addTask(Task task);

    Map<Integer, Task> getAllTasks();

    Map<Integer, Task> getAllTasksByType(TaskType taskType);

    Set<Task> getPrioritizedTasks();

    void clearAllTasks();

    void clearTasksByType(TaskType taskType);

    Optional<Task> getTaskById(int id);

    void saveTaskToHistory(int id);

    Task createTask(TaskType taskType, String name, String description, Epic parentEpic,
                    LocalDateTime startTime, Duration duration);

    Task updateTask(TaskType taskType, int uniqueID, String name, String description, Status status,
                    LocalDateTime startTime, Duration duration);

    boolean deleteTask(Task task);

    Optional<Map<Integer, Task>> getSubtasksByEpic(Epic epic);

    List<Task> getHistory();

    HistoryManager getHistoryManager();

    void clearCounterForId();
}
