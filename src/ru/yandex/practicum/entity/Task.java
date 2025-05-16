package ru.yandex.practicum.entity;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class Task implements Comparable<Task> {
    private int id;
    private String name;
    private String description;
    protected Status status;
    private LocalDateTime startTime;
    private Duration duration;

    public static int counter;
    public static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
    public static final long MAX_DURATION_MINUTES = TimeUnit.DAYS.toMinutes(30);  // например, 30 дней

    public Task(String name, String description) {
        this.id = generateId();
        this.name = name;
        this.description = description;
        this.status = Status.NEW;
    }


    public Task(String name, String description, LocalDateTime startTime, Duration duration) {
        this(name, description);
        this.startTime = startTime;
        this.duration = duration;

    }

    // при обновлении (для наследников)
    public Task(int id, String name, String description, LocalDateTime startTime, Duration duration) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.startTime = startTime;
        this.duration = duration;

    }

    // при обновлении самого класса
    public Task(int id, String name, String description, Status status, LocalDateTime startTime, Duration duration) {
        this(id, name, description, startTime, duration);
        this.status = status;
    }

    // Конструктор копирования
    public Task(Task other) {
        this.id = other.id;
        this.name = other.name;
        this.description = other.description;
        this.status = other.status;
        this.duration = other.duration;
        this.startTime = other.startTime;
    }

    public Task copy() {
        return new Task(this);
    }

    public int generateId() {
        return ++counter;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Status getStatus() {
        return status;
    }

    public TaskType getTaskType() {
        return TaskType.valueOf(this.getClass().getSimpleName().toUpperCase());
    }

    public Duration getDuration() {
        return duration;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public LocalDateTime getEndTime() {
        if (startTime == null || duration == null) {
            return null;
        }
        return startTime.plus(duration);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Task task = (Task) o;
        return id == task.id;
    }

    @Override
    public int compareTo(Task other) {
        if (other == null) {
            throw new NullPointerException("Задача для сравнения не может быть null");
        }
        if (startTime == null || other.getStartTime() == null) {
            throw new IllegalArgumentException("startTime не может быть null");
        }
        if (other instanceof Epic) {
            throw new ClassCastException("Объект не должен быть типа Epic");
        }
        return startTime.compareTo(other.getStartTime());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        String className = String.format("%-8s{", this.getClass().getSimpleName());
        String idStr = "id=" + String.format("%-3s", (id + ","));

        // Название задачи (сокращение, если слишком длинное)
        String nameStr = " name=" + (name.length() > 20
                ? name.substring(0, 17) + "...,"
                : String.format("%-21s", (name + ",")));

        // Описание задачи (сокращение, если слишком длинное)
        String descriptionStr = " descrpt" + (description.length() > 20
                ? description.substring(0, 17) + "...,"
                : String.format("%-21s", (description + ",")));

        // Статус
        String statusStr = " status=" + String.format("%-12s", (status + ","));

        // Время начала (с проверкой на null и форматированием)
        String startTimeStr = " start=" + (getStartTime() != null
                ? String.format("%-15s", getStartTime().format(DATE_TIME_FORMATTER) + ",")
                : String.format("%-15s", "null,"));

        // Время окончания (с проверкой на null и форматированием)
        String endTimeStr = " end=" + (getEndTime() != null
                ? String.format("%-15s", getEndTime().format(DATE_TIME_FORMATTER) + ",")
                : String.format("%-15s", "null,"));

        // Продолжительность (с проверкой на null)
        String durationStr = " dur=" + (getDuration() != null
                ? String.format("%-8s", getDuration().toMinutes() + " мин}")
                : String.format("%-8s", "null}"));

        // Сборка всех частей в одну строку
        return className + idStr + nameStr + descriptionStr +
                statusStr + startTimeStr + endTimeStr + durationStr;
    }

    public String writeToString() {
        // Основная часть (общая для всех задач)
        String mainPart = String.join(",",
                String.valueOf(id),
                this.getClass().getSimpleName(),
                name,
                status.toString(),
                description.isBlank() ? " " : description
        );

        // Часть для подзадачи (ID эпика) или пробел для других типов
        String subtaskPart = this instanceof Subtask ?
                String.valueOf(((Subtask) this).getParentEpic().getId()) : " ";

        // Часть с временем (пустая для Epic)
        String timePart;
        if (this instanceof Epic) {
            timePart = ",,";  // Пустые значения для Epic
        } else {
            timePart = String.join(",",
                    getStartTime() != null ? getStartTime().format(DATE_TIME_FORMATTER) : " ",
                    getDuration() != null ? String.valueOf(getDuration().toMinutes()) : " "
            );
        }
        return String.join(",", mainPart, subtaskPart, timePart) + ",";
    }
}

