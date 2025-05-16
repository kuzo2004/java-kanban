package ru.yandex.practicum.manager;


import ru.yandex.practicum.service.*;

import java.io.File;


public class Managers {

    private static final File DEFAULT_TASKS_FILE = new File("tasks.csv");


    // поле для целей тестирования, чтобы подменять на временные файлы в момент теста
    private static File tasksFile = DEFAULT_TASKS_FILE;

    public static void setDefaultTasksFile(File file) {
        tasksFile = file;
    }


    public static TaskManager getDefault() {
        return FileBackedTaskManager.loadFromFile(tasksFile); // менеджер можно переключать на new InMemoryTaskManager();
    }

    public static HistoryManager getDefaultHistory() {
        return new InMemoryHistoryManager();
    }
}