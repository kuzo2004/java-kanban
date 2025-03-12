package ru.yandex.practicum.entity;

public class Subtask extends Task {

    private Epic parentEpic;


    public Subtask(String name, String description, Epic parentEpic) {
        super(name, description);
        this.parentEpic = parentEpic;
        this.parentEpic.addSubtask(this);
    }

    public Subtask(int uniqueID, String name, String description, Status status, Epic parentEpic) {// при обновлении
        super(uniqueID, name, description, status);
        this.parentEpic = parentEpic;
        this.parentEpic.addSubtask(this);
    }

    @Override
    public void doBeforeDelete() {
        // Удалить эту подзадачу из списка подзадач. Этот список хранится у родителя(Epic).
        parentEpic.deleteSubtask(this);
    }

    public Epic getParentEpic() {
        return parentEpic;
    }

    @Override
    public String toString() {
        return super.toString() +
                " parentEpic= " + parentEpic.getId() +
                '}';
    }
}
