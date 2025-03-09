import java.util.Objects;

public class Task {
    private int UniqueID;
    private String name;
    private String description;
    protected Status status;

    public static int counter;


    public Task(String name, String description) { // для создания новой задачи
        this.name = name;
        this.description = description;
        this.status = Status.NEW;
        counter++;
        this.UniqueID = counter;
    }

    public Task(int uniqueID, String name, String description) { // при обновлении (для наследников)
        UniqueID = uniqueID;
        this.name = name;
        this.description = description;

    }

    public Task(int uniqueID, String name, String description, Status status) { // при обновлении самого класса
        this(uniqueID, name, description);
        this.status = status;
    }


    public int getUniqueID() {
        return UniqueID;
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

    public boolean prepareDelete() { // в наследниках переопределяем
        return true;
    }  // переопределяется в наследниках, здесь пока не нужен

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Task task = (Task) o;
        return UniqueID == task.UniqueID;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(UniqueID);
    }

    @Override
    public String toString() {
        return this.getClass().getName() + "     {" +
                "UniqueID=" + UniqueID +
                ", name='" + name + '\'' +
                ", description.length='" + description.length() + '\'' +
                ", status=" + status +
                '}';
    }
}
