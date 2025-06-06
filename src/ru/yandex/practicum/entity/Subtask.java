package ru.yandex.practicum.entity;

import com.google.gson.annotations.Expose;
import ru.yandex.practicum.exceptions.WrongParentEpicException;

import java.time.Duration;
import java.time.LocalDateTime;

public class Subtask extends Task {
    @Expose
    private Epic parentEpic;


    public Subtask(String name, String description, Epic parentEpic, LocalDateTime startTime, Duration duration) {
        super(name, description, startTime, duration);
        this.parentEpic = parentEpic;
        this.parentEpic.addSubtask(this);
    }

    public Subtask(int id, String name, String description, Status status,
                   Task parentEpic, LocalDateTime startTime, Duration duration) {
        super(id, name, description, status, startTime, duration);
        if (parentEpic instanceof Epic) {
            this.parentEpic = (Epic) parentEpic;
            this.parentEpic.addSubtask(this);
        } else {
            throw new WrongParentEpicException("Родительской задачи " + parentEpic.getId() + " не существует.");
        }
    }

    // конструктор копирования
    public Subtask(Subtask other) {
        super(other);
        this.parentEpic = other.parentEpic.copy();
    }

    public Subtask copy() {
        return new Subtask(this);
    }


    public Epic getParentEpic() {
        return parentEpic;
    }

    @Override
    public String toString() {
        return super.toString() +
                "parentEpic={" + (parentEpic != null ? parentEpic.getId() : "null") + '}';
    }
}

