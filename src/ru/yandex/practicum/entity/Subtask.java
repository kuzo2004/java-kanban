package ru.yandex.practicum.entity;

public class Subtask extends Task {

    private Epic parentEpic;


    public Subtask(String name, String description, Epic parentEpic) {
        super(name, description);
        this.parentEpic = parentEpic;
        this.parentEpic.addSubtask(this);
    }

    public Subtask(int uniqueID, String name, String description, Status status, Epic parentEpic) {
        super(uniqueID, name, description, status);
        this.parentEpic = parentEpic;
        this.parentEpic.addSubtask(this);
    }

    public Subtask(int uniqueID, String name, String description, Status status, Task parentEpic) {
        super(uniqueID, name, description, status);
        if (parentEpic instanceof Epic) {
            this.parentEpic = (Epic) parentEpic;
            this.parentEpic.addSubtask(this);
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
                " parentEpic= " + (parentEpic != null ? parentEpic.getId() : "null") +
                '}';
    }
}

