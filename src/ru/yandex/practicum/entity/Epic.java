package ru.yandex.practicum.entity;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.time.Duration;


public class Epic extends Task {

    protected Map<Integer, Task> subtasks;


    public Epic(String name, String description) {
        super(name, description);
        subtasks = new HashMap<>();
    }

    public Epic(int id, String name, String description) { //запись из файла
        super(id, name, description, null, null);
        subtasks = new HashMap<>();
        recountStatus();
    }

    public Epic(int id, String name, String description, Map<Integer, Task> subtasks) { // при обновлении
        super(id, name, description, null, null);
        this.subtasks = subtasks;
        removeNonSubtaskItems();
        recountStatus();
    }

    // конструктор копирования
    public Epic(Epic other) {
        super(other);
        this.subtasks = other.subtasks;
    }

    public Epic copy() {
        return new Epic(this);
    }

    @Override
    public LocalDateTime getStartTime() {
        if (subtasks.isEmpty()) {
            return null;
        }
        return subtasks.values().stream()
                .map(Task::getStartTime)
                .filter(Objects::nonNull)
                .min(LocalDateTime::compareTo)
                .orElse(null);
    }

    @Override
    public LocalDateTime getEndTime() {
        if (subtasks.isEmpty()) {
            return null;
        }
        return subtasks.values().stream()
                .map(Task::getEndTime)
                .filter(Objects::nonNull)
                .max(LocalDateTime::compareTo)
                .orElse(null);
    }

    @Override
    public Duration getDuration() {
        return subtasks.values().stream()
                .map(Task::getDuration)
                .filter(Objects::nonNull)
                .reduce(Duration.ZERO, Duration::plus);
    }


    public void removeNonSubtaskItems() {

        for (Task task : subtasks.values()) {
            if (!(task instanceof Subtask)) {
                subtasks.remove(task.getId());
            }
        }
    }

    public void addSubtask(Subtask subtask) {
        subtasks.put(subtask.getId(), subtask);
        recountStatus();
    }

    public void deleteSubtask(Subtask subtask) {
        subtasks.remove(subtask.getId());
        recountStatus();
    }

    public Map<Integer, Task> getSubtasks() {
        return subtasks;
    }

    public String getSubtasksListAsString() {
        StringBuilder sb = new StringBuilder();
        for (Task task : subtasks.values()) {
            if (!sb.isEmpty()) {
                sb.append(", ");
            }
            sb.append(task.hashCode());
        }
        return sb.toString();
    }

    public void recountStatus() {
        if (subtasks.isEmpty()) {
            status = Status.NEW;
            return;
        }
        boolean hasNew = false;
        for (Task subtask : subtasks.values()) {
            Status subtaskStatus = subtask.getStatus();
            if (subtaskStatus == Status.IN_PROGRESS) {
                status = Status.IN_PROGRESS;
                return;
            } else if (subtaskStatus == Status.NEW) {
                hasNew = true;
            }
        }
        status = hasNew ? Status.NEW : Status.DONE;
    }

    @Override
    public String toString() {
        return super.toString() +
                " subtasks= {" +
                getSubtasksListAsString() +
                '}';
    }
}


