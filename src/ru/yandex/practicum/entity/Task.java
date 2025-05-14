package ru.yandex.practicum.entity;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

public class Task {
    private int id;
    private String name;
    private String description;
    protected Status status;
    private LocalDateTime startTime;
    private Duration duration;

    public static int counter;
    private static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

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
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        String className = String.format("%-12s{", this.getClass().getSimpleName());
        String idStr = "id=" + String.format("%-4s", (id + ","));

        // Название задачи (сокращение, если слишком длинное)
        String nameStr = " name=" + (name.length() > 20
                ? name.substring(0, 17) + "...,"
                : String.format("%-21s", (name + ",")));

        // Описание задачи (сокращение, если слишком длинное)
        String descriptionStr = " description=" + (description.length() > 20
                ? description.substring(0, 17) + "...,"
                : String.format("%-21s", (description + ",")));

        // Статус
        String statusStr = " status=" + String.format("%-12s", (status + ","));

        // Время начала (с проверкой на null и форматированием)
        String startTimeStr = " startTime=" + (getStartTime() != null
                ? String.format("%-15s", getStartTime().format(DATE_TIME_FORMATTER) + ",")
                : String.format("%-15s", "null,"));

        // Время окончания (с проверкой на null и форматированием)
        String endTimeStr = " endTime=" + (getEndTime() != null
                ? String.format("%-15s", getEndTime().format(DATE_TIME_FORMATTER) + ",")
                : String.format("%-15s", "null,"));

        // Продолжительность (с проверкой на null)
        String durationStr = " duration=" + (getDuration() != null
                ? String.format("%-15s", getDuration().toMinutes() + " мин}")
                : String.format("%-15s", "null}"));

        // Сборка всех частей в одну строку
        return className + idStr + nameStr + descriptionStr +
                statusStr + startTimeStr + endTimeStr + durationStr;
    }

    public String writeToString() {
        return id + "," +
                this.getClass().getSimpleName() + "," +
                name + "," +
                status + "," +
                (description.isBlank() ? " " : description) + "," +
                (getStartTime()!= null ? getStartTime() : " ," )+
                (getDuration()!= null ? getDuration() : " ,");
    }
}

