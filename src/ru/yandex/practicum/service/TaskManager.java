package ru.yandex.practicum.service;

import ru.yandex.practicum.entity.*;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class TaskManager {
    private Map<Integer, Task> tasks;


    public TaskManager() {
        this.tasks = new HashMap<>();
    }


    public void addTask(Task task) {
        tasks.put(task.getId(), task);
    }


    public Map<Integer, Task> getAllTasks() {
        return tasks;
    }

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


    public void clearAllTasks() {
        int tasksSizeBefore = tasks.size();
        tasks.clear();
        System.out.println("Удалено задач " + tasksSizeBefore + " шт.");
    }

    public void clearTasksByType(TaskType taskType) {
        int tasksSizeBefore = tasks.size();

        // stream API будет проще, но пока не проходили эту тему, так написала
        Class<? extends Task> taskClass = taskType.getTaskClass();

        Iterator<Map.Entry<Integer, Task>> iterator = tasks.entrySet().iterator();

        while (iterator.hasNext()) {
            Map.Entry<Integer, Task> entry = iterator.next();
            if (entry.getValue().getClass() == taskClass &&
                    entry.getValue().doBeforeDelete()) {
                System.out.println("Удаление " + entry.getValue());
                iterator.remove();
            }
        }
        System.out.println("Удалено задач " + (tasksSizeBefore - tasks.size()) + " шт.");
    }


    public Task getTaskById(int id) {
        return tasks.get(id);
    }


    public Task createTask(TaskType taskType, String name, String description, Epic parentEpic) {

        Task task = switch (taskType) {
            case TASK -> new Task(name, description);
            case EPIC -> new Epic(name, description);
            case SUBTASK -> new Subtask(name, description, parentEpic);
        };

        tasks.put(task.getId(), task);
        return task;
    }

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

    public boolean deleteTask(Task task) {

        if (task.doBeforeDelete()) {
            tasks.remove(task.getId());
            return true;
        }
        return false;
    }

    public Map<Integer, Task> getSubtasksByEpic(Epic epic) {

        if (epic.getSubtasks().isEmpty()) {
            System.out.println("Для задачи " + epic.getId() + " список пуст.");
            return null;
        }
        return epic.getSubtasks();
    }


}
