package ru.yandex.practicum.manager;


import ru.yandex.practicum.service.*;

import java.io.File;


public class Managers {

    private static final File DEFAULT_TASKS_FILE = new File("tasks.csv");

    public static TaskManager getDefault() {
        return FileBackedTaskManager.loadFromFile(DEFAULT_TASKS_FILE);
    }

    public static HistoryManager getDefaultHistory() {
        return new InMemoryHistoryManager();
    }
}