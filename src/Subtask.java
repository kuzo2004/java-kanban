public class Subtask extends Task {

    private Epic parentTask;


    public Subtask(String name, String description, Epic parentTask) { // для создания новой задачи
        super(name, description);
        this.parentTask = parentTask;
        this.parentTask.addSubtask(this);
    }

    public Subtask(int uniqueID, String name, String description, Status status, Epic parentTask) {// при обновлении
        super(uniqueID, name, description, status);
        this.parentTask = parentTask;
        this.parentTask.addSubtask(this);
    }

    @Override
    public boolean prepareDelete() {
        parentTask.deleteSubtask(this);
        return true;
    }

    public Epic getParentTask() {
        return parentTask;
    }

    @Override
    public String toString() {
        return super.toString() +
                " parentTask= " + parentTask.getUniqueID() +
                '}';
    }
}
