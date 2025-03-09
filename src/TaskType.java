public enum TaskType {
    TASK(Task.class),
    EPIC(Epic.class),
    SUBTASK(Subtask.class);

    private final Class<? extends Task> taskClass;

    TaskType(Class<? extends Task> taskClass) {
        this.taskClass = taskClass;
    }

    public Class<? extends Task> getTaskClass() {
        return taskClass;
    }
}
