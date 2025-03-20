package ru.yandex.practicum.service;

import ru.yandex.practicum.entity.*;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public interface TaskManager {

    public void addTask(Task task);

    public Map<Integer, Task> getAllTasks();

    public Map<Integer, Task> getAllTasksByType(TaskType taskType);

    public void clearAllTasks();

    public void clearTasksByType(TaskType taskType);

    public Task getTaskById(int id);

    public Task createTask(TaskType taskType, String name, String description, Epic parentEpic);

    public Task updateTask(TaskType taskType, int uniqueID, String name, String description, Status status);

    public boolean deleteTask(Task task);

    public Map<Integer, Task> getSubtasksByEpic(Epic epic);

    public List<Task> getHistory();

    public HistoryManager getHistoryManager();

    public void clearCounterForId();


}
