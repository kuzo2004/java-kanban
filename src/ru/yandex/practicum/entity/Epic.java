package ru.yandex.practicum.entity;

import java.util.HashMap;
import java.util.Map;


public class Epic extends Task {

    protected Map<Integer, Task> subtasks = new HashMap<>();


    public Epic(String name, String description) {
        super(name, description);
    }

    public Epic(int uniqueID, String name, String description, Map<Integer, Task> subtasks) { // при обновлении
        super(uniqueID, name, description);
        this.subtasks = subtasks;
        recountStatus();
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

    public String printSubtasks() {
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
    // набор действий в каждом типе задачи свой, по смыслу это подготовка к удалению
    // хотелось все эти действия объединить в одном месте
    public boolean doBeforeDelete() {
        return checkBeforeDelete();
    }

    public  boolean checkBeforeDelete() {
        if (subtasks.isEmpty()) {
            return true;
        } else {
            System.out.println("Нельзя удалить задачу " + this.getId() + " так как ее список подзадач полон.");
            return false;
        }
    }

    @Override
    public String toString() {
        return super.toString() +
                " subtasks= {" +
                printSubtasks() +
                '}';
    }
}
