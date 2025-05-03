package ru.yandex.practicum.entity;

import java.util.Objects;

public class Task {
    private int id;
    private String name;
    private String description;
    protected Status status;

    public static int counter;


    public Task(String name, String description) {
        this.id = generateId();
        this.name = name;
        this.description = description;
        this.status = Status.NEW;
    }

    public Task(int id, String name, String description) { // при обновлении (для наследников)
        this.id = id;
        this.name = name;
        this.description = description;

    }

    public Task(int id, String name, String description, Status status) { // при обновлении самого класса
        this(id, name, description);
        this.status = status;
    }

    // Конструктор копирования
    public Task(Task other) {
        this.id = other.id;
        this.name = other.name;
        this.description = other.description;
        this.status = other.status;
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
        return this.getClass().getSimpleName() + "     {" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", description.length='" + description.length() + '\'' +
                ", status=" + status +
                '}';
    }

    public String writeToString() {
        return id + "," +
                this.getClass().getSimpleName() + "," +
                name + "," +
                status + "," +
                (description.isBlank() ? " " : description) + ",";
    }
}

