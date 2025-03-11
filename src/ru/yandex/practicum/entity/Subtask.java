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
    // набор действий в каждом типе задачи свой, по смыслу это подготовка к удалению
    // хотелось все эти действия объединить в одном месте
    public boolean doBeforeDelete() {
        parentEpic.deleteSubtask(this); //удалить ссылку на эту подзадачу из списка подзадач, который хранится Epic
        return true;
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
