package ru.yandex.practicum.service;

import ru.yandex.practicum.entity.*;
import ru.yandex.practicum.exceptions.TimeConflictException;
import ru.yandex.practicum.manager.Managers;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class InMemoryTaskManager implements TaskManager {

    protected Map<Integer, Task> tasks;
    protected HistoryManager historyManager;
    protected Set<Task> prioritizedTasks;


    public InMemoryTaskManager() {
        this.tasks = new HashMap<>();
        this.prioritizedTasks = new TreeSet<>();
        this.historyManager = Managers.getDefaultHistory();
    }

    @Override
    public HistoryManager getHistoryManager() {
        return historyManager;
    }

    @Override
    public void addTask(Task task) {

        if (task.getStartTime() != null &&
                !(task instanceof Epic) &&
                hasTimeOverlapWithAnyTask(task)) {

            throw new TimeConflictException("Задача " + task.getId() +
                    " пересекается по времени с существующей задачей.");
        }
        //запомним старую задачу, если была
        Task oldTask = tasks.get(task.getId());

        tasks.put(task.getId(), task);
        addPrioritizedTasks(task, oldTask);
    }

    public void addPrioritizedTasks(Task newTask, Task oldTask) {
        // 1. Если это Epic - игнорируем
        if (newTask instanceof Epic) {
            return;
        }

        // 2. Если у старой версии задачи была длительность, а у новой нет
        Duration duration = newTask.getDuration();
        if (duration == null || duration.isZero() || duration.isNegative()) {
            // Удаляем старую версию, а новую не добавляем
            if (oldTask != null && oldTask.getStartTime() != null) {
                prioritizedTasks.remove(oldTask);
            }
            return;
        }
        // 3. Если у новой версии startTime = null - нужно удалить старую версию, новую не добавляем
        if (newTask.getStartTime() == null) {
            if (oldTask != null && oldTask.getStartTime() != null) {
                prioritizedTasks.remove(oldTask);
            }
            return;
        }
        // 4. Новая задача с валидным startTime - удаляем старую версию, так как длительность может поменяться
        if (oldTask != null && oldTask.getStartTime() != null) {
            prioritizedTasks.remove(oldTask);
        }

        // Добавляем новую версию
        prioritizedTasks.add(newTask);
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

        if (tasks.isEmpty()) {
            return Collections.emptyMap();
        }

        return tasks.entrySet().stream().
                filter(entry -> entry.getValue().getClass() == taskType.getTaskClass())
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue
                ));
    }

    @Override
    public Set<Task> getPrioritizedTasks() {
        return prioritizedTasks;
    }

    @Override
    public void clearAllTasks() {
        int tasksSizeBefore = tasks.size();
        historyManager.clear();
        tasks.clear();
        prioritizedTasks.clear();
        System.out.println("Удалено задач " + tasksSizeBefore + " шт.");
    }

    @Override
    public void clearTasksByType(TaskType taskType) {
        int tasksSizeBefore = tasks.size();

        List<Task> toDelete = tasks.values().stream()
                .filter(task -> task.getClass() == taskType.getTaskClass())
                .toList();

        toDelete.forEach(this::deleteTask);

        System.out.println("Удалено задач " + (tasksSizeBefore - tasks.size()) + " шт.");
    }


    @Override
    public Task createTask(TaskType taskType, String name,
                           String description, Epic parentEpic,
                           LocalDateTime startTime, Duration duration) {

        Task task = switch (taskType) {
            case TASK -> new Task(name, description, startTime, duration);
            case EPIC -> new Epic(name, description);
            case SUBTASK -> new Subtask(name, description, parentEpic, startTime, duration);
        };

        addTask(task);
        return task;
    }

    @Override
    public Task updateTask(TaskType taskType, int id, String name,
                           String description, Status status,
                           LocalDateTime startTime, Duration duration) {

        Task newTask = switch (taskType) {
            case TASK -> new Task(id, name, description, status, startTime, duration);
            case EPIC -> new Epic(id, name, description,
                    ((Epic) tasks.get(id)).getSubtasks());
            case SUBTASK -> new Subtask(id, name, description, status,
                    ((Subtask) tasks.get(id)).getParentEpic(), startTime, duration);
        };

        addTask(newTask);
        return newTask;
    }

    @Override
    public boolean deleteTask(Task task) {
        if (task == null) {
            return false;
        }

        if (task instanceof Subtask subtask) {
            // Если это подзадача, сначала удаляем ссылку из эпика
            subtask.getParentEpic().deleteSubtask(subtask);
        } else if (task instanceof Epic epic) {
            // Если это эпик, сначала удаляем все его подзадачи
            for (Task subtask : epic.getSubtasks().values()) {
                historyManager.remove(subtask.getId());
                tasks.remove(subtask.getId());
                if (subtask.getStartTime() != null) {
                    prioritizedTasks.remove(subtask);
                }
            }
            epic.getSubtasks().clear();
        }

        // Удаляем саму задачу
        historyManager.remove(task.getId());
        tasks.remove(task.getId());

        // Удаляем из prioritizedTasks только если задача там была
        if (!(task instanceof Epic) &&
                task.getStartTime() != null &&
                task.getDuration() != null &&
                !task.getDuration().isZero()) {

            prioritizedTasks.remove(task);
        }
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

    protected boolean isTimeOverlap(Task task1, Task task2) {
        if (task1.getStartTime() == null || task2.getStartTime() == null ||
                task1.getDuration() == null || task2.getDuration() == null ||
                task1.getDuration().isZero() || task2.getDuration().isZero() ||
                task1 instanceof Epic || task2 instanceof Epic) {
            return false; // задачи без времени не могут пересекаться
        }

        LocalDateTime start1 = task1.getStartTime();
        LocalDateTime end1 = start1.plus(task1.getDuration());
        LocalDateTime start2 = task2.getStartTime();
        LocalDateTime end2 = start2.plus(task2.getDuration());

        return start1.isBefore(end2) && start2.isBefore(end1);
    }

    protected boolean hasTimeOverlapWithAnyTask(Task task) {
        if (task.getStartTime() == null || task instanceof Epic) {
            return false; // задачи без времени не могут пересекаться
        }

        return prioritizedTasks.stream()
                .filter(t -> !t.equals(task)) // исключаем саму задачу
                .anyMatch(t -> isTimeOverlap(task, t));
    }
}


